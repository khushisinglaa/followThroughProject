package com.meetingos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeetingOsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MeetingOsApplication.class, args);
    }
}
