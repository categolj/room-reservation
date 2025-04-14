package com.example.availability.web;

import com.example.availability.query.AvailabilityService;
import com.example.availability.query.AvailabilityView;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AvailabilityController {

	private final AvailabilityService availabilityService;

	public AvailabilityController(AvailabilityService availabilityService) {
		this.availabilityService = availabilityService;
	}

	@GetMapping(path = "/api/availabilities")
	public List<AvailabilityView> getAvailabilitiesByDate(@RequestParam LocalDate date) {
		return this.availabilityService.getAvailabilitiesByDate(date);
	}

}
