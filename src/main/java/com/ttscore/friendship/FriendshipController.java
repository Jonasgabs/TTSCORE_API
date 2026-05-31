package com.ttscore.friendship;

import com.ttscore.friendship.dto.FriendshipResponse;
import com.ttscore.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendships")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/request/{addresseeId}")
    public ResponseEntity<FriendshipResponse> sendRequest(@AuthenticationPrincipal User user,
                                                          @PathVariable String addresseeId,
                                                          @RequestParam(defaultValue = "false") boolean forceAccept) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(friendshipService.sendRequest(user.getId(), addresseeId, forceAccept));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<FriendshipResponse> accept(@AuthenticationPrincipal User user,
                                                     @PathVariable String id) {
        return ResponseEntity.ok(friendshipService.accept(id, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user,
                                       @PathVariable String id) {
        friendshipService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FriendshipResponse>> getFriends(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(friendshipService.getFriends(user.getId()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FriendshipResponse>> getPending(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(friendshipService.getPendingRequests(user.getId()));
    }
}
