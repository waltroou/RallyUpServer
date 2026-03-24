package com.rally.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    private String id;

    private String title;

    private String sport;

    private String location;

    private LocalDate date;

    private LocalTime time;

    private Integer maxPlayers;

    private String creatorId;

    private Boolean isPublic = true; // Default to public events

    @CreatedDate
    private LocalDateTime createdAt;

    public int getCurrentPlayers() {
        // This will be calculated from Participant collection
        return 0;
    }
}

