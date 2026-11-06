package org.example.controller;

import org.example.model.PurchaseRequest;
import org.example.security.CurrentUser;
import org.example.service.PurchaseRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-requests")
public class PurchaseRequestController {

    private final PurchaseRequestService requestService;

    public PurchaseRequestController(PurchaseRequestService requestService) {
        this.requestService = requestService;
    }

    /** Менеджер и администратор видят все заявки, сотрудник только свои. */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<PurchaseRequest> getAllRequests(Authentication authentication) {
        if (CurrentUser.hasAnyRole(authentication, "MANAGER", "ADMIN")) {
            return requestService.getAllRequests();
        }
        return requestService.getRequestsByAuthor(authentication.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN') or @accessChecker.isRequestOwner(#id, authentication)")
    public ResponseEntity<PurchaseRequest> getRequestById(@PathVariable Long id) {
        return requestService.getRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PurchaseRequest> createRequest(@RequestBody PurchaseRequest request,
                                                         Authentication authentication) {
        PurchaseRequest saved = requestService.createRequest(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN') or @accessChecker.isRequestOwner(#id, authentication)")
    public ResponseEntity<PurchaseRequest> updateRequest(@PathVariable Long id, @RequestBody PurchaseRequest updatedData) {
        PurchaseRequest updated = requestService.updateRequest(id, updatedData);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN') or @accessChecker.isRequestOwner(#id, authentication)")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        boolean deleted = requestService.deleteRequest(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}