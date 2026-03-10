package com.rally.config;

import com.rally.model.Event;
import com.rally.model.Participant;
import com.rally.model.User;
import com.rally.repository.EventRepository;
import com.rally.repository.ParticipantRepository;
import com.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Clear existing data
        participantRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Create demo users
        User user1 = new User();
        user1.setEmail("john@rally.com");
        user1.setPassword(passwordEncoder.encode("password123"));
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setCreatedAt(LocalDateTime.now());
        user1 = userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("jane@rally.com");
        user2.setPassword(passwordEncoder.encode("password123"));
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setCreatedAt(LocalDateTime.now());
        user2 = userRepository.save(user2);

        // Create demo events
        Event basketball = new Event();
        basketball.setTitle("Pickup Basketball");
        basketball.setSport("Basketball");
        basketball.setLocation("Downtown Community Center");
        basketball.setDate(LocalDate.now().plusDays(2));
        basketball.setTime(LocalTime.of(18, 0));
        basketball.setMaxPlayers(10);
        basketball.setCreatorId(user1.getId());
        basketball.setCreatedAt(LocalDateTime.now());
        basketball = eventRepository.save(basketball);

        Event soccer = new Event();
        soccer.setTitle("Weekend Soccer Match");
        soccer.setSport("Soccer");
        soccer.setLocation("Central Park Field 3");
        soccer.setDate(LocalDate.now().plusDays(5));
        soccer.setTime(LocalTime.of(10, 0));
        soccer.setMaxPlayers(22);
        soccer.setCreatorId(user2.getId());
        soccer.setCreatedAt(LocalDateTime.now());
        soccer = eventRepository.save(soccer);

        Event pickleball = new Event();
        pickleball.setTitle("Morning Pickleball");
        pickleball.setSport("Pickleball");
        pickleball.setLocation("Riverside Courts");
        pickleball.setDate(LocalDate.now().plusDays(1));
        pickleball.setTime(LocalTime.of(8, 30));
        pickleball.setMaxPlayers(4);
        pickleball.setCreatorId(user1.getId());
        pickleball.setCreatedAt(LocalDateTime.now());
        pickleball = eventRepository.save(pickleball);

        // Add some participants
        Participant p1 = new Participant();
        p1.setUserId(user2.getId());
        p1.setEventId(basketball.getId());
        p1.setJoinedAt(LocalDateTime.now());
        p1.setCheckedIn(true);
        p1.setCheckedInAt(LocalDateTime.now());
        participantRepository.save(p1);

        Participant p2 = new Participant();
        p2.setUserId(user1.getId());
        p2.setEventId(soccer.getId());
        p2.setJoinedAt(LocalDateTime.now());
        p2.setCheckedIn(false);
        participantRepository.save(p2);

        System.out.println("===========================================");
        System.out.println("Demo data initialized successfully!");
        System.out.println("===========================================");
        System.out.println("Demo users:");
        System.out.println("  - john@rally.com / password123");
        System.out.println("  - jane@rally.com / password123");
        System.out.println("===========================================");
    }
}

