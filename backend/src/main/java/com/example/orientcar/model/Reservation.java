package com.example.orientcar.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // w praktyce: @ManyToOne do User
    private Long carId;  // w praktyce: @ManyToOne do Car

    private LocalDate startDate;
    private LocalDate endDate;
    private String licenseNumber;
    private int driversCount;
    private double totalPrice;
    private String status;
}