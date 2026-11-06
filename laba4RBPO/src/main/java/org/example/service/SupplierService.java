package org.example.service;

import org.example.exception.ApiException;
import org.example.model.Supplier;
import org.example.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    public Supplier createSupplier(Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().isBlank()) {
            throw ApiException.badRequest("Название поставщика обязательно");
        }
        if (supplier.getCategory() == null || supplier.getCategory().isBlank()) {
            throw ApiException.badRequest("Категория поставщика обязательна");
        }
        if (supplier.getIsApproved() == null) {
            supplier.setIsApproved(false);
        }
        supplier.setId(null);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(Long id, Supplier updatedData) {
        Supplier supplier = supplierRepository.findById(id).orElse(null);
        if (supplier == null) {
            return null;
        }
        if (updatedData.getName() != null) {
            if (updatedData.getName().isBlank()) {
                throw ApiException.badRequest("Название поставщика не может быть пустым");
            }
            supplier.setName(updatedData.getName());
        }
        if (updatedData.getCategory() != null) {
            supplier.setCategory(updatedData.getCategory());
        }
        if (updatedData.getIsApproved() != null) {
            supplier.setIsApproved(updatedData.getIsApproved());
        }
        return supplier;
    }

    public boolean deleteSupplier(Long id) {
        if (supplierRepository.existsById(id)) {
            supplierRepository.deleteById(id);
            return true;
        }
        return false;
    }
}