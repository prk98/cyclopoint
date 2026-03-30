package com.CycloPoint.Controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.CycloPoint.Entity.PeriodRecord;
import com.CycloPoint.Entity.User;
import com.CycloPoint.Repository.PeriodRepository;
import com.CycloPoint.Repository.UserRepository;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/tracker")
public class TrackerController {

	private final PeriodRepository repository;
	private final ChatClient chatClient;
	private final UserRepository userRepository;

	public TrackerController(PeriodRepository repository, ChatClient.Builder chatClientBuilder,UserRepository userRepository) {
		this.repository = repository;
		this.chatClient = chatClientBuilder.build();
		this.userRepository=userRepository;
	}
	@PostMapping("/log")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> logPeriod(@RequestBody PeriodRecord record, Principal principal) { 
	    // 1. Identify the logged-in user
	    String username = principal.getName();
	    
	    // 2. Fetch the User entity (Assuming you have a userRepository injected)
	    User currentUser = userRepository.findByUsername(username)
	    .orElseThrow(() -> new RuntimeException("User not found"));
	    
	    // 3. Attach the user to the record
	    record.setUser(currentUser);

	    // 4. NOW save it to PostgreSQL
	    repository.save(record);

	    Map<String, Object> response = new HashMap<>();
	    response.put("success", true);
	    response.put("message", "Forest record updated for " + username + "!");
	    
	    return ResponseEntity.ok(response);
	}

	@GetMapping("/ask-ai")
	@ResponseBody
	public Flux<String> askAi(@RequestParam String question,Principal principal) {
		
		String username =principal.getName();
		User currentUser = userRepository.findByUsername(username)
		.orElseThrow(() -> new RuntimeException("User not found"));
	    List<PeriodRecord> history = repository.findByUser(currentUser); 
	    
	    String contextData = history.stream()
	    	    .sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate()))
	    	    .limit(10) // AI models can easily handle 10-15 records now
	    	    .map(r -> String.format(
	    	        "Entry: [%s to %s] | Intensity: %d/5 | Status: %s",
	    	        r.getStartDate(), 
	    	        (r.getEndDate() != null ? r.getEndDate() : "ACTIVE"), 
	    	        r.getIntensity(),
	    	        (r.getEndDate() == null ? "CURRENTLY BLEEDING" : "COMPLETED")
	    	    ))
	    	    .collect(Collectors.joining("\n"));
	    return chatClient.prompt()
	    	    .system(s -> s.text("You are a clinical health assistant. Tone: Professional, direct, and concise. "
	    	            + "Analyze the user's provided health history strictly: " + contextData + ". "
	    	            + "Priority 1: Address the specific question using the data provided. "
	    	            + "Priority 2: Provide evidence-based medical advice (e.g., specific nutrients, rest protocols, activity adjustments). "
	    	            + "Priority 3: If symptoms are reported, provide a brief clinical assessment and suggest consulting a doctor if flags appear. "
	    	            + "Constraint: No metaphors or fluff. Maximum 3 sentences per response."))
	    	    .user(question)
	    	    .stream()
	    	    .content();
	}
	@GetMapping("/active-check")
	public ResponseEntity<PeriodRecord> getActiveCycle(Principal principal) {
	    User currentUser = userRepository.findByUsername(principal.getName())
	    		.orElseThrow(() -> new RuntimeException("User not found"));
	    // Find a record where end_date is NULL
	    return ResponseEntity.ok(repository.findTopByUserAndEndDateIsNullOrderByStartDateDesc(currentUser));
	}
	
	@PutMapping("/close-active")
	public ResponseEntity<?> closeActiveCycle(@RequestBody Map<String, String> payload, Principal principal) {
	    // 1. Get the UUID of the logged-in user (assuming 'id' comes from your session/auth)
	    // For this example, I'll use the record lookup directly
		User user = userRepository.findByUsername(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
				
		UUID userId =user.getId();
	   
	    String endDateStr = payload.get("endDate");
	    LocalDate endDate = (endDateStr != null) ? LocalDate.parse(endDateStr) : LocalDate.now();

	    // 2. Find the ONE active cycle for this user
	    PeriodRecord record = repository.findFirstByUserIdAndEndDateIsNull(userId)
	            .orElseThrow(() -> new RuntimeException("No active cycle found to close!"));

	    // 3. Security Check
	    if (!record.getUser().getUsername().equals(principal.getName())) {
	        return ResponseEntity.status(403).build();
	    }

	    // 4. Close the Path
	    record.setEndDate(endDate);
	    repository.save(record);

	    Map<String, Object> response = new HashMap<>();
	    response.put("success", true);
	    response.put("message", "The forest path has been completed.");
	    return ResponseEntity.ok(response);
	}
}
