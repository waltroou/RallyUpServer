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
}

