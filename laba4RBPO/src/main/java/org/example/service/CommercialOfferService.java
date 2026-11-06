package org.example.service;

import org.example.exception.ApiException;
import org.example.model.CommercialOffer;
import org.example.model.OfferStatus;
import org.example.repository.CommercialOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CommercialOfferService {

    private final CommercialOfferRepository offerRepository;
    private final ProcurementService procurementService;

    public CommercialOfferService(CommercialOfferRepository offerRepository,
                                  ProcurementService procurementService) {
        this.offerRepository = offerRepository;
        this.procurementService = procurementService;
    }

    public List<CommercialOffer> getAllOffers() {
        return offerRepository.findAll();
    }

    public Optional<CommercialOffer> getOfferById(Long id) {
        return offerRepository.findById(id);
    }

    /** Создание предложения идёт через бизнес-операцию, чтобы нельзя было обойти правила. */
    public CommercialOffer createOffer(CommercialOffer offer) {
        if (offer.getPurchaseRequest() == null || offer.getPurchaseRequest().getId() == null
                || offer.getSupplier() == null || offer.getSupplier().getId() == null) {
            throw ApiException.badRequest(
                    "Укажите заявку и поставщика: \"purchaseRequest\": {\"id\": 1}, \"supplier\": {\"id\": 1}");
        }
        return procurementService.placeOffer(
                offer.getPurchaseRequest().getId(),
                offer.getSupplier().getId(),
                offer.getPrice(),
                offer.getDeliveryTerm(),
                offer.getValidUntil());
    }

    @Transactional
    public CommercialOffer updateOffer(Long id, CommercialOffer updatedData) {
        CommercialOffer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) {
            return null;
        }
        if (offer.getStatus() != OfferStatus.NEW) {
            throw ApiException.conflict("Изменять можно только новое предложение");
        }
        if (updatedData.getPrice() != null) {
            if (updatedData.getPrice() <= 0) {
                throw ApiException.badRequest("Цена должна быть больше нуля");
            }
            if (updatedData.getPrice() > offer.getPurchaseRequest().getMaxBudget()) {
                throw ApiException.conflict("Цена превышает максимальный бюджет заявки");
            }
            offer.setPrice(updatedData.getPrice());
        }
        if (updatedData.getDeliveryTerm() != null) {
            offer.setDeliveryTerm(updatedData.getDeliveryTerm());
        }
        if (updatedData.getValidUntil() != null) {
            if (updatedData.getValidUntil().isBefore(LocalDate.now())) {
                throw ApiException.badRequest("Срок действия не может быть в прошлом");
            }
            offer.setValidUntil(updatedData.getValidUntil());
        }
        return offer;
    }

    public boolean deleteOffer(Long id) {
        CommercialOffer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) {
            return false;
        }
        if (offer.getStatus() == OfferStatus.SELECTED) {
            throw ApiException.conflict("Выбранное предложение удалить нельзя");
        }
        offerRepository.deleteById(id);
        return true;
    }
}