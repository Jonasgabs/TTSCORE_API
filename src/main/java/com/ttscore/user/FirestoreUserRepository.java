package com.ttscore.user;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.google.cloud.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class FirestoreUserRepository implements UserRepository {

    private static final String COLLECTION = "users";
    private final Firestore firestore;

    @Override
    public User save(User user) {
        try {
            if (user.getId() == null) {
                user.setId(UUID.randomUUID().toString());
                user.setCreatedAt(new Date());
            }
            firestore.collection(COLLECTION).document(user.getId()).set(toMap(user)).get();
            return user;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public Optional<User> findById(String id) {
        try {
            var doc = firestore.collection(COLLECTION).document(id).get().get();
            return doc.exists() ? Optional.of(fromDoc(id, doc.getData())) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding user", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return queryFirst("username", username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return queryFirst("email", email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public List<User> searchByUsername(String query) {
        try {
            var snapshot = firestore.collection(COLLECTION)
                    .orderBy("username")
                    .startAt(query)
                    .endAt(query + "")
                    .get().get();
            return snapshot.getDocuments().stream()
                    .map(d -> fromDoc(d.getId(), d.getData()))
                    .toList();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error searching users", e);
        }
    }

    private Optional<User> queryFirst(String field, String value) {
        try {
            var snapshot = firestore.collection(COLLECTION)
                    .whereEqualTo(field, value)
                    .limit(1)
                    .get().get();
            if (snapshot.isEmpty()) return Optional.empty();
            QueryDocumentSnapshot doc = snapshot.getDocuments().get(0);
            return Optional.of(fromDoc(doc.getId(), doc.getData()));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error querying user", e);
        }
    }

    @Override
    public List<User> findAllOrderedByWins() {
        try {
            var snapshot = firestore.collection(COLLECTION)
                    .orderBy("wins", com.google.cloud.firestore.Query.Direction.DESCENDING)
                    .get().get();
            return snapshot.getDocuments().stream()
                    .map(d -> fromDoc(d.getId(), d.getData()))
                    .toList();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching ranking", e);
        }
    }

    private Map<String, Object> toMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("password", user.getPassword());
        map.put("avatarUrl", user.getAvatarUrl());
        map.put("createdAt", user.getCreatedAt());
        map.put("wins", user.getWins());
        map.put("losses", user.getLosses());
        return map;
    }

    private User fromDoc(String id, Map<String, Object> data) {
        Number wins = (Number) data.getOrDefault("wins", 0);
        Number losses = (Number) data.getOrDefault("losses", 0);
        return User.builder()
                .id(id)
                .username((String) data.get("username"))
                .email((String) data.get("email"))
                .password((String) data.get("password"))
                .avatarUrl((String) data.get("avatarUrl"))
                .createdAt(toDate(data.get("createdAt")))
                .wins(wins.intValue())
                .losses(losses.intValue())
                .build();
    }

    private Date toDate(Object value) {
        if (value == null) return null;
        if (value instanceof Date d) return d;
        if (value instanceof Timestamp t) return t.toDate();
        return null;
    }
}
