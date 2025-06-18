package com.javaservice.MyService.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.javaservice.MyService.Entity.Transaction;
@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {
	
	public Optional<Transaction> findById(Long transactionid);

	@Query("SELECT t FROM Transaction t WHERE t.transaction_state = :status AND t.timestamp <= :yesterdayEnd")
	List<Transaction> findByTransactionState(@Param("status") String status,@Param("yesterdayEnd") String yesterdayEnd);
	
}