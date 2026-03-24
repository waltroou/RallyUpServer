package com.rally.service;

import com.rally.dto.CreateEventRequest;
import com.rally.dto.EventResponse;
import com.rally.dto.ParticipantResponse;
import com.rally.model.Event;
import com.rally.model.Invite;
import com.rally.model.Participant;
import com.rally.model.User;
import com.rally.repository.EventRepository;
import com.rally.repository.InviteRepository;
import com.rally.repository.ParticipantRepository;
import com.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final NotificationService notificationService;
    private final InviteRepository inviteRepository;

    public EventResponse createEvent(CreateEventRequest request, String userId) {
        User creator = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setSport(request.getSport());
        event.setLocation(request.getLocation());
        event.setDate(request.getDate());
        event.setTime(request.getTime());
        event.setMaxPlayers(request.getMaxPlayers());
        event.setCreatorId(userId);
        event.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);
        event.setCreatedAt(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);

        // Automatically join the creator to the event
        Participant creatorParticipant = new Participant();
        creatorParticipant.setUserId(userId);
        creatorParticipant.setEventId(savedEvent.getId());
        creatorParticipant.setJoinedAt(LocalDateTime.now());
        creatorParticipant.setCheckedIn(false);
        creatorParticipant.setVerificationCode(generateVerificationCode());
        creatorParticipant.setVerified(false);
        participantRepository.save(creatorParticipant);

        return buildEventResponse(savedEvent, creator);
    }

    // Generate a 6-digit verification code
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // Get all events - public events only (for unauthenticated users)
    public List<EventResponse> getAllEvents(String sport, LocalDate date) {
        List<Event> events;
        LocalDate filterDate = date != null ? date : LocalDate.now();

        // Only show public events
        if (sport != null && !sport.isEmpty()) {
            events = eventRepository.findByIsPublicAndSportAndDateGreaterThanEqualOrderByDateAscTimeAsc(true, sport, filterDate);
        } else {
            events = eventRepository.findByIsPublicAndDateGreaterThanEqualOrderByDateAscTimeAsc(true, filterDate);
        }

        return events.stream()
            .map(this::buildEventResponseWithoutParticipants)
            .collect(Collectors.toList());
    }

    // Get all events visible to a specific user (public + private events they created or are invited to)
    public List<EventResponse> getAllEventsForUser(String userId, String sport, LocalDate date) {
        List<Event> events = new ArrayList<>();
        LocalDate filterDate = date != null ? date : LocalDate.now();

        // Get public events
        if (sport != null && !sport.isEmpty()) {
            events.addAll(eventRepository.findByIsPublicAndSportAndDateGreaterThanEqualOrderByDateAscTimeAsc(true, sport, filterDate));
        } else {
            events.addAll(eventRepository.findByIsPublicAndDateGreaterThanEqualOrderByDateAscTimeAsc(true, filterDate));
        }

        // Get private events created by this user
        List<Event> createdPrivateEvents = eventRepository.findByCreatorIdAndDateGreaterThanEqualOrderByDateAscTimeAsc(userId, filterDate)
            .stream()
            .filter(e -> !e.getIsPublic())
            .collect(Collectors.toList());
        events.addAll(createdPrivateEvents);

        // Get private events user is invited to
        List<Invite> userInvites = inviteRepository.findByInviteeIdAndStatus(userId, "PENDING");
        userInvites.addAll(inviteRepository.findByInviteeIdAndStatus(userId, "ACCEPTED"));

        for (Invite invite : userInvites) {
            eventRepository.findById(invite.getEventId()).ifPresent(event -> {
                if (!event.getIsPublic() &&
                    !event.getDate().isBefore(filterDate) &&
                    (sport == null || sport.isEmpty() || event.getSport().equals(sport))) {
                    // Only add if not already in the list
                    if (events.stream().noneMatch(e -> e.getId().equals(event.getId()))) {
                        events.add(event);
                    }
                }
            });
        }

        // Sort by date and time
        events.sort((e1, e2) -> {
            int dateCompare = e1.getDate().compareTo(e2.getDate());
            if (dateCompare != 0) return dateCompare;
            return e1.getTime().compareTo(e2.getTime());
        });

        return events.stream()
            .map(this::buildEventResponseWithoutParticipants)
            .collect(Collectors.toList());
    }

    public EventResponse getEventById(String eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        User creator = userRepository.findById(event.getCreatorId())
            .orElseThrow(() -> new RuntimeException("Creator not found"));

        List<Participant> participants = participantRepository.findByEventId(eventId);

        return buildEventResponseWithParticipants(event, creator, participants);
    }

    public void joinEvent(String eventId, String userId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        // Check if event is private and user is invited
        if (!event.getIsPublic()) {
            // Allow creator to join their own private event
            if (!event.getCreatorId().equals(userId)) {
                // Check if user is invited
                boolean isInvited = inviteRepository.findByEventIdAndInviteeId(eventId, userId)
                    .map(invite -> "PENDING".equals(invite.getStatus()) || "ACCEPTED".equals(invite.getStatus()))
                    .orElse(false);

                if (!isInvited) {
                    throw new RuntimeException("This is a private event. You must be invited to join.");
                }
            }
        }

        if (participantRepository.findByUserIdAndEventId(userId, eventId).isPresent()) {
            throw new RuntimeException("Already joined this event");
        }

        long currentPlayers = participantRepository.findByEventId(eventId).size();
        if (currentPlayers >= event.getMaxPlayers()) {
            throw new RuntimeException("Event is full");
        }

        Participant participant = new Participant();
        participant.setUserId(userId);
        participant.setEventId(eventId);
        participant.setJoinedAt(LocalDateTime.now());
        participant.setCheckedIn(false);
        participant.setVerificationCode(generateVerificationCode());
        participant.setVerified(false);

        participantRepository.save(participant);

        // If user was invited, mark invite as accepted
        if (!event.getIsPublic()) {
            inviteRepository.findByEventIdAndInviteeId(eventId, userId).ifPresent(invite -> {
                if ("PENDING".equals(invite.getStatus())) {
                    invite.setStatus("ACCEPTED");
                    invite.setRespondedAt(LocalDateTime.now());
                    inviteRepository.save(invite);
                }
            });
        }
    }

    public void leaveEvent(String eventId, String userId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        Participant participant = participantRepository.findByUserIdAndEventId(userId, eventId)
            .orElseThrow(() -> new RuntimeException("Not joined to this event"));

        // Check if event is within 24 hours
        LocalDateTime eventDateTime = LocalDateTime.of(event.getDate(), event.getTime());
        LocalDateTime now = LocalDateTime.now();
        long hoursUntilEvent = java.time.Duration.between(now, eventDateTime).toHours();

        if (hoursUntilEvent < 24 && hoursUntilEvent > 0) {
            throw new RuntimeException("Cannot leave event within 24 hours of start time. This will affect your reliability score.");
        }

        // Get the leaving user's name for notifications
        User leavingUser = userRepository.findById(userId).orElse(null);
        String leavingUserName = leavingUser != null ? leavingUser.getFirstName() + " " + leavingUser.getLastName() : "A participant";

        // If the creator is leaving, delete the entire event
        if (event.getCreatorId().equals(userId)) {
            // Get all participants to notify them
            List<Participant> participants = participantRepository.findByEventId(eventId);
            List<String> participantUserIds = participants.stream()
                .map(Participant::getUserId)
                .filter(id -> !id.equals(userId)) // Don't notify the person leaving
                .collect(Collectors.toList());

            // Notify all participants that the event is cancelled
            if (!participantUserIds.isEmpty()) {
                notificationService.notifyParticipantsOfCancellation(eventId, event.getTitle(), participantUserIds);
            }

            // Delete all participants first
            participantRepository.deleteByEventId(eventId);
            // Delete the event
            eventRepository.delete(event);
        } else {
            // Get remaining participants to notify them
            List<Participant> remainingParticipants = participantRepository.findByEventId(eventId);
            List<String> participantUserIds = remainingParticipants.stream()
                .map(Participant::getUserId)
                .filter(id -> !id.equals(userId)) // Don't notify the person leaving
                .collect(Collectors.toList());

            // Notify remaining participants
            if (!participantUserIds.isEmpty()) {
                notificationService.notifyParticipantsOfLeave(eventId, event.getTitle(), leavingUserName, participantUserIds);
            }

            // Just remove this participant
            participantRepository.delete(participant);
        }
    }

    public void checkIn(String eventId, String userId) {
        Participant participant = participantRepository.findByUserIdAndEventId(userId, eventId)
            .orElseThrow(() -> new RuntimeException("Not joined to this event"));

        if (participant.getCheckedIn()) {
            throw new RuntimeException("Already checked in");
        }

        participant.setCheckedIn(true);
        participant.setCheckedInAt(LocalDateTime.now());
        participantRepository.save(participant);
    }

    // Verify attendance by entering another participant's verification code
    public void verifyAttendance(String eventId, String userId, String verificationCode) {
        // Get the current user's participant record
        Participant currentParticipant = participantRepository.findByUserIdAndEventId(userId, eventId)
            .orElseThrow(() -> new RuntimeException("Not joined to this event"));

        // Find the participant with the given verification code
        List<Participant> eventParticipants = participantRepository.findByEventId(eventId);
        Participant codeOwner = eventParticipants.stream()
            .filter(p -> verificationCode.equals(p.getVerificationCode()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Invalid verification code"));

        // Can't verify yourself
        if (codeOwner.getUserId().equals(userId)) {
            throw new RuntimeException("Cannot verify your own attendance");
        }

        // Mark the code owner as verified
        if (!codeOwner.getVerified()) {
            codeOwner.setVerified(true);
            codeOwner.setVerifiedAt(LocalDateTime.now());
            participantRepository.save(codeOwner);
        }
    }

    // Delete event (organizer only)
    public void deleteEvent(String eventId, String userId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        // Verify user is the event creator
        if (!event.getCreatorId().equals(userId)) {
            throw new RuntimeException("Only the event organizer can delete this event");
        }

        // Get all participants to notify them
        List<Participant> participants = participantRepository.findByEventId(eventId);
        List<String> participantUserIds = participants.stream()
            .map(Participant::getUserId)
            .filter(id -> !id.equals(userId)) // Don't notify the organizer
            .collect(Collectors.toList());

        // Notify all participants that the event is cancelled
        if (!participantUserIds.isEmpty()) {
            notificationService.notifyParticipantsOfCancellation(eventId, event.getTitle(), participantUserIds);
        }

        // Delete all participants first
        participantRepository.deleteByEventId(eventId);

        // Delete the event
        eventRepository.delete(event);
    }

    // Kick participant (organizer only)
    public void kickParticipant(String eventId, String organizerId, String participantUserId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        // Verify user is the event creator
        if (!event.getCreatorId().equals(organizerId)) {
            throw new RuntimeException("Only the event organizer can remove participants");
        }

        // Can't kick yourself
        if (organizerId.equals(participantUserId)) {
            throw new RuntimeException("Cannot remove yourself. Use 'Leave Event' or 'Delete Event' instead");
        }

        // Get the participant record
        Participant participant = participantRepository.findByUserIdAndEventId(participantUserId, eventId)
            .orElseThrow(() -> new RuntimeException("User is not a participant of this event"));

        // Get kicked user's name
        User kickedUser = userRepository.findById(participantUserId).orElse(null);
        String kickedUserName = kickedUser != null ?
            kickedUser.getFirstName() + " " + kickedUser.getLastName() : "A participant";

        // Remove the participant
        participantRepository.delete(participant);

        // Notify the kicked user
        String kickMessage = "You have been removed from the event: " + event.getTitle();
        notificationService.createNotification(
            participantUserId,
            eventId,
            event.getTitle(),
            kickMessage,
            "PARTICIPANT_KICKED"
        );

        // Optionally notify other participants
        List<Participant> remainingParticipants = participantRepository.findByEventId(eventId);
        List<String> otherParticipantIds = remainingParticipants.stream()
            .map(Participant::getUserId)
            .filter(id -> !id.equals(organizerId)) // Don't notify the organizer
            .collect(Collectors.toList());

        if (!otherParticipantIds.isEmpty()) {
            String notifyMessage = kickedUserName + " has been removed from the event: " + event.getTitle();
            for (String userId : otherParticipantIds) {
                notificationService.createNotification(
                    userId,
                    eventId,
                    event.getTitle(),
                    notifyMessage,
                    "PARTICIPANT_REMOVED"
                );
            }
        }
    }

    private EventResponse buildEventResponse(Event event, User creator) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setSport(event.getSport());
        response.setLocation(event.getLocation());
        response.setDate(event.getDate());
        response.setTime(event.getTime());
        response.setMaxPlayers(event.getMaxPlayers());
        response.setCreatorId(creator.getId());
        response.setCreatorName(creator.getFirstName() + " " + creator.getLastName());
        response.setIsPublic(event.getIsPublic());
        response.setCreatedAt(event.getCreatedAt());
        response.setCurrentPlayers(0);
        response.setParticipants(List.of());
        return response;
    }

    private EventResponse buildEventResponseWithoutParticipants(Event event) {
        User creator = userRepository.findById(event.getCreatorId()).orElse(null);
        int currentPlayers = participantRepository.findByEventId(event.getId()).size();

        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setSport(event.getSport());
        response.setLocation(event.getLocation());
        response.setDate(event.getDate());
        response.setTime(event.getTime());
        response.setMaxPlayers(event.getMaxPlayers());
        response.setCurrentPlayers(currentPlayers);
        response.setCreatorId(event.getCreatorId());
        response.setCreatorName(creator != null ? creator.getFirstName() + " " + creator.getLastName() : "Unknown");
        response.setIsPublic(event.getIsPublic());
        response.setCreatedAt(event.getCreatedAt());
        response.setParticipants(null);
        return response;
    }

    private EventResponse buildEventResponseWithParticipants(Event event, User creator, List<Participant> participants) {
        List<ParticipantResponse> participantResponses = participants.stream()
            .map(p -> {
                User user = userRepository.findById(p.getUserId()).orElse(null);
                ParticipantResponse pr = new ParticipantResponse();
                pr.setId(p.getId());
                pr.setUserId(p.getUserId());
                pr.setUserName(user != null ? user.getFirstName() + " " + user.getLastName() : "Unknown");
                pr.setEventId(p.getEventId());
                pr.setJoinedAt(p.getJoinedAt());
                pr.setCheckedIn(p.getCheckedIn());
                pr.setCheckedInAt(p.getCheckedInAt());
                pr.setVerificationCode(p.getVerificationCode());
                pr.setVerified(p.getVerified());
                pr.setVerifiedAt(p.getVerifiedAt());
                return pr;
            })
            .collect(Collectors.toList());

        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setSport(event.getSport());
        response.setLocation(event.getLocation());
        response.setDate(event.getDate());
        response.setTime(event.getTime());
        response.setMaxPlayers(event.getMaxPlayers());
        response.setCurrentPlayers(participants.size());
        response.setCreatorId(creator.getId());
        response.setCreatorName(creator.getFirstName() + " " + creator.getLastName());
        response.setIsPublic(event.getIsPublic());
        response.setCreatedAt(event.getCreatedAt());
        response.setParticipants(participantResponses);
        return response;
    }
}

