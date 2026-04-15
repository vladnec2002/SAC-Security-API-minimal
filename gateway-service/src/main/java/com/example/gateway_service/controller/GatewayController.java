package com.example.gateway_service.controller;

import com.example.gateway_service.security.InputSanitizer;
import com.example.gateway_service.security.JwtUtil;
import com.example.gateway_service.security.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    private final JwtUtil jwtUtil;
    private final RateLimiterService rateLimiterService;
    private final InputSanitizer inputSanitizer;
    private final RestTemplate restTemplate;
    private final String backendBaseUrl;

    public GatewayController(
            JwtUtil jwtUtil,
            RateLimiterService rateLimiterService,
            InputSanitizer inputSanitizer,
            RestTemplate restTemplate,
            @Value("${backend.base-url}") String backendBaseUrl
    ) {
        this.jwtUtil = jwtUtil;
        this.rateLimiterService = rateLimiterService;
        this.inputSanitizer = inputSanitizer;
        this.restTemplate = restTemplate;
        this.backendBaseUrl = backendBaseUrl;
    }

    @GetMapping("/public/cars")
    public ResponseEntity<?> getCars(HttpServletRequest request) {
        if (!checkRateLimit(request, false)) {
            return tooManyRequests();
        }

        return restTemplate.getForEntity(backendBaseUrl + "/api/public/cars", String.class);
    }

    @GetMapping("/public/cars/{id}")
    public ResponseEntity<?> getCarById(@PathVariable Long id, HttpServletRequest request) {
        if (!checkRateLimit(request, false)) {
            return tooManyRequests();
        }

        return restTemplate.getForEntity(backendBaseUrl + "/api/public/cars/" + id, String.class);
    }

    @GetMapping("/private/profile")
    public ResponseEntity<?> getProfile(
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (!checkRateLimit(request, true)) {
            return tooManyRequests();
        }

        if (!checkJwt(authHeader)) {
            return unauthorized();
        }

        return restTemplate.getForEntity(backendBaseUrl + "/api/private/profile", String.class);
    }

    @PostMapping("/private/cars")
    public ResponseEntity<?> addCar(
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        if (!checkRateLimit(request, true)) {
            return tooManyRequests();
        }

        if (!checkJwt(authHeader)) {
            return unauthorized();
        }

        if (!inputSanitizer.isSafe(body)) {
            return badRequest();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

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
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        if (!checkRateLimit(request, true)) {
            return tooManyRequests();
        }

        if (!checkJwt(authHeader)) {
            return unauthorized();
        }

        if (!inputSanitizer.isSafe(body)) {
            return badRequest();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
                backendBaseUrl + "/api/private/messages",
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    private boolean checkRateLimit(HttpServletRequest request, boolean isPrivate) {
        return rateLimiterService.allowRequest(getClientIp(request), isPrivate);
    }

    private boolean checkJwt(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);
        return jwtUtil.isValid(token);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Missing or invalid JWT"));
    }

    private ResponseEntity<Map<String, String>> tooManyRequests() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "Rate limit exceeded"));
    }

    private ResponseEntity<Map<String, String>> badRequest() {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Unsafe input detected"));
    }
}