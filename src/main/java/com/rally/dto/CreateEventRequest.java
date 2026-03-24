package com.rally.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateEventRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Sport is required")
    private String sport;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    @NotNull(message = "Time is required")
    private LocalTime time;
    
    @NotNull(message = "Max players is required")
    @Min(value = 2, message = "Max players must be at least 2")
    private Integer maxPlayers;

    private Boolean isPublic = true; // Default to public events
}

