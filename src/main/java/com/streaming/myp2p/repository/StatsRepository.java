package com.streaming.myp2p.repository;

import com.streaming.myp2p.repository.model.StatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatsRepository extends JpaRepository<StatsEntity, Long> {
}
