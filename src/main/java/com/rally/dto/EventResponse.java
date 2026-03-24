package com.rally.dto;

import com.rally.model.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private String id;
    private String title;
    private String sport;
    private String location;
    private LocalDate date;
    private LocalTime time;
    private Integer maxPlayers;
    private Integer currentPlayers;
    private String creatorId;
    private String creatorName;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private List<ParticipantResponse> participants;
}

