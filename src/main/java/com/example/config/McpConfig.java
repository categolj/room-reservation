package com.example.config;

import com.example.availability.query.AvailabilityService;
import com.example.reservation.query.ReservationService;
import java.time.LocalDateTime;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;

@Configuration(proxyBeanMethods = false)
public class McpConfig {

	@Bean
	public ToolCallbackProvider toolCallbackProvider(AvailabilityService availabilityService,
			ReservationService reservationService) {
		return MethodToolCallbackProvider.builder()
			.toolObjects(availabilityService, reservationService, new DateTimeTools())
			.build();
	}

	private static class DateTimeTools {

		@Tool(description = "Get the current date and time in the users timezone")
		public String getCurrentDateTime() {
			return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
		}

	}

}
