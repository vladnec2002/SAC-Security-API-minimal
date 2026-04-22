package com.example.gateway_service.controller;

import com.example.gateway_service.security.InputSanitizer;
import com.example.gateway_service.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    private final JwtUtil jwtUtil;
    private final InputSanitizer inputSanitizer;
    private final RestTemplate restTemplate;
    private final String backendBaseUrl;

    public GatewayController(
            JwtUtil jwtUtil,
            InputSanitizer inputSanitizer,
            RestTemplate restTemplate,
            @Value("${backend.base-url}") String backendBaseUrl
    ) {
        this.jwtUtil = jwtUtil;
        this.inputSanitizer = inputSanitizer;
        this.restTemplate = restTemplate;
        this.backendBaseUrl = backendBaseUrl;
    }

    @GetMapping("/public/cars")
    public ResponseEntity<?> getCars() {
        return restTemplate.getForEntity(backendBaseUrl + "/api/public/cars", String.class);
    }

    @GetMapping("/public/cars/{id}")
    public ResponseEntity<?> getCarById(@PathVariable Long id) {
        return restTemplate.getForEntity(backendBaseUrl + "/api/public/cars/" + id, String.class);
    }

    @GetMapping("/private/profile")
    public ResponseEntity<?> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (!checkJwt(authHeader)) {
            return unauthorized();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(extractToken(authHeader));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                backendBaseUrl + "/api/private/profile",
                HttpMethod.GET,
                entity,
                String.class
        );
    }

    @PostMapping("/private/cars")
    public ResponseEntity<?> addCar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        if (!checkJwt(authHeader)) {
            return unauthorized();
        }

        if (!inputSanitizer.isSafe(body)) {
            return badRequest();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(extractToken(authHeader));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
                backendBaseUrl + "/api/private/cars",
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    @PostMapping("/private/messages")
    public ResponseEntity<?> sendMessage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        if (!checkJwt(authHeader)) {
            return unauthorized();
        }

        if (!inputSanitizer.isSafe(body)) {
            return badRequest();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(extractToken(authHeader));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
                backendBaseUrl + "/api/private/messages",
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    private boolean checkJwt(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = extractToken(authHeader);
        return jwtUtil.isValid(token);
    }

    private String extractToken(String authHeader) {
        return authHeader.substring(7);
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Missing or invalid JWT"));
    }

    private ResponseEntity<Map<String, String>> badRequest() {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Unsafe input detected"));
    }
}