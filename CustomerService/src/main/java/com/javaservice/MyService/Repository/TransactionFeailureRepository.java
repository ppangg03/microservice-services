package com.javaservice.MyService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.javaservice.MyService.Entity.Transaction_failure_report;

@Repository
public interface TransactionFeailureRepository extends JpaRepository<Transaction_failure_report,Long> {
	
	
}
