package com.CycloPoint.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CycloPoint.Entity.User;
import com.CycloPoint.Repository.UserRepository;
import com.CycloPoint.Service.TrackerService;

@RestController
@RequestMapping("/api/cycles")
public class CycleStatsController {

	private final TrackerService service;
	private final UserRepository userRepository;
	public CycleStatsController(TrackerService service,UserRepository repository) {
		this.service = service;
		this.userRepository=repository;
	}
	@GetMapping("/heatmap")
	public ResponseEntity<List<Map<String, Object>>> getHeatmap(Principal principal) {
	    User user = userRepository.findByUsername(principal.getName());
	    return ResponseEntity.ok(service.getHeatmapStats(user.getId()));
	}
	
	
	
}
