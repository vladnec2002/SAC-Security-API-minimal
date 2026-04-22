package com.example.backend_service.repository;

import com.example.backend_service.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByOwnerId(Long ownerId);
}