package com.upgrade.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = "com.upgrade")
@PropertySource("classpath:application.properties")
public class CampsiteConfiguration {
}
