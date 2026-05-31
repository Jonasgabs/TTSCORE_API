package com.ttscore.friendship.dto;

import com.ttscore.friendship.Friendship;
import com.ttscore.friendship.FriendshipStatus;
import com.ttscore.user.dto.UserResponse;

import java.util.Date;

public record FriendshipResponse(
        String id,
        UserResponse requester,
        UserResponse addressee,
        FriendshipStatus status,
        Date createdAt
) {
    public static FriendshipResponse from(Friendship f, UserResponse requester, UserResponse addressee) {
        return new FriendshipResponse(f.getId(), requester, addressee, f.getStatus(), f.getCreatedAt());
    }
}
