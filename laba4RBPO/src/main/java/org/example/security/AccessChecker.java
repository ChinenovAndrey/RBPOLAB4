package org.example.security;

import org.example.model.PurchaseOrder;
import org.example.model.PurchaseRequest;
import org.example.repository.CommercialOfferRepository;
import org.example.repository.PurchaseRequestRepository;
import org.example.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("accessChecker")
public class AccessChecker {

    private final UserRepository userRepository;
    private final PurchaseRequestRepository requestRepository;
    private final CommercialOfferRepository offerRepository;

    public AccessChecker(UserRepository userRepository,
                         PurchaseRequestRepository requestRepository,
                         CommercialOfferRepository offerRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.offerRepository = offerRepository;
    }

    public boolean isSelf(Long userId, Authentication authentication) {
        if (userId == null || authentication == null) {
            return false;
        }
        return userRepository.findById(userId)
                .map(u -> u.getUsername().equalsIgnoreCase(authentication.getName()))
                .orElse(false);
    }

    public boolean isRequestOwner(Long requestId, Authentication authentication) {
        if (requestId == null || authentication == null) {
            return false;
        }
        return requestRepository.findById(requestId)
                .map(r -> isAuthor(r, authentication))
                .orElse(false);
    }

    public boolean isNotRequestAuthorForOffer(Long offerId, Authentication authentication) {
        if (offerId == null || authentication == null) {
            return true;
        }
        return offerRepository.findById(offerId)
                .map(o -> !isAuthor(o.getPurchaseRequest(), authentication))
                .orElse(true);
    }

    public boolean isNotRequestAuthorForOrder(PurchaseOrder order, Authentication authentication) {
        if (order == null || order.getSelectedOffer() == null) {
            return true;
        }
        return isNotRequestAuthorForOffer(order.getSelectedOffer().getId(), authentication);
    }

    private boolean isAuthor(PurchaseRequest request, Authentication authentication) {
        return request != null
                && request.getAuthor() != null
                && request.getAuthor().getUsername() != null
                && request.getAuthor().getUsername().equalsIgnoreCase(authentication.getName());
    }
}