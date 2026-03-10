package com.rally.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String userId;
    private String eventId;
    private String eventTitle;
    private String message;
    private String type;
    private Boolean read;
    private LocalDateTime createdAt;
}
