package org.example.controller;

import org.example.dto.CreateUserRequest;
import org.example.dto.UpdateUserRequest;
import org.example.dto.UserFullProfileDto;
import org.example.dto.UserResponse;
import org.example.model.Supplier;
import org.example.model.User;
import org.example.repository.CommercialOfferRepository;
import org.example.repository.PurchaseOrderRepository;
import org.example.repository.PurchaseRequestRepository;
import org.example.repository.SupplierRepository;
import org.example.repository.UserRepository;
import org.example.service.RegistrationService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private PurchaseRequestRepository purchaseRequestRepository;
    @Autowired
    private CommercialOfferRepository commercialOfferRepository;
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    private final UserService userService;
    private final RegistrationService registrationService;

    public UserController(UserService userService, RegistrationService registrationService) {
        this.userService = userService;
        this.registrationService = registrationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return userService.getByUsername(authentication.getName())
                .map(u -> ResponseEntity.ok(UserResponse.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accessChecker.isSelf(#id, authentication)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(u -> ResponseEntity.ok(UserResponse.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        User saved = registrationService.createByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(saved));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody UpdateUserRequest request,
                                                   Authentication authentication) {
        User updated = userService.updateUser(id, request, authentication.getName());
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(UserResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        boolean deleted = userService.deleteUser(id, authentication.getName());
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/full-profile")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN') or @accessChecker.isSelf(#id, authentication)")
    public ResponseEntity<UserFullProfileDto> getFullProfile(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        var requests = purchaseRequestRepository.findByAuthorId(id);

        var offers = commercialOfferRepository.findAll().stream()
                .filter(offer -> offer.getPurchaseRequest() != null &&
                        requests.contains(offer.getPurchaseRequest()))
                .toList();

        var orders = purchaseOrderRepository.findAll().stream()
                .filter(order -> order.getPurchaseRequest() != null &&
                        requests.contains(order.getPurchaseRequest()))
                .toList();

        var suppliers = commercialOfferRepository.findAll().stream()
                .filter(offer -> offer.getPurchaseRequest() != null &&
                        requests.contains(offer.getPurchaseRequest()))
                .map(offer -> (Supplier) offer.getSupplier())
                .distinct()
                .toList();

        UserFullProfileDto profile = new UserFullProfileDto(user, suppliers, requests, offers, orders);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/full-profiles/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserFullProfileDto>> getAllFullProfiles() {
        List<User> users = userRepository.findAll();
        List<UserFullProfileDto> allProfiles = new ArrayList<>();

        for (User u : users) {
            Long currentId = u.getId();

            var requests = purchaseRequestRepository.findByAuthorId(currentId);

            var offers = commercialOfferRepository.findAll().stream()
                    .filter(offer -> offer.getPurchaseRequest() != null &&
                            requests.contains(offer.getPurchaseRequest()))
                    .toList();

            var orders = purchaseOrderRepository.findAll().stream()
                    .filter(order -> order.getPurchaseRequest() != null &&
                            requests.contains(order.getPurchaseRequest()))
                    .toList();

            var suppliers = commercialOfferRepository.findAll().stream()
                    .filter(offer -> offer.getPurchaseRequest() != null &&
                            requests.contains(offer.getPurchaseRequest()))
                    .map(offer -> (Supplier) offer.getSupplier())
                    .distinct()
                    .toList();

            allProfiles.add(new UserFullProfileDto(u, suppliers, requests, offers, orders));
        }

        return ResponseEntity.ok(allProfiles);
    }
}