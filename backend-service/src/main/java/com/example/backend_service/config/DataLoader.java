package com.example.backend_service.config;

import com.example.backend_service.model.Car;
import com.example.backend_service.model.Message;
import com.example.backend_service.model.User;
import com.example.backend_service.repository.CarRepository;
import com.example.backend_service.repository.MessageRepository;
import com.example.backend_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadData(UserRepository userRepository,
                               CarRepository carRepository,
                               MessageRepository messageRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {

            if (userRepository.count() == 0) {

                User u1 = userRepository.save(new User(
                        "andrei",
                        "andrei@mail.com",
                        passwordEncoder.encode("1234"),
                        "USER"
                ));

                User u2 = userRepository.save(new User(
                        "mihai",
                        "mihai@mail.com",
                        passwordEncoder.encode("1234"),
                        "USER"
                ));

                User u3 = userRepository.save(new User(
                        "alex",
                        "alex@mail.com",
                        passwordEncoder.encode("1234"),
                        "USER"
                ));

                User u4 = userRepository.save(new User(
                        "ion",
                        "ion@mail.com",
                        passwordEncoder.encode("1234"),
                        "USER"
                ));

                Car c1 = carRepository.save(new Car(
                        "BMW", "320d", 2018, 16500,
                        "Masina bine intretinuta", u1));

                Car c2 = carRepository.save(new Car(
                        "Audi", "A4", 2017, 14900,
                        "Revizii la zi", u2));

                Car c3 = carRepository.save(new Car(
                        "Mercedes", "C220", 2019, 22000,
                        "Full options", u1));

                Car c4 = carRepository.save(new Car(
                        "Volkswagen", "Golf 7", 2016, 11000,
                        "Consum mic", u3));

                Car c5 = carRepository.save(new Car(
                        "Skoda", "Octavia", 2015, 9500,
                        "Foarte fiabila", u4));

                messageRepository.save(new Message(
                        "Salut, masina mai este disponibila?",
                        c1,
                        u2,
                        u1
                ));

                messageRepository.save(new Message(
                        "Accepti schimb?",
                        c3,
                        u3,
                        u1
                ));

                messageRepository.save(new Message(
                        "Se poate negocia pretul?",
                        c2,
                        u4,
                        u2
                ));

                messageRepository.save(new Message(
                        "Cati km are masina?",
                        c4,
                        u1,
                        u3
                ));

                messageRepository.save(new Message(
                        "Mai este valabil anuntul?",
                        c5,
                        u2,
                        u4
                ));
            }
        };
    }
}