package com.rally.repository;

import com.rally.model.Participant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends MongoRepository<Participant, String> {

    Optional<Participant> findByUserIdAndEventId(String userId, String eventId);

    List<Participant> findByUserId(String userId);

    List<Participant> findByEventId(String eventId);

    long countByUserId(String userId);

    @Query(value = "{ 'userId': ?0, 'checkedIn': ?1 }", count = true)
    long countByUserIdAndCheckedIn(String userId, Boolean checkedIn);

    void deleteByUserIdAndEventId(String userId, String eventId);

    void deleteByEventId(String eventId);
}
