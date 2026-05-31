package com.ttscore.match;

import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class FirestoreMatchRepository implements MatchRepository {

    private static final String COLLECTION = "matches";
    private final Firestore firestore;

    @Override
    public Match save(Match match) {
        try {
            if (match.getId() == null) {
                match.setId(UUID.randomUUID().toString());
                match.setPlayedAt(new Date());
            }
            firestore.collection(COLLECTION).document(match.getId()).set(toMap(match)).get();
            return match;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving match", e);
        }
    }

    @Override
    public Optional<Match> findById(String id) {
        try {
            var doc = firestore.collection(COLLECTION).document(id).get().get();
            return doc.exists() ? Optional.of(fromDoc(id, doc.getData())) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding match", e);
        }
    }

    @Override
    public List<Match> findByUserId(String userId) {
        try {
            var p1 = firestore.collection(COLLECTION).whereEqualTo("player1Id", userId).get().get();
            var p2 = firestore.collection(COLLECTION).whereEqualTo("player2Id", userId).get().get();

            List<Match> matches = new ArrayList<>();
            p1.getDocuments().forEach(d -> matches.add(fromDoc(d.getId(), d.getData())));
            p2.getDocuments().forEach(d -> matches.add(fromDoc(d.getId(), d.getData())));
            matches.sort(Comparator.comparing(Match::getPlayedAt).reversed());
            return matches;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding matches", e);
        }
    }

    @Override
    public List<Match> findByBothPlayers(String player1Id, String player2Id) {
        return findByUserId(player1Id).stream()
                .filter(m -> m.getPlayer2Id().equals(player2Id) || m.getPlayer1Id().equals(player2Id))
                .toList();
    }

    private Map<String, Object> toMap(Match match) {
        Map<String, Object> map = new HashMap<>();
        map.put("player1Id", match.getPlayer1Id());
        map.put("player2Id", match.getPlayer2Id());
        map.put("player1Score", match.getPlayer1Score());
        map.put("player2Score", match.getPlayer2Score());
        map.put("winnerId", match.getWinnerId());
        map.put("playedAt", match.getPlayedAt());
        return map;
    }

    private Match fromDoc(String id, Map<String, Object> data) {
        return Match.builder()
                .id(id)
                .player1Id((String) data.get("player1Id"))
                .player2Id((String) data.get("player2Id"))
                .player1Score(((Long) data.get("player1Score")).intValue())
                .player2Score(((Long) data.get("player2Score")).intValue())
                .winnerId((String) data.get("winnerId"))
                .playedAt((Date) data.get("playedAt"))
                .build();
    }
}
