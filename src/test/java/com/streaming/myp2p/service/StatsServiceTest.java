package com.streaming.myp2p.service;

import com.streaming.myp2p.configuration.ContainersConfiguration;
import com.streaming.myp2p.controller.dto.StatsDto;
import com.streaming.myp2p.repository.RedisRepository;
import com.streaming.myp2p.repository.StatsRepository;
import com.streaming.myp2p.repository.model.StatsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(initializers = {ContainersConfiguration.Initializer.class})
public class StatsServiceTest extends ContainersConfiguration {

    @Autowired
    private StatsService service;
    @Autowired
    private RedisRepository redisRepository;
    @Autowired
    private StatsRepository statsRepository;
    @Autowired
    private StatsSessionsService statsSessionsService;
    @MockBean
    private TimeService timeService;

    @BeforeEach
    public void cleanup() {
        redisRepository.flushAll();
        statsRepository.deleteAll();
    }

    @Test
    public void processStatistics_shouldAddStatsForCollecting_nominal() throws InterruptedException {
        // GIVEN
        var customer = "customer11";
        var content = "content11";
        var cdn = 123L;
        var p2p = 345L;
        StatsDto statsDto = StatsDto.builder()
                                    .customer(customer)
                                    .content(content)
                                    .token(UUID.randomUUID().toString())
                                    .cdn(cdn)
                                    .p2p(p2p)
                                    .build();
        when(timeService.getCurrentDateTimeSecond())
                .thenReturn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        when(timeService.getExpirationDateTimeSecond())
                .thenReturn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        // WHEN
        service.processStatistics(statsDto);
        Thread.sleep(1000); // waiting for the scheduler

        // THEN
        var entities = statsRepository.findAll();
        assertThat(entities.size()).isOne();
        var statsEntity = (StatsEntity) entities.get(0);
        assertThat(statsEntity.getCustomer()).isEqualTo(customer);
        assertThat(statsEntity.getContent()).isEqualTo(content);
        assertThat(statsEntity.getCdn()).isEqualTo(cdn);
        assertThat(statsEntity.getP2p()).isEqualTo(p2p);
    }

    @Test
    public void processStatistics_shouldSumDataValues_whenProcessedMultipleTimes() throws InterruptedException {
        // GIVEN
        var customer = "customer55";
        var content = "content55";
        var cdn = 123L;
        var p2p = 345L;
        StatsDto statsDto = StatsDto.builder()
                                    .customer(customer)
                                    .content(content)
                                    .token(UUID.randomUUID().toString())
                                    .cdn(cdn)
                                    .p2p(p2p)
                                    .build();
        when(timeService.getCurrentDateTimeSecond())
                .thenReturn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        when(timeService.getExpirationDateTimeSecond())
                .thenReturn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        // WHEN
        for (int i = 0; i < 10; i++) {
            service.processStatistics(statsDto);
        }
        Thread.sleep(1000); // waiting for the scheduler

        // THEN
        var entities = statsRepository.findAll();
        assertThat(entities.size()).isOne();
        var statsEntity = (StatsEntity) entities.get(0);
        assertThat(statsEntity.getCustomer()).isEqualTo(customer);
        assertThat(statsEntity.getContent()).isEqualTo(content);
        assertThat(statsEntity.getCdn()).isEqualTo(cdn * 10);
        assertThat(statsEntity.getP2p()).isEqualTo(p2p * 10);
    }

    @Test
    public void processStatistics_shouldAddToRedis_andNotPostgres_withExpirationNotMet() {
        // GIVEN
        var customer = "customer22";
        var content = "content22";
        var cacheKey = RedisRepository.buildStatisticsCacheKey(customer, content);
        StatsDto statsDto = StatsDto.builder()
                                    .customer(customer)
                                    .content(content)
                                    .token(UUID.randomUUID().toString())
                                    .cdn(123L)
                                    .p2p(345L)
                                    .build();
        when(timeService.getCurrentDateTimeSecond())
                .thenReturn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        when(timeService.getExpirationDateTimeSecond())
                .thenReturn(LocalDateTime.now().plusMinutes(5).truncatedTo(ChronoUnit.SECONDS));

        // WHEN
        service.processStatistics(statsDto);

        // THEN
        assertThat(statsRepository.findAll().size()).isZero();
        assertThat(redisRepository.getStartedSessionsNumber(cacheKey)).isOne();
        assertThat(redisRepository.getAllStatsSession(cacheKey).size()).isOne();
    }

    @Test
    public void processStatistics_shouldRegisterCacheKeyToActiveSession_withExpirationNotMet() {
        // GIVEN
        var customer = "customer99";
        var content = "content99";
        var cacheKey = RedisRepository.buildStatisticsCacheKey(customer, content);
        StatsDto statsDto = StatsDto.builder()
                                    .customer(customer)
                                    .content(content)
                                    .token(UUID.randomUUID().toString())
                                    .cdn(123L)
                                    .p2p(345L)
                                    .build();
        when(timeService.getCurrentDateTimeSecond())
                .thenReturn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        when(timeService.getExpirationDateTimeSecond())
                .thenReturn(LocalDateTime.now().plusMinutes(5).truncatedTo(ChronoUnit.SECONDS));

        // WHEN
        assertThat(statsSessionsService.isNewSession(cacheKey)).isTrue();
        service.processStatistics(statsDto);

        // THEN
        assertThat(statsSessionsService.isNewSession(cacheKey)).isFalse();
    }

    @Test
    public void collectStatistics_shouldSaveEntity() {
        // GIVEN
        var customer = "customer946";
        var content = "content1111";
        var cacheKey = RedisRepository.buildStatisticsCacheKey(customer, content);
        StatsDto statsDto = StatsDto.builder()
                                    .customer(customer)
                                    .content(content)
                                    .token(UUID.randomUUID().toString())
                                    .cdn(123L)
                                    .p2p(345L)
                                    .build();
        var timeToCollect = LocalDateTime.now().plusMinutes(5).truncatedTo(ChronoUnit.SECONDS);
        when(timeService.getCurrentDateTimeSecond())
                .thenReturn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        when(timeService.getExpirationDateTimeSecond())
                .thenReturn(timeToCollect);

        // WHEN
        service.processStatistics(statsDto);
        service.collectStatistics(statsSessionsService.getSessionListToCollect(timeToCollect),
                                  timeToCollect);

        // THEN
        assertThat(statsRepository.findAll().size()).isOne();
        assertThat(redisRepository.getStartedSessionsNumber(cacheKey)).isZero();
        assertThat(redisRepository.getAllStatsSession(cacheKey).size()).isZero();
        assertThat(statsSessionsService.getSessionListToCollect(timeToCollect).size()).isZero();
    }
}
