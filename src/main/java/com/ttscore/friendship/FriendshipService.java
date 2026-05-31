package com.ttscore.friendship;

import com.ttscore.exception.BusinessException;
import com.ttscore.exception.ResourceNotFoundException;
import com.ttscore.friendship.dto.FriendshipResponse;
import com.ttscore.user.UserService;
import com.ttscore.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserService userService;

    public FriendshipResponse sendRequest(String requesterId, String addresseeId, boolean forceAccept) {
        if (requesterId.equals(addresseeId)) {
            throw new BusinessException("Cannot add yourself as a friend");
        }
        userService.getById(addresseeId);

        friendshipRepository.findBetween(requesterId, addresseeId).ifPresent(f -> {
            throw new BusinessException("Friendship already exists with status: " + f.getStatus());
        });

        FriendshipStatus status = forceAccept ? FriendshipStatus.ACCEPTED : FriendshipStatus.PENDING;

        Friendship friendship = Friendship.builder()
                .requesterId(requesterId)
                .addresseeId(addresseeId)
                .status(status)
                .build();

        return toResponse(friendshipRepository.save(friendship));
    }

    public FriendshipResponse accept(String friendshipId, String currentUserId) {
        Friendship friendship = findOrThrow(friendshipId);

        if (!friendship.getAddresseeId().equals(currentUserId)) {
            throw new BusinessException("Only the addressee can accept this request");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BusinessException("Friendship is not in PENDING state");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        return toResponse(friendshipRepository.save(friendship));
    }

    public void delete(String friendshipId, String currentUserId) {
        Friendship friendship = findOrThrow(friendshipId);

        if (!friendship.getRequesterId().equals(currentUserId) && !friendship.getAddresseeId().equals(currentUserId)) {
            throw new BusinessException("You are not part of this friendship");
        }

        friendship.setStatus(FriendshipStatus.BLOCKED);
        friendshipRepository.save(friendship);
    }

    public List<FriendshipResponse> getFriends(String userId) {
        return friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED)
                .stream().map(this::toResponse).toList();
    }

    public List<FriendshipResponse> getPendingRequests(String userId) {
        return friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    private Friendship findOrThrow(String id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship", id));
    }

    private FriendshipResponse toResponse(Friendship f) {
        UserResponse requester = userService.getById(f.getRequesterId());
        UserResponse addressee = userService.getById(f.getAddresseeId());
        return FriendshipResponse.from(f, requester, addressee);
    }
}
