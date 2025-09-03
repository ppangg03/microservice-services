package com.javaservice.Service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.javaservice.Service.Entity.Transaction_report;

@Repository
public interface TransactionFeailureRepository extends JpaRepository<Transaction_report,Long> {
	
	
}
