package com.bank.account.repository;

import com.bank.account.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserId(Long userId);

    Optional<Account> findByAccountNumber(String accountNumber);

    // [변경 전] findByAccountNumber().isPresent()로 중복 확인 시
    // 해당 계좌 엔티티 전체를 SELECT한 뒤 존재 여부만 판단했음.
    // 불필요한 컬럼 데이터까지 조회하는 비효율이 있었음.
    //
    // [변경 후] EXISTS 쿼리로 존재 여부만 확인하여 불필요한 데이터 전송 제거.
    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.accountNumber = :accountNumber")
    boolean existsByAccountNumber(@Param("accountNumber") String accountNumber);
}
