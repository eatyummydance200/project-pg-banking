-- =========================================================================
-- pgbench 용 계좌 이체(송금) 시나리오 SQL (SERIALIZABLE 격리 수준)
-- =========================================================================
-- 1부터 50까지의 무작위 ID를 할당합니다. 
-- (테스트 실행 전 해당 ID 범위 내의 계좌 데이터가 실제로 등록되어 있어야 합니다)
\set from_id random(1, 50)
\set to_id random(1, 50)
\set amount 100

-- Serializable 격리 수준 트랜잭션 시작
-- Serializable 모드에서는 비관적 락(SELECT FOR UPDATE)을 사용하는 대신, 
-- 동시성 충돌 발생 시 PostgreSQL이 트랜잭션을 중단하고 Serialization Failure (SQLSTATE 40001)를 발생시킵니다.
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE;

-- 1. 잔액 및 계좌 상태 검증을 위한 조회 (FOR UPDATE 락을 걸지 않음)
-- Serializable 격리 수준의 충돌 감지를 명확히 테스트하기 위해 락을 사용하지 않습니다.
SELECT balance, status FROM account WHERE id = :from_id;
SELECT balance, status FROM account WHERE id = :to_id;

-- 2. 보내는 계좌와 받는 계좌가 다르고, 보내는 계좌의 잔액이 이체 금액 이상일 때만 출금 업데이트
UPDATE account 
SET balance = balance - :amount 
WHERE id = :from_id 
  AND :from_id != :to_id 
  AND balance >= :amount 
  AND status = 'ACTIVE';

-- 3. 상대 계좌가 활성화 상태이고 출금이 정상적으로 가능한 상태일 때만 입금 업데이트
UPDATE account 
SET balance = balance + :amount 
WHERE id = :to_id 
  AND :from_id != :to_id 
  AND EXISTS (
      SELECT 1 FROM account 
      WHERE id = :from_id 
        AND balance >= :amount 
        AND status = 'ACTIVE'
  )
  AND status = 'ACTIVE';

COMMIT;
