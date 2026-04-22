package com.example.backend_service.service;

import com.example.backend_service.model.*;
import com.example.backend_service.repository.CarRepository;
import com.example.backend_service.repository.MessageRepository;
import com.example.backend_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public CarService(CarRepository carRepository,
                      UserRepository userRepository,
                      MessageRepository messageRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Optional<Car> getCarById(Long id) {
        return carRepository.findById(id);
    }

    public Optional<Car> addCarForAuthenticatedUser(CarRequest request, String username) {
        Optional<User> ownerOptional = userRepository.findByUsername(username);

        if (ownerOptional.isEmpty()) {
            return Optional.empty();
        }

        Car car = new Car(
                request.getBrand(),
                request.getModel(),
                request.getYear(),
                request.getPrice(),
                request.getDescription(),
                ownerOptional.get()
        );

        return Optional.of(carRepository.save(car));
    }

    public List<Car> getCarsByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return List.of();
        }

        return carRepository.findByOwnerId(userOptional.get().getId());
    }

    public Optional<Message> sendMessageFromAuthenticatedUser(MessageRequest request, String username) {
        Optional<Car> carOptional = carRepository.findById(request.getCarId());
        Optional<User> senderOptional = userRepository.findByUsername(username);

        if (carOptional.isEmpty() || senderOptional.isEmpty()) {
            return Optional.empty();
        }

        Car car = carOptional.get();
        User sender = senderOptional.get();
        User receiver = car.getOwner();

        Message message = new Message(
                request.getContent(),
                car,
                sender,
                receiver
        );

        return Optional.of(messageRepository.save(message));
    }

    public List<Message> getReceivedMessagesByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return List.of();
        }

        return messageRepository.findByReceiverId(userOptional.get().getId());
    }
}