package org.webchat.webchatbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.webchat.webchatbackend.mapper")
public class WebChatBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebChatBackendApplication.class, args);
    }

}
