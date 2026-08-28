package com.example.booking.repository;

import com.example.booking.entity.Reservation;
import com.example.booking.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
        SELECT r FROM Reservation r
        WHERE (:status IS NULL OR r.status = :status)
          AND (:minPrice IS NULL OR r.price >= :minPrice)
          AND (:maxPrice IS NULL OR r.price <= :maxPrice)
          AND (:username IS NULL OR r.user.username = :username)
        """)
    Page<Reservation> search(
            @Param("status") ReservationStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("username") String username,
            Pageable pageable);

    @Query("""
        SELECT COUNT(r) > 0 FROM Reservation r
        WHERE r.resource.id = :resourceId
          AND r.status <> com.example.booking.entity.ReservationStatus.CANCELLED
          AND r.startTime < :endTime
          AND r.endTime > :startTime
        """)
    boolean existsOverlappingReservation(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    @Modifying
    @Query("DELETE FROM Reservation r WHERE r.resource.id = :resourceId")
    void deleteByResourceId(@Param("resourceId") Long resourceId);
}
