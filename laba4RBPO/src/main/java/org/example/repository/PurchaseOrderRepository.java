package org.example.repository;

import org.example.model.OrderStatus;
import org.example.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByPurchaseRequestId(Long requestId);

    @Query("select count(o) > 0 from PurchaseOrder o " +
            "where o.selectedOffer.supplier.id = :supplierId and o.executionStatus = :status")
    boolean existsBySupplierAndStatus(@Param("supplierId") Long supplierId,
                                      @Param("status") OrderStatus status);
}