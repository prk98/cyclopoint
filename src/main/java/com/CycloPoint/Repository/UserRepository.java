package com.CycloPoint.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.CycloPoint.Entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
	User findByUsername(String username);

}
