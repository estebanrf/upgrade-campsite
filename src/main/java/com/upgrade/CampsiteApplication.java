package com.upgrade;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.joda.time.DateTimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CampsiteApplication {

	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	public static void main(String[] args) {
		SpringApplication.run(CampsiteApplication.class, args);
	}

}

