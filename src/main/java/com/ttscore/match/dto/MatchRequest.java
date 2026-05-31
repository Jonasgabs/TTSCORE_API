package com.ttscore.match.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MatchRequest(
        @NotNull String opponentUsername,
        @NotNull @Min(0) Integer player1Score,
        @NotNull @Min(0) Integer player2Score
) {}
