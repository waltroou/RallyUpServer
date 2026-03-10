package com.rally.service;

import com.rally.dto.EventResponse;
import com.rally.dto.UserResponse;
import com.rally.dto.UserStatsResponse;
import com.rally.model.Event;
import com.rally.model.Participant;
import com.rally.model.User;
import com.rally.repository.EventRepository;
import com.rally.repository.ParticipantRepository;
import com.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.from(user);
    }

    public UserStatsResponse getUserStats(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        long eventsJoined = participantRepository.countByUserId(userId);
        long eventsCheckedIn = participantRepository.countByUserIdAndCheckedIn(userId, true);

        return UserStatsResponse.from(userId, eventsJoined, eventsCheckedIn);
    }

    public List<EventResponse> getUserEvents(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        List<Participant> participants = participantRepository.findByUserId(userId);

        return participants.stream()
            .map(p -> {
                Event event = eventRepository.findById(p.getEventId()).orElse(null);
                if (event == null) return null;

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
            })
            .filter(e -> e != null)
            .collect(Collectors.toList());
    }
}

