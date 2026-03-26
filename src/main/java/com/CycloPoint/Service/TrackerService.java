package com.CycloPoint.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CycloPoint.Entity.PeriodRecord;
import com.CycloPoint.Repository.PeriodRepository;

@Service
public class TrackerService {

	@Autowired
	private PeriodRepository repository;

	public String getAiContext(UUID userId) {
		List<PeriodRecord> records = repository.findRecentCyclesByUserId(userId);

		StringBuilder sb = new StringBuilder("Kansh's recent cycle history:\n");
		for (PeriodRecord record : records) {
			sb.append(String.format("- Started: %s, Ended: %s. Symptoms: %s\n", record.getStartDate(),
					record.getEndDate() != null ? record.getEndDate() : "Ongoing", record.getIntensity()));
		}
		return sb.toString();
	}

	public List<Map<String, Object>> getHeatmapStats(UUID userId) {
		List<PeriodRecord> records = repository.findByUserId(userId);
		List<Map<String, Object>> points = new ArrayList<>();

		for (PeriodRecord record : records) {
			LocalDate current = record.getStartDate();
			LocalDate end = record.getEndDate();

			// Loop through every day in the range
			while (!current.isAfter(end)) {
				Map<String, Object> point = new HashMap<>();
				// Cal-Heatmap needs Unix Seconds (not milliseconds)
				point.put("timestamp", current.atStartOfDay(ZoneOffset.UTC).toEpochSecond());
				point.put("weight", record.getIntensity());
				points.add(point);

				current = current.plusDays(1);
			}
		}
		return points;
	}

}
