package com.streaming.myp2p.repository;

import com.streaming.myp2p.configuration.ContainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {ContainersConfiguration.Initializer.class})
public class RedisRepositoryTest extends ContainersConfiguration {

    @Autowired
    private RedisRepository repository;

    @BeforeEach
    public void cleanup() {
        repository.flushAll();
    }

    @Test
    public void createOrUpdateStats_shouldInsertFirstElement_nominal() {
        // GIVEN
        var key = "customer:content";
        var hashKey = "token";
        var cdn = 123L;
        var p2p = 456L;

        // WHEN
        repository.createOrUpdateStats(key, hashKey, cdn, p2p);

        // THEN
        var allStatsSession = repository.getAllStatsSession(key);
        assertThat(allStatsSession.size()).isOne();
        var entity = allStatsSession.get(0);
        assertThat(entity.getCdn()).isEqualTo(cdn);
        assertThat(entity.getP2p()).isEqualTo(p2p);
    }

    @Test
    public void createOrUpdateStats_shouldIncrement_withSameSecondElement() {
        // GIVEN
        var key = "customer:content";
        var hashKey = "token";
        var cdn = 111L;
        var p2p = 333L;

        // WHEN
        repository.createOrUpdateStats(key, hashKey, cdn, p2p);
        repository.createOrUpdateStats(key, hashKey, cdn, p2p);

        // THEN
        var allStatsSession = repository.getAllStatsSession(key);
        assertThat(allStatsSession.size()).isOne();
        var entity = allStatsSession.get(0);
        assertThat(entity.getCdn()).isEqualTo(cdn * 2);
        assertThat(entity.getP2p()).isEqualTo(p2p * 2);
    }

    @Test
    public void createOrUpdateStats_shouldAddElement_withThirdDifferentElement() {
        // GIVEN
        var key = "customer:content";
        var hashKey = "token";
        var cdn = 111L;
        var p2p = 333L;

        // WHEN
        repository.createOrUpdateStats(key, hashKey, cdn, p2p);
        repository.createOrUpdateStats(key, hashKey, cdn, p2p);
        repository.createOrUpdateStats(key, "token2", cdn, p2p);

        //// THEN
        assertThat(repository.getAllStatsSession(key).size()).isEqualTo(2);
    }

    @Test
    public void findAvailableStats_shouldReturnValue_nominalCase() {
        // GIVEN
        var key = "customer:content";
        var hashKey = "token";
        var cdn = 123L;
        var p2p = 456L;

        // WHEN
        repository.createOrUpdateStats(key, hashKey, cdn, p2p);
        var foundStats = repository.findAvailableStats(key, hashKey);

        // THEN
        assertThat(foundStats.getCdn()).isEqualTo(cdn);
        assertThat(foundStats.getP2p()).isEqualTo(p2p);
    }

    @Test
    public void findAvailableStats_shouldNull_whenUnknow() {
        // GIVEN | WHEN
        var foundStats = repository.findAvailableStats("a", "b");

        // THEN
        assertThat(foundStats).isNull();
    }

    @Test
    public void getAllStatsSessions_shouldReturnAllTargetedStatistics_nominalCase() {
        // GIVEN
        var key = "customer:content";
        var p2p = 456L;

        // WHEN
        repository.createOrUpdateStats(key, "token1", 1L, p2p);
        repository.createOrUpdateStats(key, "token2", 2L, p2p);
        repository.createOrUpdateStats(key, "token3", 3L, p2p);
        var allStats = repository.getAllStatsSession(key);

        // THEN
        assertThat(allStats.size()).isEqualTo(3);
        assertThat(allStats.get(0).getCdn()).isEqualTo(1L);
        assertThat(allStats.get(1).getCdn()).isEqualTo(2L);
        assertThat(allStats.get(2).getCdn()).isEqualTo(3L);
    }

    @Test
    public void deleteSession_shouldCleanKeyStatistics_nominalCase() {
        // GIVEN
        var key = "customer:content";
        var p2p = 456L;

        // WHEN
        repository.createOrUpdateStats(key, "token1", 1L, p2p);
        repository.createOrUpdateStats(key, "token2", 2L, p2p);
        repository.createOrUpdateStats(key, "token3", 3L, p2p);
        var allStats = repository.getAllStatsSession(key);
        assertThat(allStats.size()).isEqualTo(3);

        repository.deleteStatsSession(key);

        // THEN
        allStats = repository.getAllStatsSession(key);
        assertThat(allStats.size()).isEqualTo(0);
    }

    @Test
    public void getStartedSessionsNumber_shouldCountAliveSessions() {
        // GIVEN
        var key = "customer1:content1";
        var key2 = "customer2:content3";
        var cdn = 123L;
        var p2p = 456L;

        // WHEN
        repository.createOrUpdateStats(key, "token1", cdn, p2p);
        repository.createOrUpdateStats(key, "token2", cdn, p2p);
        repository.createOrUpdateStats(key, "token3", cdn, p2p);
        repository.createOrUpdateStats(key2, "token1", cdn, p2p);

        var nbStartedSessions = repository.getStartedSessionsNumber(key);
        var nbStartedSessions2 = repository.getStartedSessionsNumber(key2);

        // THEN
        assertThat(nbStartedSessions).isEqualTo(3);
        assertThat(nbStartedSessions2).isEqualTo(1);
    }

    @Test
    public void buildStatisticsCacheKey_nominal() {
        // GIVEN
        var customer = "customer";
        var content = "content";

        // WHEN
        var cacheKey = RedisRepository.buildStatisticsCacheKey(customer, content);

        // THEN
        assertThat(cacheKey).isEqualTo("customer:content");
    }
}
