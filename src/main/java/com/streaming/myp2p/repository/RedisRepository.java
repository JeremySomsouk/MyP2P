package com.streaming.myp2p.repository;

import com.google.common.annotations.VisibleForTesting;
import com.streaming.myp2p.repository.model.StatsCacheEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RedisRepository {

    private final RedisTemplate<String, String> stringTemplate;
    private final HashOperations<String, String, StatsCacheEntity> hashOperations;

    public RedisRepository(@Qualifier("StatsRedisTemplate") RedisTemplate<String, StatsCacheEntity> statsTemplate,
                           @Qualifier("StringRedisTemplate") RedisTemplate<String, String> stringTemplate) {
        this.hashOperations = statsTemplate.opsForHash();
        this.stringTemplate = stringTemplate;
    }

    /**
     * Stats are being stored in the Redis cache as :
     * "customer1:content1" -> "token1 = {cdn = 1, p2p = 3}", "token2 = {cdn = 2, p2p = 5}", etc.
     * "customer2:content2" -> "token4 = {cdn = 9, p2p = 4}", "token9 = {cdn = 3, p2p = 9}", etc.
     */
    public void createOrUpdateStats(String key, String hashKey, Long cdn, Long p2p) {
        StatsCacheEntity stats = findAvailableStats(key, hashKey);
        var isAvailableStats = stats != null;
        hashOperations.put(key, hashKey, StatsCacheEntity.builder()
                                                         .cdn(isAvailableStats ? stats.getCdn() + cdn : cdn)
                                                         .p2p(isAvailableStats ? stats.getP2p() + p2p : p2p)
                                                         .build());
    }

    public StatsCacheEntity findAvailableStats(String key, String hashKey) {
        return hashOperations.get(key, hashKey);
    }

    public List<StatsCacheEntity> getAllStatsSession(String key) {
        return hashOperations.values(key);
    }

    public void deleteStatsSession(String key) {
        stringTemplate.delete(key);
    }

    public Integer getStartedSessionsNumber(String key) {
        return Math.toIntExact(hashOperations.size(key));
    }

    public static String buildStatisticsCacheKey(String customer, String content) {
        return customer + ":" + content;
    }

    @VisibleForTesting
    public void flushAll() {
        stringTemplate.getConnectionFactory().getConnection().flushAll();
    }
}
