package com.ttscore.friendship;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class FirestoreFriendshipRepository implements FriendshipRepository {

    private static final String COLLECTION = "friendships";
    private final Firestore firestore;

    @Override
    public Friendship save(Friendship friendship) {
        try {
            if (friendship.getId() == null) {
                friendship.setId(UUID.randomUUID().toString());
                friendship.setCreatedAt(new Date());
            }
            firestore.collection(COLLECTION).document(friendship.getId()).set(toMap(friendship)).get();
            return friendship;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving friendship", e);
        }
    }

    @Override
    public Optional<Friendship> findById(String id) {
        try {
            var doc = firestore.collection(COLLECTION).document(id).get().get();
            return doc.exists() ? Optional.of(fromDoc(id, doc.getData())) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding friendship", e);
        }
    }

    @Override
    public Optional<Friendship> findBetween(String userId1, String userId2) {
        try {
            var snap1 = firestore.collection(COLLECTION)
                    .whereEqualTo("requesterId", userId1)
                    .whereEqualTo("addresseeId", userId2)
                    .limit(1).get().get();
            if (!snap1.isEmpty()) return Optional.of(fromDoc(snap1.getDocuments().get(0).getId(), snap1.getDocuments().get(0).getData()));

            var snap2 = firestore.collection(COLLECTION)
                    .whereEqualTo("requesterId", userId2)
                    .whereEqualTo("addresseeId", userId1)
                    .limit(1).get().get();
            if (!snap2.isEmpty()) return Optional.of(fromDoc(snap2.getDocuments().get(0).getId(), snap2.getDocuments().get(0).getData()));

            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding friendship", e);
        }
    }

    @Override
    public List<Friendship> findByUserIdAndStatus(String userId, FriendshipStatus status) {
        try {
            var snap1 = firestore.collection(COLLECTION)
                    .whereEqualTo("requesterId", userId)
                    .whereEqualTo("status", status.name())
                    .get().get();
            var snap2 = firestore.collection(COLLECTION)
                    .whereEqualTo("addresseeId", userId)
                    .whereEqualTo("status", status.name())
                    .get().get();

            List<Friendship> list = new ArrayList<>();
            snap1.getDocuments().forEach(d -> list.add(fromDoc(d.getId(), d.getData())));
            snap2.getDocuments().forEach(d -> list.add(fromDoc(d.getId(), d.getData())));
            return list;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error listing friendships", e);
        }
    }

    private Map<String, Object> toMap(Friendship f) {
        Map<String, Object> map = new HashMap<>();
        map.put("requesterId", f.getRequesterId());
        map.put("addresseeId", f.getAddresseeId());
        map.put("status", f.getStatus().name());
        map.put("createdAt", f.getCreatedAt());
        return map;
    }

    private Friendship fromDoc(String id, Map<String, Object> data) {
        return Friendship.builder()
                .id(id)
                .requesterId((String) data.get("requesterId"))
                .addresseeId((String) data.get("addresseeId"))
                .status(FriendshipStatus.valueOf((String) data.get("status")))
                .createdAt(toDate(data.get("createdAt")))
                .build();
    }

    private Date toDate(Object value) {
        if (value == null) return null;
        if (value instanceof Date d) return d;
        if (value instanceof Timestamp t) return t.toDate();
        return null;
    }
}
