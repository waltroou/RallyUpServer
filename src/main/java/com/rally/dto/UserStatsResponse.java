package com.rally.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    private String userId;
    private Long eventsJoined;
    private Long eventsCheckedIn;
    private Double reliabilityScore;

    public static UserStatsResponse from(String userId, long eventsJoined, long eventsCheckedIn) {
        double score = eventsJoined > 0 ? (eventsCheckedIn * 100.0 / eventsJoined) : 0.0;
        return new UserStatsResponse(userId, eventsJoined, eventsCheckedIn, Math.round(score * 100.0) / 100.0);
    }
}

