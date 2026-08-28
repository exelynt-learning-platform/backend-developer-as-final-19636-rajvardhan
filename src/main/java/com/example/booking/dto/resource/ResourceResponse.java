package com.example.booking.dto.resource;

import java.math.BigDecimal;

public record ResourceResponse(Long id,
                               String name,
                               String description,
                               BigDecimal price,
                               boolean active) {

}
