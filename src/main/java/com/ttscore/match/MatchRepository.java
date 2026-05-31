package com.ttscore.match;

import java.util.List;
import java.util.Optional;

public interface MatchRepository {

    Match save(Match match);

    Optional<Match> findById(String id);

    List<Match> findByUserId(String userId);

    List<Match> findByBothPlayers(String player1Id, String player2Id);
}
