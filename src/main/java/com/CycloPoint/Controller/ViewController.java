package com.CycloPoint.Controller;

import java.beans.JavaBean;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.CycloPoint.Entity.PeriodRecord;
import com.CycloPoint.Entity.User;
import com.CycloPoint.Repository.PeriodRepository;
import com.CycloPoint.Repository.UserRepository;

@Controller
public class ViewController {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	private final PeriodRepository repository;

	public ViewController(PeriodRepository repository, PasswordEncoder passwordEncoder, UserRepository userRepository) {
		this.userRepository = userRepository;
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/")
	public String index(Model model, Principal principal) {
		if (principal != null) {
			// 1. Get the current logged-in user
			String username = principal.getName();
			User currentUser = userRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("User not found"));
			Boolean isNewUser = (currentUser.getWeight()==null);
		

			// 2. Fetch ONLY their records
			List<PeriodRecord> userRecords = repository.findByUser(currentUser);

			// 3. Add to the model for Thymeleaf to render
			model.addAttribute("isNewUser", isNewUser);
			model.addAttribute("history", userRecords);
			model.addAttribute("username", username);
			
		}

		return "index";
	}

	@GetMapping("/login")
	public String login() {
		return "login"; // Refers to login.html
	}

	@PostMapping("/register")
	public String registerUser(@RequestParam String username, @RequestParam String password,
			RedirectAttributes redirectAttributes) {
		if (username.length() < 3 || password.length() < 6) {
			redirectAttributes.addFlashAttribute("error", "username[3+] and passsword[6+] too short.");
			return "redirect:/login?register";
		}
		if (userRepository.findByUsername(username).isPresent()) {
			redirectAttributes.addFlashAttribute("error", "That name is already taken in this forest.");
			return "redirect:/login?register";
		}
		User user = new User();
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode(password));
		userRepository.save(user);
		return "redirect:/login?success";
	}
}
