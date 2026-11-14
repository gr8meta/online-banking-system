package com.bankingsystem.repository;

import com.bankingsystem.entity.Transaction;
import com.bankingsystem.entity.enums.TransactionStatus;
import com.bankingsystem.entity.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t WHERE t.senderAccount.accountId = :accId OR t.receiverAccount.accountId = :accId ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountId(@Param("accId") Long accId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.receiverAccount.accountId = :accId AND t.transactionType = 'DEPOSIT' AND t.status = 'SUCCESS'")
    BigDecimal sumDepositsByAccount(@Param("accId") Long accId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.senderAccount.accountId = :accId AND (t.transactionType = 'WITHDRAWAL' OR t.transactionType = 'TRANSFER') AND t.status = 'SUCCESS'")
    BigDecimal sumWithdrawalsByAccount(@Param("accId") Long accId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionType = :type AND t.status = 'SUCCESS'")
    BigDecimal sumAllByType(@Param("type") TransactionType type);

    long countByStatus(TransactionStatus status);
}
