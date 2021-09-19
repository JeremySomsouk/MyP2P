package com.streaming.myp2p.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class StatsSessionsServiceTest {

    private final StatsSessionsService statsSessionsService = new StatsSessionsService();

    @Test
    public void addSessionForFutureCollection_setActiveSession() {
        // GIVEN
        var key = "customer:content";

        // WHEN
        assertThat(statsSessionsService.isNewSession(key)).isTrue();
        statsSessionsService.addSessionForFutureCollection(key, LocalDateTime.now());

        // THEN
        assertThat(statsSessionsService.isNewSession(key)).isFalse();
    }

    @Test
    public void addSessionForFutureCollection_addSessionListQueue() {
        // GIVEN
        var key = "customer:content";
        final LocalDateTime now = LocalDateTime.now();

        // WHEN
        statsSessionsService.addSessionForFutureCollection(key, now);

        // THEN
        var sessionListToCollect = statsSessionsService.getSessionListToCollect(now);
        assertThat(sessionListToCollect.size()).isOne();
    }

    @Test
    public void removeSessionListsFromDate_removeFromSessionListQueue() {
        // GIVEN
        var key = "customer:content";
        final LocalDateTime now = LocalDateTime.now();

        // WHEN
        statsSessionsService.addSessionForFutureCollection(key, now);
        assertThat(statsSessionsService.getSessionListToCollect(now).size()).isOne();
        statsSessionsService.removeSessionListsFromDate(now);

        // THEN
        assertThat(statsSessionsService.getSessionListToCollect(now).size()).isZero();
    }

    @Test
    public void removeActiveSession_freeSessionFromActiveSession() {
        // GIVEN
        var key = "customer:content";

        // WHEN
        statsSessionsService.addSessionForFutureCollection(key, LocalDateTime.now());
        assertThat(statsSessionsService.isNewSession(key)).isFalse();
        statsSessionsService.removeActiveSession(key);

        // THEN
        assertThat(statsSessionsService.isNewSession(key)).isTrue();
    }
}
