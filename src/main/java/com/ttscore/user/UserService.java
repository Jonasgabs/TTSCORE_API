package com.ttscore.user;

import com.ttscore.exception.ResourceNotFoundException;
import com.ttscore.user.dto.UpdateProfileRequest;
import com.ttscore.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getById(String id) {
        return UserResponse.from(findOrThrow(id));
    }

    public List<UserResponse> search(String query) {
        return userRepository.searchByUsername(query)
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse updateProfile(String id, UpdateProfileRequest request) {
        User user = findOrThrow(id);

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new IllegalArgumentException("Username already taken");
            }
            user.setUsername(request.username());
        }

        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }

        return UserResponse.from(userRepository.save(user));
    }

    public List<UserResponse> getRanking() {
        return userRepository.findAllOrderedByWins()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public User findOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public User findOrThrowByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }

    public void incrementWins(String userId) {
        User user = findOrThrow(userId);
        user.setWins(user.getWins() + 1);
        userRepository.save(user);
    }

    public void incrementLosses(String userId) {
        User user = findOrThrow(userId);
        user.setLosses(user.getLosses() + 1);
        userRepository.save(user);
    }
}
