package com.streaming.myp2p;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(info = @Info(title = "MyP2P", description = "__Streaming P2P stats collector !__", version = "0.42"))
public class MyP2PApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyP2PApplication.class, args);
    }
}
