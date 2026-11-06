package org.example.repository;

import org.example.model.CommercialOffer;
import org.example.model.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommercialOfferRepository extends JpaRepository<CommercialOffer, Long> {
    List<CommercialOffer> findByPurchaseRequestId(Long requestId);

    List<CommercialOffer> findBySupplierIdAndStatus(Long supplierId, OfferStatus status);

    boolean existsByPurchaseRequestIdAndSupplierId(Long requestId, Long supplierId);
}