-- =========================================================================
-- pgbench 용 계좌 이체(송금) 시나리오 SQL
-- =========================================================================
-- 1부터 50까지의 무작위 ID를 할당합니다. 
-- (테스트 실행 전 해당 ID 범위 내의 계좌 데이터가 실제로 등록되어 있어야 합니다)
\set from_id random(1, 50)
\set to_id random(1, 50)
\set amount 100

BEGIN;

-- 1. 데드락(Deadlock) 방지를 위해 ID 오름차순으로 순차 락 획득 (SELECT FOR UPDATE)
SELECT * FROM account WHERE id = LEAST(:from_id, :to_id) FOR UPDATE;
SELECT * FROM account WHERE id = GREATEST(:from_id, :to_id) FOR UPDATE;

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
