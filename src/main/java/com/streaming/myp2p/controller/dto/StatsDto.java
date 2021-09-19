package com.streaming.myp2p.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatsDto {

    @JsonProperty("token")
    private String token;

    @JsonProperty("customer")
    private String customer;

    @JsonProperty("content")
    private String content;

    @JsonProperty("cdn")
    private Long cdn;

    @JsonProperty("p2p")
    private Long p2p;

    @JsonIgnore
    public boolean isInvalid() {
        return !(StringUtils.isNotBlank(this.token)
                 && StringUtils.isNotBlank(this.customer)
                 && !this.customer.contains(":")
                 && StringUtils.isNotBlank(this.content)
                 && !this.content.contains(":")
                 && cdn != null
                 && p2p != null);
    }
}
