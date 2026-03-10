package com.rally.service;

import com.rally.model.Notification;
import com.rally.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createNotification(String userId, String eventId, String eventTitle, String message, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setEventId(eventId);
        notification.setEventTitle(eventTitle);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false);
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }

    public void markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    public void notifyParticipantsOfLeave(String eventId, String eventTitle, String leavingUserName, List<String> participantUserIds) {
        String message = leavingUserName + " has left the event: " + eventTitle;
        
        for (String userId : participantUserIds) {
            createNotification(userId, eventId, eventTitle, message, "PARTICIPANT_LEFT");
        }
    }

    public void notifyParticipantsOfCancellation(String eventId, String eventTitle, List<String> participantUserIds) {
        String message = "Event has been cancelled by the organizer: " + eventTitle;
        
        for (String userId : participantUserIds) {
            createNotification(userId, eventId, eventTitle, message, "EVENT_CANCELLED");
        }
    }
}
