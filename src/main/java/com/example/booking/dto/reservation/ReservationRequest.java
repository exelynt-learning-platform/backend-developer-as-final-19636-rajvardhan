package com.example.booking.dto.reservation;

import java.time.LocalDateTime;

public record ReservationRequest(
        Long resourceId,
        LocalDateTime startTime,
        LocalDateTime endTime) {
}
