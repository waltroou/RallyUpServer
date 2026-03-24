package com.rally.controller;

import com.rally.dto.InviteRequest;
import com.rally.dto.InviteResponse;
import com.rally.model.Invite;
import com.rally.service.AuthService;
import com.rally.service.InviteService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;
    private final AuthService authService;

    @PostMapping("/event/{eventId}")
    public ResponseEntity<InviteResponse> createInvite(
            @PathVariable String eventId,
            @Valid @RequestBody InviteRequest request,
            HttpSession session) {
        try {
            String userId = authService.getCurrentUserId(session);
            Invite invite = inviteService.createInvite(eventId, userId, request.getInviteeEmail());
            return ResponseEntity.ok(toResponse(invite));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<InviteResponse>> getUserInvites(HttpSession session) {
        try {
            String userId = authService.getCurrentUserId(session);
            List<Invite> invites = inviteService.getUserInvites(userId);
            List<InviteResponse> responses = invites.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/{inviteId}/accept")
    public ResponseEntity<Void> acceptInvite(
            @PathVariable String inviteId,
            HttpSession session) {
        try {
            String userId = authService.getCurrentUserId(session);
            inviteService.acceptInvite(inviteId, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{inviteId}/decline")
    public ResponseEntity<Void> declineInvite(
            @PathVariable String inviteId,
            HttpSession session) {
        try {
            String userId = authService.getCurrentUserId(session);
            inviteService.declineInvite(inviteId, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<InviteResponse>> getEventInvites(
            @PathVariable String eventId,
            HttpSession session) {
        try {
            authService.getCurrentUserId(session); // Verify authenticated
            List<Invite> invites = inviteService.getEventInvites(eventId);
            List<InviteResponse> responses = invites.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }

    private InviteResponse toResponse(Invite invite) {
        InviteResponse response = new InviteResponse();
        response.setId(invite.getId());
        response.setEventId(invite.getEventId());
        response.setInviterId(invite.getInviterId());
        response.setInviteeId(invite.getInviteeId());
        response.setInviteeEmail(invite.getInviteeEmail());
        response.setStatus(invite.getStatus());
        response.setCreatedAt(invite.getCreatedAt());
        response.setRespondedAt(invite.getRespondedAt());
        return response;
    }
}

