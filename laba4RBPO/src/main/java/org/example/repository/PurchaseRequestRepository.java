package org.example.repository;

import org.example.model.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    List<PurchaseRequest> findByAuthorId(Long authorId);

    List<PurchaseRequest> findByAuthorUsername(String username);
}