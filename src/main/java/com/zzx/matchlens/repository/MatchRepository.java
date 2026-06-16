package com.zzx.matchlens.repository;

import com.zzx.matchlens.entity.Match;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MatchRepository {

    private final Map<String, Match> store = new ConcurrentHashMap<>();

    public Match save(Match match) {
        store.put(match.getMatchId(), match);
        return match;
    }

    public Optional<Match> findById(String matchId) {
        return Optional.ofNullable(store.get(matchId));
    }

    public List<Match> findAll() {
        return new ArrayList<>(store.values());
    }

    public void delete(String matchId) {
        store.remove(matchId);
    }
}
