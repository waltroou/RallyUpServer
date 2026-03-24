package com.rally.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InviteResponse {
    
    private String id;
    private String eventId;
    private String inviterId;
    private String inviteeId;
    private String inviteeEmail;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}

