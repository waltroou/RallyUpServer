package com.rally.service;

import com.rally.dto.CreateEventRequest;
import com.rally.dto.EventResponse;
import com.rally.dto.ParticipantResponse;
import com.rally.model.Event;
import com.rally.model.Participant;
import com.rally.model.User;
import com.rally.repository.EventRepository;
import com.rally.repository.ParticipantRepository;
import com.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public List<EventResponse> getAllEvents(String sport, LocalDate date) {
        List<Event> events;
        LocalDate filterDate = date != null ? date : LocalDate.now();

        if (sport != null && !sport.isEmpty()) {
            events = eventRepository.findBySportAndDateGreaterThanEqualOrderByDateAscTimeAsc(sport, filterDate);
        } else {
            events = eventRepository.findByDateGreaterThanEqualOrderByDateAscTimeAsc(filterDate);
        }

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
        response.setCreatedAt(event.getCreatedAt());
        response.setParticipants(participantResponses);
        return response;
    }
}

