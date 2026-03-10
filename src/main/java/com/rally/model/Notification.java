package com.rally.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;

    private String userId;

    private String eventId;

    private String eventTitle;

    private String message;

    private String type; // "PARTICIPANT_LEFT", "EVENT_CANCELLED", "EVENT_REMINDER", etc.

    private Boolean read = false;

    private LocalDateTime createdAt;
}

