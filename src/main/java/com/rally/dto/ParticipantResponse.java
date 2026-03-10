package com.rally.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {
    private String id;
    private String userId;
    private String userName;
    private String eventId;
    private LocalDateTime joinedAt;
    private Boolean checkedIn;
    private LocalDateTime checkedInAt;
    private String verificationCode;
    private Boolean verified;
    private LocalDateTime verifiedAt;
}

