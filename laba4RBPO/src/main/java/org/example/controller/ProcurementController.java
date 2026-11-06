package org.example.controller;

import org.example.dto.PlaceOfferDto;
import org.example.model.CommercialOffer;
import org.example.model.PurchaseOrder;
import org.example.model.PurchaseRequest;
import org.example.model.Supplier;
import org.example.service.ProcurementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations")
public class ProcurementController {

    private final ProcurementService procurementService;

    public ProcurementController(ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    @PostMapping("/offers")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<CommercialOffer> placeOffer(@RequestBody PlaceOfferDto dto) {
        CommercialOffer offer = procurementService.placeOffer(
                dto.requestId(), dto.supplierId(), dto.price(), dto.deliveryTerm(), dto.validUntil());
        return ResponseEntity.status(HttpStatus.CREATED).body(offer);
    }

    @PostMapping("/offers/{offerId}/select")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @accessChecker.isNotRequestAuthorForOffer(#offerId, authentication))")
    public ResponseEntity<PurchaseOrder> selectOffer(@PathVariable Long offerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(procurementService.selectOffer(offerId));
    }

    @PostMapping("/orders/{orderId}/complete")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<PurchaseOrder> completeOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(procurementService.completeOrder(orderId));
    }

    @PostMapping("/requests/{requestId}/cancel")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN') or @accessChecker.isRequestOwner(#requestId, authentication)")
    public ResponseEntity<PurchaseRequest> cancelRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(procurementService.cancelRequest(requestId));
    }

    @PostMapping("/suppliers/{supplierId}/revoke-approval")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Supplier> revokeApproval(@PathVariable Long supplierId) {
        return ResponseEntity.ok(procurementService.revokeSupplierApproval(supplierId));
    }
}