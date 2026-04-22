package com.example.backend_service.controller;

import com.example.backend_service.model.CarRequest;
import com.example.backend_service.model.MessageRequest;
import com.example.backend_service.service.CarService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/public/cars")
    public ResponseEntity<?> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars());
    }

    @GetMapping("/public/cars/{id}")
    public ResponseEntity<?> getCarById(@PathVariable Long id) {
        return carService.getCarById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Car not found")));
    }

    @PostMapping("/private/cars")
    public ResponseEntity<?> addCar(@RequestBody CarRequest request, Authentication authentication) {
        String username = authentication.getName();

        return carService.addCarForAuthenticatedUser(request, username)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "Owner not found")));
    }

    @GetMapping("/private/my-cars")
    public ResponseEntity<?> getMyCars(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(carService.getCarsByUsername(username));
    }

    @PostMapping("/private/messages")
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest request, Authentication authentication) {
        String username = authentication.getName();

        return carService.sendMessageFromAuthenticatedUser(request, username)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "Car or sender not found")));
    }

    @GetMapping("/private/messages/received")
    public ResponseEntity<?> getReceivedMessages(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(carService.getReceivedMessagesByUsername(username));
    }

    @GetMapping("/private/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "role", "USER"
        ));
    }
}