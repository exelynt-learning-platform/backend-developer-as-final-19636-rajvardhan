package com.example.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active = true;

    public Resource(String name, String description, BigDecimal price, boolean active) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.active = active;
    }

    public void update(String name, String description, BigDecimal price, boolean active) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.active = active;
    }
}
