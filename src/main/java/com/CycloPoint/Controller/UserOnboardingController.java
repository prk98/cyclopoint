package com.CycloPoint.Controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.CycloPoint.Entity.User;
import com.CycloPoint.Entity.OnboardingRequest;
import com.CycloPoint.Repository.UserRepository;

@RestController
@RequestMapping("/api/v1/user")
public class UserOnboardingController {

	@Autowired
	private UserRepository userRepository;

	@PostMapping("/onboarding")
	public ResponseEntity<?> userOnboarding(@RequestBody OnboardingRequest request, Principal principal) {
		User user = userRepository.findByUsername(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
         user.setWeight(request.getWeight());
         user.setActivityLevel(request.getActivity());
         userRepository.save(user);
         return ResponseEntity.ok().body("{\"message\": \"Forest synced successfully\"}");
		
	}

}
