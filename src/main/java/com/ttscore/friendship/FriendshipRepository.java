package com.ttscore.friendship;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository {

    Friendship save(Friendship friendship);

    Optional<Friendship> findById(String id);

    Optional<Friendship> findBetween(String userId1, String userId2);

    List<Friendship> findByUserIdAndStatus(String userId, FriendshipStatus status);
}
