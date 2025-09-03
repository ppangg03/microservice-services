package com.javaservice.Service.Repository;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.javaservice.Service.Entity.Transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

	public Optional<Transaction> findById(Long transactionId);

	@Query("SELECT t FROM Transaction t " +
		       "WHERE t.transaction_state = :transaction_state " +
		       "AND TO_TIMESTAMP(t.timestamp, 'YYYY-MM-DD HH24:MI:SS') <= :yesterdayEnd " +
		       "AND t.type = 'TRANSFER'")
	List<Transaction> findByTransactionStateAndCreatedAtBetween(
		    @Param("transaction_state") String transaction_state,
		    @Param("yesterdayEnd") LocalDateTime yesterdayEnd
	);

//	@Query("SELECT t FROM Transaction t WHERE t.transaction_state = :transaction_state AND t.timestamp <= :yesterdayEnd AND t.type = 'TRANSFER'")
//	List<Transaction> findByTransactionStateAndCreatedAtBetween(@Param("transaction_state") String transaction_state,@Param("yesterdayEnd") String yesterdayEnd);

}
