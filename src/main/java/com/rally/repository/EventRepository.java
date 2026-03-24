package com.rally.repository;

import com.rally.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    List<Event> findByDateGreaterThanEqualOrderByDateAscTimeAsc(LocalDate date);

    List<Event> findBySportAndDateGreaterThanEqualOrderByDateAscTimeAsc(String sport, LocalDate date);

    List<Event> findByCreatorIdOrderByDateDescTimeDesc(String userId);

    // Public events only
    List<Event> findByIsPublicAndDateGreaterThanEqualOrderByDateAscTimeAsc(Boolean isPublic, LocalDate date);

    List<Event> findByIsPublicAndSportAndDateGreaterThanEqualOrderByDateAscTimeAsc(Boolean isPublic, String sport, LocalDate date);

    // Events by creator
    List<Event> findByCreatorIdAndDateGreaterThanEqualOrderByDateAscTimeAsc(String creatorId, LocalDate date);
}

