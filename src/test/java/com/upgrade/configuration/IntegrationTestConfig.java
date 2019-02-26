package com.upgrade.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.upgrade.config.CampsiteConfiguration;
import com.upgrade.config.SecurityConfiguration;

@Configuration
@Import({CampsiteConfiguration.class, SecurityConfiguration.class})
public class IntegrationTestConfig {
}
