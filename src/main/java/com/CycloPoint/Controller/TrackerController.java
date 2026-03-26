package com.CycloPoint.Controller;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	    User currentUser = userRepository.findByUsername(username);
	    
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
		User currentUser = userRepository.findByUsername(username);
	    List<PeriodRecord> history = repository.findByUser(currentUser); 
	    
	    String contextData = history.stream()
	            .sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate())) // Get newest first
	            .limit(5) 
	            .map(r -> "Dates: " + r.getStartDate() + " to " + r.getEndDate() + " | Intensity: " + r.getIntensity() + "/5")
	            .reduce("", (a, b) -> a + "\n" + b);

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

}
