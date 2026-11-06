package org.example.service;

import org.example.exception.ApiException;
import org.example.model.OrderStatus;
import org.example.model.PurchaseOrder;
import org.example.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository orderRepository;
    private final ProcurementService procurementService;

    public PurchaseOrderService(PurchaseOrderRepository orderRepository,
                                ProcurementService procurementService) {
        this.orderRepository = orderRepository;
        this.procurementService = procurementService;
    }

    public List<PurchaseOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<PurchaseOrder> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    /** Заказ создаётся только выбором предложения. */
    public PurchaseOrder createOrder(PurchaseOrder order) {
        if (order.getSelectedOffer() == null || order.getSelectedOffer().getId() == null) {
            throw ApiException.badRequest("Укажите предложение: \"selectedOffer\": {\"id\": 1}");
        }
        return procurementService.selectOffer(order.getSelectedOffer().getId());
    }

    /** Статус заказа меняется только через бизнес-операции. Здесь разрешено лишь завершение. */
    public PurchaseOrder updateOrder(Long id, PurchaseOrder updatedData) {
        if (!orderRepository.existsById(id)) {
            return null;
        }
        if (updatedData.getExecutionStatus() == OrderStatus.COMPLETED) {
            return procurementService.completeOrder(id);
        }
        throw ApiException.conflict("Заказ можно изменить только завершением (executionStatus = COMPLETED)");
    }

    public boolean deleteOrder(Long id) {
        PurchaseOrder order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return false;
        }
        if (order.getExecutionStatus() == OrderStatus.IN_PROGRESS) {
            throw ApiException.conflict("Заказ в работе удалить нельзя");
        }
        orderRepository.deleteById(id);
        return true;
    }
}