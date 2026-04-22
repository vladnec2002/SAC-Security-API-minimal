package com.example.backend_service.model;

public class MessageRequest {
    private Long carId;
    private String content;

    public MessageRequest() {
    }

    public Long getCarId() {
        return carId;
    }

    public String getContent() {
        return content;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}