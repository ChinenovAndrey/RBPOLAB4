package org.example.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "purchase_orders")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = PurchaseOrder.class)
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_request_id")
    private PurchaseRequest purchaseRequest;

    @ManyToOne
    @JoinColumn(name = "commercial_offer_id")
    private CommercialOffer selectedOffer;

    @Enumerated(EnumType.STRING)
    private OrderStatus executionStatus = OrderStatus.IN_PROGRESS;

    public PurchaseOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PurchaseRequest getPurchaseRequest() { return purchaseRequest; }
    public void setPurchaseRequest(PurchaseRequest purchaseRequest) { this.purchaseRequest = purchaseRequest; }

    public CommercialOffer getSelectedOffer() { return selectedOffer; }
    public void setSelectedOffer(CommercialOffer selectedOffer) { this.selectedOffer = selectedOffer; }

    public OrderStatus getExecutionStatus() { return executionStatus; }
    public void setExecutionStatus(OrderStatus executionStatus) { this.executionStatus = executionStatus; }
}