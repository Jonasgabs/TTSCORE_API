package com.ttscore.user;

import com.ttscore.user.dto.UpdateProfileRequest;
import com.ttscore.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(userService.search(q));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<UserResponse>> getRanking() {
        return ResponseEntity.ok(userService.getRanking());
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal User user,
                                                      @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request));
    }
}
