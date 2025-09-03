package com.javaservice.Service.Repository;


import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.javaservice.Service.Entity.Transaction_logs;

@Repository
public interface TransactionlogRepository extends JpaRepository<Transaction_logs,Long>{
	
	@Query("SELECT tl FROM Transaction_logs tl order by tl.logs_id DESC ,tl.transaction_id DESC LIMIT 30")
	List<Transaction_logs> findAllOrderByIdDesc();
	
	@Query("SELECT tl FROM Transaction_logs tl WHERE tl.account.accountid = :account_id ORDER BY tl.logs_id DESC ,tl.transaction_id DESC LIMIT 30")
	List<Transaction_logs> findByAccountIdOrderByIdDesc(@Param("account_id") Long account_id);
	
	@Query("SELECT tl FROM Transaction_logs tl WHERE tl.account.accountid = :account_id AND tl.timestamp LIKE %:date% ORDER BY tl.logs_id DESC ,tl.transaction_id DESC")
	List<Transaction_logs> findByAccountIdandDate(@Param("account_id") Long account_id,@Param("date") String date);
	
	@Query("SELECT tl FROM Transaction_logs tl WHERE tl.account.accountid = :account_id AND tl.amount LIKE %:amount% ORDER BY tl.logs_id DESC ,tl.transaction_id DESC LIMIT 30")
	List<Transaction_logs> findByAccountIdandAmount(@Param("account_id") Long account_id ,@Param("amount") String amount);
	
	
}
