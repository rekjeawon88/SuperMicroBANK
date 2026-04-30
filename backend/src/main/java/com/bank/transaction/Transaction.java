package com.bank.transaction;

import com.bank.account.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_tx_from_account", columnList = "from_account_id"),
                @Index(name = "idx_tx_to_account", columnList = "to_account_id")
        }
)
@Check(constraints = "amount > 0")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transactionsSeqGenerator")
    @SequenceGenerator(name = "transactionsSeqGenerator", sequenceName = "TRANSACTIONS_SEQ", allocationSize = 1)
    // Oracle은 ID 생성 시 SEQUENCE 전략을 사용한다.
    // Tibero는 운영 환경 정책에 맞춰 IDENTITY 또는 SEQUENCE를 선택할 수 있다.
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
