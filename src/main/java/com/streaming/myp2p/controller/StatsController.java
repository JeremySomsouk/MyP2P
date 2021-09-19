package com.streaming.myp2p.controller;

import com.streaming.myp2p.controller.dto.StatsDto;
import com.streaming.myp2p.controller.exception.BadRequestException;
import com.streaming.myp2p.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/stats",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
public class StatsController {

    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping
    public void postStatistics(@RequestBody StatsDto stats) {
        if (stats.isInvalid()) {
            throw new BadRequestException("Invalid input received");
        }

        statsService.processStatistics(stats);
    }
}