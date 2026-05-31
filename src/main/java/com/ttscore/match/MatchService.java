package com.ttscore.match;

import com.ttscore.exception.ResourceNotFoundException;
import com.ttscore.match.dto.MatchRequest;
import com.ttscore.match.dto.MatchResponse;
import com.ttscore.user.UserService;
import com.ttscore.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserService userService;

    public MatchResponse create(String currentUserId, MatchRequest request) {
        String opponentId = userService.findOrThrowByUsername(request.opponentUsername()).getId();
        String winnerId = request.player1Score() > request.player2Score()
                ? currentUserId : opponentId;
        String loserId = winnerId.equals(currentUserId) ? opponentId : currentUserId;

        Match match = Match.builder()
                .player1Id(currentUserId)
                .player2Id(opponentId)
                .player1Score(request.player1Score())
                .player2Score(request.player2Score())
                .winnerId(winnerId)
                .build();

        Match saved = matchRepository.save(match);
        userService.incrementWins(winnerId);
        userService.incrementLosses(loserId);
        return toResponse(saved);
    }

    public List<MatchResponse> getByUser(String userId) {
        return matchRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<MatchResponse> getByUsername(String username) {
        String userId = userService.findOrThrowByUsername(username).getId();
        return getByUser(userId);
    }

    public List<MatchResponse> getByBothPlayers(String userId1, String userId2) {
        return matchRepository.findByBothPlayers(userId1, userId2)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<MatchResponse> getByBothPlayersViaUsername(String userId1, String opponentUsername) {
        String opponentId = userService.findOrThrowByUsername(opponentUsername).getId();
        return getByBothPlayers(userId1, opponentId);
    }

    public MatchResponse getById(String id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match", id));
        return toResponse(match);
    }

    private MatchResponse toResponse(Match match) {
        UserResponse p1 = userService.getById(match.getPlayer1Id());
        UserResponse p2 = userService.getById(match.getPlayer2Id());
        return MatchResponse.from(match, p1, p2);
    }
}
