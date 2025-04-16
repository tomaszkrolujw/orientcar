package com.example.orientcar.controller;

import com.example.orientcar.model.Reservation;
import com.example.orientcar.repository.ReservationRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    // Pobierz wszystkie rezerwacje (tylko ADMIN)
    @GetMapping
    public Object getAllReservations(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return "Brak tokena";
        }
        String role = claims.get("role", String.class);
        if (!"ADMIN".equals(role)) {
            return "Brak uprawnień (ADMIN)";
        }
        return reservationRepository.findAll();
    }

    // Tworzenie nowej rezerwacji (wystarczy rola USER)
    @PostMapping
    public Object createReservation(HttpServletRequest request, @RequestBody Reservation reservation) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return "Brak tokena";
        }
        // W realnym projekcie powiąż usera i car z DB
        reservation.setUserId(0L);
        reservation.setStatus("Oczekująca");
        if (reservation.getStartDate() == null) {
            reservation.setStartDate(LocalDate.now());
        }
        if (reservation.getEndDate() == null) {
            reservation.setEndDate(LocalDate.now().plusDays(1));
        }
        return reservationRepository.save(reservation);
    }
}