package com.example.backend_service.model;

public class MessageRequest {
    private Long carId;
    private Long senderId;
    private String content;

    public MessageRequest() {
    }

    public Long getCarId() {
        return carId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}