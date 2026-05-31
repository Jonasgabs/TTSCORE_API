package com.ttscore.match;

import com.ttscore.match.dto.MatchRequest;
import com.ttscore.match.dto.MatchResponse;
import com.ttscore.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    public ResponseEntity<MatchResponse> create(@AuthenticationPrincipal User user,
                                                @Valid @RequestBody MatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matchService.create(user.getId(), request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<MatchResponse>> myMatches(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(matchService.getByUser(user.getId()));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<MatchResponse>> byUser(@PathVariable String username) {
        return ResponseEntity.ok(matchService.getByUsername(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(matchService.getById(id));
    }

    @GetMapping("/versus/{opponentUsername}")
    public ResponseEntity<List<MatchResponse>> versus(@AuthenticationPrincipal User user,
                                                      @PathVariable String opponentUsername) {
        return ResponseEntity.ok(matchService.getByBothPlayersViaUsername(user.getId(), opponentUsername));
    }
}
