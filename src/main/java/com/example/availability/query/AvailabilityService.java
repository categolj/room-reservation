package com.example.availability.query;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

	private final AvailabilityViewRepository availabilityViewRepository;

	public AvailabilityService(AvailabilityViewRepository availabilityViewRepository) {
		this.availabilityViewRepository = availabilityViewRepository;
	}

	public List<AvailabilityView> getAvailabilitiesByDate(LocalDate date) {
		return this.availabilityViewRepository.findByDate(date);
	}

}
