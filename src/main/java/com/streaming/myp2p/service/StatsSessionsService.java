package com.streaming.myp2p.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class StatsSessionsService {

    /* Keep track of the combination of "customer:content" that are currently playing */
    private final Set<String> activeSessionKeys = new HashSet<>();

    /* Register all sessions to be later on collected on Redis, example :
     * - "2021-09-19T20:30:19" -> [ "customer1:content1", "customer2:content3" ,"customer4:content9"]
     * - "2021-09-19T20:30:20" -> [ ""customer4:content8"]
     */
    private final Map<LocalDateTime, List<String>> sessionListQueue = new HashMap<>();

    List<String> getSessionListToCollect(LocalDateTime datetime) {
        return Optional.ofNullable(sessionListQueue.get(datetime))
                       .orElse(Collections.emptyList());
    }

    boolean isNewSession(String key) {
        return !activeSessionKeys.contains(key);
    }

    void addSessionForFutureCollection(String key, LocalDateTime datetime) {
        var activeKeysToCollectLater = sessionListQueue.getOrDefault(datetime, new ArrayList<>());
        activeKeysToCollectLater.add(key);

        sessionListQueue.put(datetime, activeKeysToCollectLater);
        activeSessionKeys.add(key);
    }

    void removeActiveSession(String sessionKey) {
        activeSessionKeys.remove(sessionKey);
    }

    void removeSessionListsFromDate(LocalDateTime datetime) {
        sessionListQueue.remove(datetime);
    }
}
