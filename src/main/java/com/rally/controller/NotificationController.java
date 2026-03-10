package com.rally.controller;

import com.rally.dto.NotificationResponse;
import com.rally.model.Notification;
import com.rally.service.AuthService;
import com.rally.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(HttpSession session) {
        try {
            String userId = authService.getCurrentUserId(session);
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            
            List<NotificationResponse> responses = notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(HttpSession session) {
        try {
            String userId = authService.getCurrentUserId(session);
            List<Notification> notifications = notificationService.getUnreadNotifications(userId);
            
            List<NotificationResponse> responses = notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(HttpSession session) {
        try {
            String userId = authService.getCurrentUserId(session);
            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(count);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String id, HttpSession session) {
        try {
            authService.getCurrentUserId(session); // Verify user is authenticated
            notificationService.markAsRead(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(HttpSession session) {
        try {
            String userId = authService.getCurrentUserId(session);
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setEventId(notification.getEventId());
        response.setEventTitle(notification.getEventTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setRead(notification.getRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}
