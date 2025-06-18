package com.javaservice.MyService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.javaservice.MyService.Entity.Transaction_logs;
@Repository
public interface TransactionlogRepository extends JpaRepository<Transaction_logs,Long>{

}
