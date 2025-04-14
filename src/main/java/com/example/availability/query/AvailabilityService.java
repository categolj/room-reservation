package com.example.availability.query;

import java.time.LocalDate;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

	private final AvailabilityViewRepository availabilityViewRepository;

	public AvailabilityService(AvailabilityViewRepository availabilityViewRepository) {
		this.availabilityViewRepository = availabilityViewRepository;
	}

	@Tool(description = """
			Retrieves room availability information for a specific date.
			Returns a list of availability views for all rooms on the given date.
			This information can be used to determine which rooms are available for reservation at what times.
			""")
	public List<AvailabilityView> getAvailabilitiesByDate(
			@ToolParam(description = "The date for which to retrieve room availability information.") LocalDate date) {
		return this.availabilityViewRepository.findByDate(date);
	}

}
