package com.example.backend_service.service;

import com.example.backend_service.model.Car;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CarService {

    private final List<Car> cars = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(3);

    public CarService() {
        cars.add(new Car(1L, "BMW", "320d", 2018, 16500, "Andrei", "Masina bine intretinuta, fara accidente."));
        cars.add(new Car(2L, "Audi", "A4", 2017, 14900, "Mihai", "Revizii la zi, stare foarte buna."));
        cars.add(new Car(3L, "Volkswagen", "Passat", 2019, 17800, "Cristian", "Unic proprietar, kilometri reali."));
    }

    public List<Car> getAllCars() {
        return cars;
    }

    public Optional<Car> getCarById(Long id) {
        return cars.stream().filter(car -> car.getId().equals(id)).findFirst();
    }

    public Car addCar(Car car) {
        car.setId(idGenerator.incrementAndGet());
        cars.add(car);
        return car;
    }
}