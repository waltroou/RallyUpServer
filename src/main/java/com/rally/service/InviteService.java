package com.rally.service;

import com.rally.model.Event;
import com.rally.model.Invite;
import com.rally.model.User;
import com.rally.repository.EventRepository;
import com.rally.repository.InviteRepository;
import com.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Invite createInvite(String eventId, String inviterId, String inviteeEmail) {
        // Verify event exists and is private
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getIsPublic()) {
            throw new RuntimeException("Cannot send invites to public events");
        }

        // Verify inviter is the event creator
        if (!event.getCreatorId().equals(inviterId)) {
            throw new RuntimeException("Only the event organizer can send invites");
        }

        // Find user by email
        User invitee = userRepository.findByEmail(inviteeEmail)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + inviteeEmail));

        // Check if invite already exists
        if (inviteRepository.findByEventIdAndInviteeId(eventId, invitee.getId()).isPresent()) {
            throw new RuntimeException("Invite already sent to this user");
        }

        // Create invite
        Invite invite = new Invite();
        invite.setEventId(eventId);
        invite.setInviterId(inviterId);
        invite.setInviteeId(invitee.getId());
        invite.setInviteeEmail(inviteeEmail);
        invite.setStatus("PENDING");
        invite.setCreatedAt(LocalDateTime.now());

        Invite savedInvite = inviteRepository.save(invite);

        // Send notification to invitee
        String message = "You have been invited to join: " + event.getTitle();
        notificationService.createNotification(
            invitee.getId(),
            eventId,
            event.getTitle(),
            message,
            "EVENT_INVITE"
        );

        return savedInvite;
    }

    public List<Invite> getUserInvites(String userId) {
        return inviteRepository.findByInviteeIdAndStatus(userId, "PENDING");
    }

    public void acceptInvite(String inviteId, String userId) {
        Invite invite = inviteRepository.findById(inviteId)
            .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (!invite.getInviteeId().equals(userId)) {
            throw new RuntimeException("Not authorized to accept this invite");
        }

        if (!"PENDING".equals(invite.getStatus())) {
            throw new RuntimeException("Invite has already been responded to");
        }

        invite.setStatus("ACCEPTED");
        invite.setRespondedAt(LocalDateTime.now());
        inviteRepository.save(invite);
    }

    public void declineInvite(String inviteId, String userId) {
        Invite invite = inviteRepository.findById(inviteId)
            .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (!invite.getInviteeId().equals(userId)) {
            throw new RuntimeException("Not authorized to decline this invite");
        }

        if (!"PENDING".equals(invite.getStatus())) {
            throw new RuntimeException("Invite has already been responded to");
        }

        invite.setStatus("DECLINED");
        invite.setRespondedAt(LocalDateTime.now());
        inviteRepository.save(invite);
    }

    public boolean isUserInvited(String eventId, String userId) {
        return inviteRepository.findByEventIdAndInviteeId(eventId, userId)
            .map(invite -> "PENDING".equals(invite.getStatus()) || "ACCEPTED".equals(invite.getStatus()))
            .orElse(false);
    }

    public List<Invite> getEventInvites(String eventId) {
        return inviteRepository.findByEventId(eventId);
    }
}

