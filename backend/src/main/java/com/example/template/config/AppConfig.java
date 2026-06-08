package com.example.template.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync   // required for @Async on EmailService
public class AppConfig {
}
