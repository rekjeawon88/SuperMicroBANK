package com.bank.transaction.repository;

import com.bank.transaction.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // [변경 전] nativeQuery로 Transaction 목록만 가져오면,
    // TransactionDetailResponse.from()에서 getFromAccount().getId(), getToAccount().getId() 호출 시
    // LAZY 로딩으로 인해 Transaction N건에 대해 최대 2N번의 추가 SELECT가 발생하는 N+1 문제가 있었음.
    //
    // [변경 후] JPQL fetch join으로 fromAccount, toAccount를 한 번의 쿼리로 함께 조회.
    // LEFT JOIN FETCH를 사용해 fromAccount 또는 toAccount가 null인 경우(입금/출금)도 정상 처리.
    @Query("""
            SELECT t FROM Transaction t
            LEFT JOIN FETCH t.fromAccount
            LEFT JOIN FETCH t.toAccount
            WHERE t.fromAccount.id = :accountId
               OR t.toAccount.id = :accountId
            ORDER BY t.createdAt DESC, t.id DESC
            """)
    List<Transaction> findAllByAccountIdOrderByLatest(@Param("accountId") Long accountId);
}
