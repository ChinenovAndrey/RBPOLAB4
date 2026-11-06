package org.example.dto;

import java.time.LocalDate;

public record PlaceOfferDto(Long requestId,
                            Long supplierId,
                            Double price,
                            String deliveryTerm,
                            LocalDate validUntil) {
}