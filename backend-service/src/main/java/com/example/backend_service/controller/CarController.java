package com.example.backend_service.controller;

import com.example.backend_service.model.Car;
import com.example.backend_service.model.MessageRequest;
import com.example.backend_service.service.CarService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> addCar(@RequestBody Car car) {
        return ResponseEntity.ok(carService.addCar(car));
    }

    @PostMapping("/private/messages")
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest request) {
        return ResponseEntity.ok(Map.of(
                "status", "Message sent",
                "carId", request.getCarId(),
                "senderName", request.getSenderName()
        ));
    }

    @GetMapping("/private/profile")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok(Map.of(
                "username", "demo-user",
                "role", "USER"
        ));
    }
}