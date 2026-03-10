package com.rally.controller;

import com.rally.dto.CreateEventRequest;
import com.rally.dto.EventResponse;
import com.rally.dto.VerifyAttendanceRequest;
import com.rally.service.AuthService;
import com.rally.service.EventService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            HttpSession session) {
        try {
            String userId = authService.getCurrentUserId(session);
            EventResponse response = eventService.createEvent(request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents(
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<EventResponse> events = eventService.getAllEvents(sport, date);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable String id) {
        try {
            EventResponse response = eventService.getEventById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Void> joinEvent(@PathVariable String id, HttpSession session) {
        try {
            String userId = authService.getCurrentUserId(session);
            eventService.joinEvent(id, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveEvent(@PathVariable String id, HttpSession session) {
        try {
            String userId = authService.getCurrentUserId(session);
            eventService.leaveEvent(id, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/checkin")
    public ResponseEntity<Void> checkIn(@PathVariable String id, HttpSession session) {
        try {
            String userId = authService.getCurrentUserId(session);
            eventService.checkIn(id, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<Void> verifyAttendance(
            @PathVariable String id,
            @Valid @RequestBody VerifyAttendanceRequest request,
            HttpSession session) {
        try {
            String userId = authService.getCurrentUserId(session);
            eventService.verifyAttendance(id, userId, request.getVerificationCode());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

