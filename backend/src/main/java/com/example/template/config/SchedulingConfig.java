package com.example.template.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables Spring's @Scheduled support for the Gmail sync poller. */
@Configuration
@EnableScheduling
public class SchedulingConfig {}
