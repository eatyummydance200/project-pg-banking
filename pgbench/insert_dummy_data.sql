-- =========================================================================
-- pgbench 테스트를 위한 bank_user 및 account 더미 데이터 초기화 스크립트
-- =========================================================================

-- 1. bank_user 테이블 더미 데이터 삽입 (50명)
-- 이미 해당 ID가 존재할 경우 업데이트를 수행(UPSERT)합니다.
INSERT INTO bank_user (id, login_id, name, created_at)
SELECT 
    i, 
    'user_' || LPAD(i::text, 3, '0'), 
    'User_' || LPAD(i::text, 3, '0'), 
    NOW()
FROM generate_series(1, 50) AS i
ON CONFLICT (id) DO UPDATE 
SET login_id = EXCLUDED.login_id, name = EXCLUDED.name;

-- 2. account 테이블 더미 데이터 삽입 (50개, 각 계좌당 잔액 10,000,000원)
-- 이미 해당 ID가 존재할 경우 잔액과 상태, 버전을 리셋합니다.
INSERT INTO account (id, user_id, account_number, balance, status, version, created_at)
SELECT 
    i, 
    i, 
    'ACC-' || LPAD(i::text, 5, '0'), 
    10000000.00, 
    'ACTIVE', 
    0, 
    NOW()
FROM generate_series(1, 50) AS i
ON CONFLICT (id) DO UPDATE 
SET balance = EXCLUDED.balance, status = EXCLUDED.status, version = EXCLUDED.version;
