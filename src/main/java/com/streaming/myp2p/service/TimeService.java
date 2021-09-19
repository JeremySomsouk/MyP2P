package com.streaming.myp2p.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class TimeService {

    private static final int EXPIRATION_WINDOW = 5;

    LocalDateTime getCurrentDateTimeSecond() {
        return LocalDateTime.now()
                            .truncatedTo(ChronoUnit.SECONDS);
    }

    LocalDateTime getExpirationDateTimeSecond() {
        return LocalDateTime.now()
                            .plusMinutes(EXPIRATION_WINDOW)
                            .truncatedTo(ChronoUnit.SECONDS);
    }
}
