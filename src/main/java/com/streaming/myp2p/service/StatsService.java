package com.streaming.myp2p.service;

import com.streaming.myp2p.controller.dto.StatsDto;
import com.streaming.myp2p.repository.RedisRepository;
import com.streaming.myp2p.repository.StatsRepository;
import com.streaming.myp2p.repository.model.StatsCacheEntity;
import com.streaming.myp2p.repository.model.StatsEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.streaming.myp2p.repository.RedisRepository.buildStatisticsCacheKey;

@Service
public class StatsService {

    private final RedisRepository redisRepository;
    private final StatsRepository statsRepository;
    private final StatsSessionsService statsSessionsService;
    private final TimeService timeService;

    public StatsService(RedisRepository redisRepository,
                        StatsRepository statsRepository,
                        StatsSessionsService statsSessionsService,
                        TimeService timeService) {
        this.redisRepository = redisRepository;
        this.statsRepository = statsRepository;
        this.statsSessionsService = statsSessionsService;
        this.timeService = timeService;
    }

    // CRON Scheduled each second
    @Scheduled(cron = "* * * * * *")
    public void scheduledDumpCollection() {
        var now = timeService.getCurrentDateTimeSecond();
        var sessionListToCollect = statsSessionsService.getSessionListToCollect(now);
        if (!sessionListToCollect.isEmpty()) {
            collectStatistics(sessionListToCollect, now);
        }
    }

    /**
     * This method collects statistics for intermediate processing, we :
     * - Cache statistics values for later computation.
     * - Schedule the computation for later by adding the keys of the statistics to a 'queue'.
     */
    public void processStatistics(StatsDto stats) {
        var cacheKey = buildStatisticsCacheKey(stats.getCustomer(), stats.getContent());
        redisRepository.createOrUpdateStats(cacheKey, stats.getToken(), stats.getCdn(), stats.getP2p());

        if (statsSessionsService.isNewSession(cacheKey)) {
            statsSessionsService.addSessionForFutureCollection(cacheKey, timeService.getExpirationDateTimeSecond());
        }
    }

    /**
     * This method collects and computes all statistics that were stored in the cache previously (X minutes ago).
     * In the end we clean the cache and remove them from active sessions, awaiting new ones from processStatistics().
     */
    void collectStatistics(List<String> sessionListToCollect, LocalDateTime nowDatetime) {
        for (String sessionKey : sessionListToCollect) {
            var keySplit = sessionKey.split(":");
            var statisticsByKey = redisRepository.getAllStatsSession(sessionKey);
            var startedSessionsNumber = redisRepository.getStartedSessionsNumber(sessionKey);
            var statsEntity = StatsEntity.builder()
                                         .time(nowDatetime)
                                         .customer(keySplit[0])
                                         .content(keySplit[1])
                                         .cdn(statisticsByKey.stream().mapToLong(StatsCacheEntity::getCdn).sum())
                                         .p2p(statisticsByKey.stream().mapToLong(StatsCacheEntity::getP2p).sum())
                                         .sessions(startedSessionsNumber)
                                         .build();

            statsRepository.save(statsEntity);
            redisRepository.deleteStatsSession(sessionKey);
            statsSessionsService.removeActiveSession(sessionKey);
        }

        statsSessionsService.removeSessionListsFromDate(nowDatetime);
    }
}
