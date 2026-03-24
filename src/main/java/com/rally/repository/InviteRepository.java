package com.rally.repository;

import com.rally.model.Invite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InviteRepository extends MongoRepository<Invite, String> {
    
    List<Invite> findByEventId(String eventId);
    
    List<Invite> findByInviteeId(String inviteeId);
    
    List<Invite> findByInviteeIdAndStatus(String inviteeId, String status);
    
    Optional<Invite> findByEventIdAndInviteeId(String eventId, String inviteeId);
    
    void deleteByEventId(String eventId);
    
    long countByEventIdAndStatus(String eventId, String status);
}

