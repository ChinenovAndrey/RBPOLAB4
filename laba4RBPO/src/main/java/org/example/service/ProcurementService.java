package org.example.service;

import org.example.exception.ApiException;
import org.example.model.*;
import org.example.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ProcurementService {

    private final PurchaseRequestRepository requestRepository;
    private final CommercialOfferRepository offerRepository;
    private final PurchaseOrderRepository orderRepository;
    private final SupplierRepository supplierRepository;

    public ProcurementService(PurchaseRequestRepository requestRepository,
                              CommercialOfferRepository offerRepository,
                              PurchaseOrderRepository orderRepository,
                              SupplierRepository supplierRepository) {
        this.requestRepository = requestRepository;
        this.offerRepository = offerRepository;
        this.orderRepository = orderRepository;
        this.supplierRepository = supplierRepository;
    }

    /** Операция 1. Подать предложение по заявке (заявка + поставщик -> предложение). */
    @Transactional
    public CommercialOffer placeOffer(Long requestId, Long supplierId, Double price,
                                      String deliveryTerm, LocalDate validUntil) {
        if (requestId == null || supplierId == null) {
            throw ApiException.badRequest("Нужно указать заявку и поставщика");
        }
        if (price == null || price <= 0) {
            throw ApiException.badRequest("Цена должна быть больше нуля");
        }
        if (validUntil == null || validUntil.isBefore(LocalDate.now())) {
            throw ApiException.badRequest("Срок действия предложения не может быть в прошлом");
        }

        PurchaseRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> ApiException.notFound("Заявка не найдена"));
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> ApiException.notFound("Поставщик не найден"));

        if (request.getStatus() != RequestStatus.OPEN) {
            throw ApiException.conflict("Предложения принимаются только по открытым заявкам");
        }
        if (!Boolean.TRUE.equals(supplier.getIsApproved())) {
            throw ApiException.conflict("Поставщик не одобрен и не может подавать предложения");
        }
        if (price > request.getMaxBudget()) {
            throw ApiException.conflict("Цена превышает максимальный бюджет заявки");
        }
        if (offerRepository.existsByPurchaseRequestIdAndSupplierId(requestId, supplierId)) {
            throw ApiException.conflict("Этот поставщик уже подал предложение по данной заявке");
        }

        CommercialOffer offer = new CommercialOffer();
        offer.setPurchaseRequest(request);
        offer.setSupplier(supplier);
        offer.setPrice(price);
        offer.setDeliveryTerm(deliveryTerm);
        offer.setValidUntil(validUntil);
        offer.setStatus(OfferStatus.NEW);
        return offerRepository.save(offer);
    }

    /** Операция 2. Выбрать предложение: предложение + остальные предложения + заявка + новый заказ. */
    @Transactional
    public PurchaseOrder selectOffer(Long offerId) {
        CommercialOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> ApiException.notFound("Предложение не найдено"));
        PurchaseRequest request = offer.getPurchaseRequest();

        if (offer.getStatus() != OfferStatus.NEW) {
            throw ApiException.conflict("Выбрать можно только новое предложение");
        }
        if (request.getStatus() != RequestStatus.OPEN) {
            throw ApiException.conflict("Заявка уже не открыта: предложение выбрать нельзя");
        }
        if (offer.getValidUntil().isBefore(LocalDate.now())) {
            throw ApiException.conflict("Срок действия предложения истёк");
        }
        if (!Boolean.TRUE.equals(offer.getSupplier().getIsApproved())) {
            throw ApiException.conflict("Поставщик больше не одобрен");
        }

        for (CommercialOffer other : offerRepository.findByPurchaseRequestId(request.getId())) {
            if (other.getId().equals(offer.getId())) {
                other.setStatus(OfferStatus.SELECTED);
            } else if (other.getStatus() == OfferStatus.NEW) {
                other.setStatus(OfferStatus.REJECTED);
            }
        }
        request.setStatus(RequestStatus.ORDERED);

        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseRequest(request);
        order.setSelectedOffer(offer);
        order.setExecutionStatus(OrderStatus.IN_PROGRESS);
        return orderRepository.save(order);
    }

    /** Операция 3. Завершить заказ (заказ + заявка). */
    @Transactional
    public PurchaseOrder completeOrder(Long orderId) {
        PurchaseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Заказ не найден"));
        PurchaseRequest request = order.getPurchaseRequest();

        if (order.getExecutionStatus() != OrderStatus.IN_PROGRESS) {
            throw ApiException.conflict("Завершить можно только заказ в работе");
        }
        if (request.getStatus() != RequestStatus.ORDERED) {
            throw ApiException.conflict("Заявка находится в неподходящем статусе");
        }

        order.setExecutionStatus(OrderStatus.COMPLETED);
        request.setStatus(RequestStatus.COMPLETED);
        return order;
    }

    /** Операция 4. Отменить заявку (заявка + предложения + активный заказ). */
    @Transactional
    public PurchaseRequest cancelRequest(Long requestId) {
        PurchaseRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> ApiException.notFound("Заявка не найдена"));

        if (request.getStatus() == RequestStatus.COMPLETED || request.getStatus() == RequestStatus.CANCELLED) {
            throw ApiException.conflict("Выполненную или уже отменённую заявку отменить нельзя");
        }

        for (CommercialOffer offer : offerRepository.findByPurchaseRequestId(requestId)) {
            offer.setStatus(OfferStatus.REJECTED);
        }
        for (PurchaseOrder order : orderRepository.findByPurchaseRequestId(requestId)) {
            if (order.getExecutionStatus() == OrderStatus.IN_PROGRESS) {
                order.setExecutionStatus(OrderStatus.CANCELLED);
            }
        }
        request.setStatus(RequestStatus.CANCELLED);
        return request;
    }

    /** Операция 5. Отозвать одобрение поставщика (поставщик + его новые предложения; проверка заказов). */
    @Transactional
    public Supplier revokeSupplierApproval(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> ApiException.notFound("Поставщик не найден"));

        if (!Boolean.TRUE.equals(supplier.getIsApproved())) {
            throw ApiException.conflict("Поставщик уже не одобрен");
        }
        if (orderRepository.existsBySupplierAndStatus(supplierId, OrderStatus.IN_PROGRESS)) {
            throw ApiException.conflict("У поставщика есть заказ в работе: отозвать одобрение нельзя");
        }

        for (CommercialOffer offer : offerRepository.findBySupplierIdAndStatus(supplierId, OfferStatus.NEW)) {
            offer.setStatus(OfferStatus.REJECTED);
        }
        supplier.setIsApproved(false);
        return supplier;
    }
}