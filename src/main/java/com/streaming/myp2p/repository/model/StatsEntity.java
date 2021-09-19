package com.streaming.myp2p.repository.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stats")
public class StatsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long statsId;

    @Column(nullable = false)
    LocalDateTime time;

    @Column(nullable = false)
    String customer;

    @Column(nullable = false)
    String content;

    @Column(nullable = false)
    Long cdn;

    @Column(nullable = false)
    Long p2p;

    @Column(nullable = false)
    Integer sessions;
}
