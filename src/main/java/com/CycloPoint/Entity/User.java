package com.CycloPoint.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank(message="Username Required.")
	@Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
	@Column(unique = true, nullable = false)
	private String username;

	@NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
	@Column(nullable = false)
	private String password; // Remember to encode this later!

	// mappedBy refers to the "user" field in the PeriodRecord class
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PeriodRecord> records;

	// Standard Getters and Setters
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<PeriodRecord> getRecords() {
		return records;
	}

	public void setRecords(List<PeriodRecord> records) {
		this.records = records;
	}
}