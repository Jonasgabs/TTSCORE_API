package com.ttscore.match;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    private String id;
    private String player1Id;
    private String player2Id;
    private int player1Score;
    private int player2Score;
    private String winnerId;
    private Date playedAt;
}
