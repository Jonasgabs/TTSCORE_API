package com.ttscore.user.dto;

import com.ttscore.user.User;

import java.util.Date;

public record UserResponse(
        String id,
        String username,
        String email,
        String avatarUrl,
        Date createdAt,
        int wins,
        int losses
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getCreatedAt(),
                user.getWins(),
                user.getLosses()
        );
    }
}
