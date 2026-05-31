package com.ttscore.match.dto;

import com.ttscore.match.Match;
import com.ttscore.user.dto.UserResponse;

import java.util.Date;

public record MatchResponse(
        String id,
        UserResponse player1,
        UserResponse player2,
        int player1Score,
        int player2Score,
        String winnerId,
        Date playedAt
) {
    public static MatchResponse from(Match match, UserResponse player1, UserResponse player2) {
        return new MatchResponse(
                match.getId(),
                player1,
                player2,
                match.getPlayer1Score(),
                match.getPlayer2Score(),
                match.getWinnerId(),
                match.getPlayedAt()
        );
    }
}
