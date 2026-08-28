package com.example.booking.dto.resource;

import java.math.BigDecimal;

public record ResourceRequest(
        String name,
        String description,
        BigDecimal price,
        Boolean active) {
}
