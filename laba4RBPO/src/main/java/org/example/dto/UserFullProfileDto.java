package org.example.dto;

import org.example.model.*;
import java.util.List;

public class UserFullProfileDto {
    private User user;
    private List<Supplier> suppliers;
    private List<PurchaseRequest> purchaseRequests;
    private List<CommercialOffer> commercialOffers;
    private List<PurchaseOrder> purchaseOrders;

    public UserFullProfileDto(User user, List<Supplier> suppliers, List<PurchaseRequest> purchaseRequests,
                              List<CommercialOffer> commercialOffers, List<PurchaseOrder> purchaseOrders) {
        this.user = user;
        this.suppliers = suppliers;
        this.purchaseRequests = purchaseRequests;
        this.commercialOffers = commercialOffers;
        this.purchaseOrders = purchaseOrders;
    }

    public User getUser() { return user; }
    public List<Supplier> getSuppliers() { return suppliers; }
    public List<PurchaseRequest> getPurchaseRequests() { return purchaseRequests; }
    public List<CommercialOffer> getCommercialOffers() { return commercialOffers; }
    public List<PurchaseOrder> getPurchaseOrders() { return purchaseOrders; }
}