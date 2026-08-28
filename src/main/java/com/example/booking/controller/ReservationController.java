package com.example.booking.controller;

import com.example.booking.dto.reservation.*;
import com.example.booking.entity.ReservationStatus;
import com.example.booking.service.ReservationService;
import com.example.booking.utility.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.create(
                        request, authentication.getName()));
    }

    @GetMapping
    public Page<ReservationResponse> search(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            Authentication authentication) {

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String username = admin ? null : authentication.getName();

        return reservationService.search(
                status, minPrice, maxPrice, page, size, sort, username);
    }

    @GetMapping("/{id}")
    public ReservationResponse getById(
            @PathVariable Long id,
            Authentication authentication) {

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return reservationService.getById(
                id, authentication.getName(), admin);
    }

    @PutMapping("/{id}")
    public ReservationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest request) {
        return reservationService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public ReservationResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatusRequest request) {
        return reservationService.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<ReservationResponse>> delete(@PathVariable Long id) {
        ReservationResponse deleted = reservationService.delete(id);
        return ResponseEntity.ok(BaseResponse.success("Reservation deleted successfully", deleted));
    }
}
