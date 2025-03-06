package com.example.availability.web;

import com.example.availability.query.AvailabilityView;
import com.example.availability.query.AvailabilityViewRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AvailabilityController {

	private final AvailabilityViewRepository availabilityViewRepository;

	public AvailabilityController(AvailabilityViewRepository availabilityViewRepository) {
		this.availabilityViewRepository = availabilityViewRepository;
	}

	@GetMapping(path = "/api/availabilities")
	public List<AvailabilityView> getAvailabilitiesByDate(@RequestParam LocalDate date) {
		return this.availabilityViewRepository.findByDate(date);
	}

}
