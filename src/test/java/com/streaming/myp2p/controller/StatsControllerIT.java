package com.streaming.myp2p.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaming.myp2p.configuration.ContainersConfiguration;
import com.streaming.myp2p.controller.dto.StatsDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ContextConfiguration(initializers = {ContainersConfiguration.Initializer.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StatsControllerIT extends ContainersConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void post_nominal() throws Exception {
        StatsDto statsDto = StatsDto.builder()
                                    .customer("customer1")
                                    .content("content1")
                                    .token(UUID.randomUUID().toString())
                                    .cdn(123L)
                                    .p2p(345L)
                                    .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/stats")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(objectMapper.writeValueAsString(statsDto)))
               .andExpect(status().isOk());
    }


    @Test
    public void post_invalidCustomerName_returnBadRequest() throws Exception {
        StatsDto statsDto = StatsDto.builder()
                                    .customer("cust:omer1")
                                    .content("content1")
                                    .token(UUID.randomUUID().toString())
                                    .cdn(123L)
                                    .p2p(345L)
                                    .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/stats")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(objectMapper.writeValueAsString(statsDto)))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void post_invalidContentName_returnBadRequest() throws Exception {
        StatsDto statsDto = StatsDto.builder()
                                    .customer("customer1")
                                    .content("cont:ent1")
                                    .token(UUID.randomUUID().toString())
                                    .cdn(123L)
                                    .p2p(345L)
                                    .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/stats")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(objectMapper.writeValueAsString(statsDto)))
               .andExpect(status().isBadRequest());
    }
}
