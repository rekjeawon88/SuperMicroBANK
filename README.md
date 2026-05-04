# 🏦 SuperMicroBANK

> 금융권 도메인 이해를 목적으로 구현한 소규모 은행 거래 시스템입니다.  
> Oracle DB 기반의 계좌 관리, 입출금, 계좌이체 기능을 Spring Boot + React로 구현했습니다.

<br>

## 📌 프로젝트 개요

| 항목 | 내용 |
|---|---|
| **목적** | 금융권 핵심 도메인(계좌, 거래, 이체) 직접 구현을 통한 금융 시스템 이해 |
| **핵심 기술** | Oracle DB, Spring Transaction, JPA, REST API |
| **기간** | 2026.04.27 ~ 2026.05.04 |

### 구현 기능

- **회원** — 회원가입 / 로그인
- **계좌** — 계좌 생성 / 단건 조회 / 사용자별 계좌 목록 조회
- **거래** — 입금 / 출금 / 계좌번호 기반 계좌이체 / 거래내역 조회

<br>

## 🛠 기술 스택

### Backend

| 기술 | 버전 | 선택 이유 |
|---|---|---|
| Java | 21 | LTS 버전, Record 타입·가상 스레드 등 최신 문법 활용 |
| Spring Boot | 3.5.14 | 금융권 백엔드 주력 프레임워크 |
| Spring Data JPA | 3.5.14 | ORM을 통한 엔티티-테이블 매핑, JPQL 쿼리 관리 |
| Oracle DB (XE) | 21c | 실제 금융권 운영 환경에서 가장 많이 사용되는 RDBMS |
| ojdbc11 | 21.x | Oracle JDBC 드라이버 |
| Lombok | 최신 | 보일러플레이트 코드 제거 |
| SpringDoc OpenAPI | 2.7.0 | Swagger UI 자동 생성 |
| H2 (테스트 전용) | 최신 | Oracle 없이 통합 테스트 실행 가능한 인메모리 DB |
| JUnit 5 | 5.x | 단위 테스트 / 통합 테스트 |
| Mockito | 최신 | Service 레이어 단위 테스트용 Mock 프레임워크 |
| Gradle | 8.x | 빌드 도구 |

### Frontend

| 기술 | 버전 | 선택 이유 |
|---|---|---|
| React | 19.2.5 | SPA 구성, 상태 기반 UI |
| TypeScript | 6.0.2 | 타입 안정성 확보 |
| Vite | 8.0.10 | 빠른 개발 서버 및 번들링 |
| Tailwind CSS | 4.1.11 | 유틸리티 기반 스타일링 |
| Framer Motion | 12.38.0 | UI 애니메이션 |
| Lucide React | 0.553.0 | 아이콘 라이브러리 |
| React Router DOM | 7.14.2 | 클라이언트 사이드 라우팅 |

<br>

## 💡 기술적 의사결정

### Oracle DB + SEQUENCE 전략

금융권에서 가장 널리 사용되는 Oracle DB를 직접 채택했습니다. JPA의 ID 생성 전략으로 Oracle 표준인 `SEQUENCE` + `@SequenceGenerator`를 사용했으며, `allocationSize = 1`로 설정해 순번의 연속성과 예측 가능성을 확보했습니다.

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accountsSeqGenerator")
@SequenceGenerator(name = "accountsSeqGenerator", sequenceName = "ACCOUNTS_SEQ", allocationSize = 1)
private Long id;
```

### Spring Transaction 관리

계좌이체는 출금과 입금이 하나의 원자적 단위로 처리되어야 합니다. `@Transactional`을 적용해 두 잔액 변경이 모두 성공하거나 모두 롤백되도록 보장했습니다.

```java
@Transactional
public Transaction transfer(Long fromAccountId, String toAccountNumber, Long amount) {
    // 출금 계좌 잔액 감소
    fromAccount.decreaseBalance(amount);
    // 입금 계좌 잔액 증가
    toAccount.increaseBalance(amount);
    // 거래 내역 저장
    return transactionRepository.save(Transaction.createTransfer(fromAccount, toAccount, amount));
}
```

- 조회 메서드는 `@Transactional(readOnly = true)`로 분리해 불필요한 dirty checking 비용을 제거했습니다.
- LAZY 로딩된 연관 엔티티가 트랜잭션 범위 밖에서 접근되는 `LazyInitializationException`을 방지하기 위해 Controller 레이어에도 트랜잭션 범위를 명시했습니다.

### DB 레벨 데이터 정합성

애플리케이션 레이어의 검증 외에도, DB 레벨에서 거래 금액이 항상 양수임을 보장하는 CHECK 제약 조건을 추가했습니다.

```java
@Check(constraints = "amount > 0")
public class Transaction { ... }
```

### 계좌번호 생성

`SecureRandom`을 사용해 9자리 고유 계좌번호를 생성하며, `000000000`과 같이 의미 없는 번호를 방지하기 위해 생성 범위를 `1 ~ 999,999,999`로 제한했습니다. 중복 발생 시 최대 20회 재시도하는 방어 로직을 포함했습니다.

### 테스트 전략

- **단위 테스트 (26개)** — Mockito로 Repository를 Mock 처리하여 Spring Context 없이 Service 레이어만 독립 검증
- **통합 테스트 (20개)** — H2 인메모리 DB(Oracle 호환 모드)와 `@SpringBootTest`로 Controller → Service → Repository 전 계층 실제 관통 검증

<br>

## 📁 폴더 구조

```
SuperMicroBANK/
├── backend/                          # Spring Boot 백엔드
│   └── src/
│       ├── main/
│       │   ├── java/com/bank/
│       │   │   ├── SmbankApplication.java
│       │   │   ├── global/
│       │   │   │   └── config/
│       │   │   │       ├── CorsConfig.java         # CORS 설정
│       │   │   │       └── OpenApiConfig.java       # Swagger 설정
│       │   │   ├── user/
│       │   │   │   ├── User.java                   # 사용자 엔티티
│       │   │   │   ├── controller/UserController.java
│       │   │   │   ├── service/UserService.java
│       │   │   │   ├── repository/UserRepository.java
│       │   │   │   └── dto/
│       │   │   ├── account/
│       │   │   │   ├── Account.java                # 계좌 엔티티
│       │   │   │   ├── controller/AccountController.java
│       │   │   │   ├── service/AccountService.java
│       │   │   │   ├── repository/AccountRepository.java
│       │   │   │   └── dto/
│       │   │   └── transaction/
│       │   │       ├── Transaction.java            # 거래 엔티티
│       │   │       ├── controller/TransactionController.java
│       │   │       ├── service/TransactionService.java
│       │   │       ├── repository/TransactionRepository.java
│       │   │       └── dto/
│       │   └── resources/
│       │       └── application.yaml               # Oracle DB 설정
│       └── test/
│           ├── java/com/bank/
│           │   ├── user/service/UserServiceTest.java
│           │   ├── user/controller/UserControllerIntegrationTest.java
│           │   ├── account/service/AccountServiceTest.java
│           │   ├── account/controller/AccountControllerIntegrationTest.java
│           │   ├── transaction/service/TransactionServiceTest.java
│           │   └── transaction/controller/TransactionControllerIntegrationTest.java
│           └── resources/
│               └── application.yaml               # H2 테스트 DB 설정
│
├── frontend/                          # React 프론트엔드
│   ├── src/
│   │   ├── main.tsx
│   │   ├── App.tsx                    # 메인 화면 (인증 / 대시보드)
│   │   ├── index.css                  # Tailwind 전역 스타일
│   │   └── services/
│   │       ├── api-client.ts          # 백엔드 API 호출 모듈
│   │       └── types.ts               # 공통 타입 정의
│   ├── lib/
│   │   └── utils.ts                   # cn() 유틸리티
│   ├── hooks/
│   │   └── use-mobile.ts
│   ├── public/
│   ├── index.html
│   ├── vite.config.ts
│   └── package.json
│
└── README.md
```

<br>

## ⚙️ 환경 설정

### 사전 요구사항

| 도구 | 버전 |
|---|---|
| JDK | 21 이상 |
| Oracle DB XE | 21c |
| Node.js | 20 이상 |
| npm | 10 이상 |

### Oracle DB 설정

Oracle XE 설치 후 아래 계정과 스키마를 생성합니다.

```sql
CREATE USER smbank IDENTIFIED BY smbank;
GRANT CONNECT, RESOURCE TO smbank;
GRANT UNLIMITED TABLESPACE TO smbank;
```

> 테이블과 시퀀스는 `spring.jpa.hibernate.ddl-auto: create` 설정으로 애플리케이션 실행 시 자동 생성됩니다.

<br>

## 🚀 실행 방법

### Backend

```bash
cd SuperMicroBANK/backend

# Windows
.\gradlew bootRun

# macOS / Linux
./gradlew bootRun
```

> 백엔드 서버: `http://localhost:8080`

### Frontend

```bash
cd SuperMicroBANK/frontend

# 패키지 설치 (최초 1회)
npm install

# 개발 서버 실행
npm run dev
```

> 프론트엔드 서버: `http://localhost:5173`

### 테스트 실행

```bash
cd SuperMicroBANK/backend

# 전체 테스트 실행 (단위 26개 + 통합 20개)
.\gradlew test

# 결과 리포트 확인
# build/reports/tests/test/index.html
```

> 테스트는 Oracle 연결 없이 H2 인메모리 DB로 실행됩니다.

<br>

## 📋 API 명세

### Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

### 엔드포인트 요약

| 메서드 | URL | 설명 |
|---|---|---|
| `POST` | `/users` | 회원가입 |
| `POST` | `/login` | 로그인 |
| `POST` | `/accounts` | 계좌 생성 |
| `GET` | `/accounts/{accountId}` | 계좌 단건 조회 |
| `GET` | `/users/{userId}/accounts` | 사용자 계좌 목록 조회 |
| `POST` | `/accounts/{accountId}/deposit` | 입금 |
| `POST` | `/accounts/{accountId}/withdraw` | 출금 |
| `POST` | `/transfer` | 계좌이체 (계좌번호 기반) |
| `GET` | `/accounts/{accountId}/transactions` | 거래내역 조회 |

<br>

## 🗄️ ERD

```
users
├── id            (NUMBER, PK, SEQUENCE)
├── email         (VARCHAR2, UNIQUE, NOT NULL)
├── password      (VARCHAR2, NOT NULL)
├── name          (VARCHAR2, NOT NULL)
└── created_at    (TIMESTAMP, NOT NULL)

accounts
├── id             (NUMBER, PK, SEQUENCE)
├── user_id        (NUMBER, FK → users.id, NOT NULL)
├── account_number (VARCHAR2, UNIQUE, NOT NULL)
├── balance        (NUMBER, NOT NULL, DEFAULT 0)
└── created_at     (TIMESTAMP, NOT NULL)

transactions
├── id              (NUMBER, PK, SEQUENCE)
├── from_account_id (NUMBER, FK → accounts.id, NULLABLE)
├── to_account_id   (NUMBER, FK → accounts.id, NULLABLE)
├── amount          (NUMBER, NOT NULL, CHECK > 0)
├── type            (VARCHAR2, NOT NULL)  -- DEPOSIT / WITHDRAW / TRANSFER
└── created_at      (TIMESTAMP, NOT NULL)
```
