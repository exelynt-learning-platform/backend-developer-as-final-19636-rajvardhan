package com.example.booking.service;

import com.example.booking.dto.reservation.*;
import com.example.booking.entity.*;
import com.example.booking.exception.*;
import com.example.booking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ResourceRepository resourceRepository;
    @Autowired
    UserRepository userRepository;

    @Transactional
    public ReservationResponse create(
            ReservationRequest request, String username) {

        validateTimeRange(request.startTime(), request.endTime());

        Optional<SystemUser> user = userRepository.findByUsername(username);

        Resource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found: " + request.resourceId()));

        if (!resource.isActive()) {
            throw new BadRequestException("Resource is not active");
        }

        if (reservationRepository.existsOverlappingReservation(
                resource.getId(), request.startTime(), request.endTime())) {
            throw new BadRequestException(
                    "Resource is already reserved for the requested time");
        }

        Reservation reservation = new Reservation();
        reservation.setResource(resource);
        reservation.setUser(user.orElse(null));
        reservation.setPrice(resource.getPrice());
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());
        reservation.setStatus(ReservationStatus.PENDING);


        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> search(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort,
            String username) {

        if (page < 0) {
            throw new BadRequestException("Page cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }

        if (minPrice != null && minPrice.signum() < 0) {
            throw new BadRequestException("Minimum price cannot be negative");
        }

        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new BadRequestException("Maximum price cannot be negative");
        }

        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException(
                    "Minimum price cannot exceed maximum price");
        }

        Pageable pageable = PageRequest.of(page, size, parseSort(sort));

        return reservationRepository.search(
                        status, minPrice, maxPrice, username, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getById(Long id, String username, boolean admin) {
        Reservation reservation = find(id);

        if (!admin && !reservation.getUser().getUsername().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You cannot access this reservation");
        }

        return toResponse(reservation);
    }

    @Transactional
    public ReservationResponse update(
            Long id, ReservationRequest request) {

        validateTimeRange(request.startTime(), request.endTime());

        Reservation reservation = find(id);

        Resource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found: " + request.resourceId()));

        if (!resource.isActive()) {
            throw new BadRequestException("Resource is not active");
        }

        if (reservationRepository.existsOverlappingReservation(
                resource.getId(), request.startTime(), request.endTime())) {

            // Existing reservation itself is allowed to occupy its original slot.
            if (!resource.getId().equals(reservation.getResource().getId())
                    || !request.startTime().equals(reservation.getStartTime())
                    || !request.endTime().equals(reservation.getEndTime())) {
                throw new BadRequestException(
                        "Resource is already reserved for the requested time");
            }
        }

        reservation.update(
                resource,
                resource.getPrice(),
                request.startTime(),
                request.endTime());

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse updateStatus(
            Long id, ReservationStatus status) {

        Reservation reservation = find(id);
        reservation.updateStatus(status);
        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse delete(Long id) {
        Reservation reservation = find(id);
        ReservationResponse response = toResponse(reservation);
        reservationRepository.delete(reservation);
        return response;
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by("id").ascending();
        }

        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();

        if (!property.matches("id|price|startTime|endTime|status")) {
            throw new BadRequestException("Unsupported sort field: " + property);
        }

        Sort.Direction direction = Sort.Direction.ASC;

        if (parts.length == 2) {
            direction = Sort.Direction.fromOptionalString(parts[1].trim())
                    .orElseThrow(() ->
                            new BadRequestException(
                                    "Sort direction must be asc or desc"));
        }

        return Sort.by(direction, property);
    }

    private void validateTimeRange(
            LocalDateTime startTime, LocalDateTime endTime) {

        if (!startTime.isBefore(endTime)) {
            throw new BadRequestException(
                    "Start time must be before end time");
        }
    }

    private Reservation find(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found: " + id));
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getResource().getId(),
                reservation.getResource().getName(),
                reservation.getUser().getUsername(),
                reservation.getPrice(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus());
    }
}
