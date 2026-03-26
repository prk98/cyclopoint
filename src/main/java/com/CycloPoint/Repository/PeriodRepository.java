package com.CycloPoint.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.CycloPoint.Entity.PeriodRecord;
import com.CycloPoint.Entity.User;

@Repository
public interface PeriodRepository extends JpaRepository<PeriodRecord, UUID> {
	    
	    // Used for the "Recent Activity" list on your dashboard
	    @Query("SELECT p FROM PeriodRecord p WHERE p.user.id = :userId ORDER BY p.startDate DESC LIMIT 5")
	    List<PeriodRecord> findRecentCyclesByUserId(UUID userId);
	    // This is the core method for your User-Specific Heatmap
	    List<PeriodRecord> findByUserId(UUID userId);
	    List<PeriodRecord> findByUser(User user);
	}	


