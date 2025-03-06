package com.example.config;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.IdGenerator;

@Configuration(proxyBeanMethods = false)
public class AppConfig {

	@Bean
	public IdGenerator uuidV7Generator() {
		// UUID v7
		TimeBasedEpochGenerator generator = Generators.timeBasedEpochGenerator();
		return generator::generate;
	}

}
