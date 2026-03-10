package com.rally.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

@Document(collection = "participants")
@CompoundIndex(name = "user_event_idx", def = "{'userId': 1, 'eventId': 1}", unique = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    @Id
    private String id;

    private String userId;

    private String eventId;

    @CreatedDate
    private LocalDateTime joinedAt;

    private Boolean checkedIn = false;

    private LocalDateTime checkedInAt;

    // Verification code for this participant (shown to them)
    private String verificationCode;

    // Whether this participant has been verified by others
    private Boolean verified = false;

    // Timestamp when verification was completed
    private LocalDateTime verifiedAt;
}

