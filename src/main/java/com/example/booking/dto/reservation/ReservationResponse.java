package com.example.booking.dto.reservation;

import com.example.booking.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long resourceId,
        String resourceName,
        String username,
        BigDecimal price,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ReservationStatus status) {
}