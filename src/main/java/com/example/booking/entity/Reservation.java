package com.example.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private SystemUser user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    public Reservation(Resource resource, SystemUser user, BigDecimal price,
            LocalDateTime startTime, LocalDateTime endTime,
            ReservationStatus status) {
        this.resource = resource;
        this.user = user;
        this.price = price;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public void update(Resource resource, BigDecimal price,
            LocalDateTime startTime, LocalDateTime endTime) {
        this.resource = resource;
        this.price = price;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }
}
