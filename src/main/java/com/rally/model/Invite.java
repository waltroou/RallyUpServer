package com.rally.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

@Document(collection = "invites")
@CompoundIndex(name = "event_invitee_idx", def = "{'eventId': 1, 'inviteeId': 1}", unique = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invite {

    @Id
    private String id;

    private String eventId;

    private String inviterId; // User who sent the invite (organizer)

    private String inviteeId; // User who is being invited

    private String inviteeEmail; // Email of the person being invited

    private String status; // PENDING, ACCEPTED, DECLINED

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime respondedAt;
}

