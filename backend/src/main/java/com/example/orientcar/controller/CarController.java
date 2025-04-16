package com.example.orientcar.controller;

import com.example.orientcar.model.Car;
import com.example.orientcar.repository.CarRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    @Autowired
    private CarRepository carRepository;

    // Pobierz wszystkie auta
    @GetMapping
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    // Pobierz tylko dostępne
    @GetMapping("/available")
    public List<Car> getAvailableCars() {
        return carRepository.findByAvailableTrue();
    }

    // Dodaj nowe auto (przykład: wymaga roli ADMIN)
    @PostMapping
    public Object createCar(HttpServletRequest request, @RequestBody Car car) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return "Brak tokena";
        }
        String role = claims.get("role", String.class);
        if (!"ADMIN".equals(role)) {
            return "Brak uprawnień (ADMIN)";
        }
        return carRepository.save(car);
    }
}