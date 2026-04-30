package com.bank.transaction.repository;

import com.bank.transaction.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(
            value = """
                    SELECT *
                    FROM transactions
                    WHERE from_account_id = :accountId
                       OR to_account_id = :accountId
                    ORDER BY created_at DESC, id DESC
                    """,
            nativeQuery = true
    )
    List<Transaction> findAllByAccountIdOrderByLatest(@Param("accountId") Long accountId);
}
