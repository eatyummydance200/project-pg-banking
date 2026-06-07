#!/bin/bash

# =========================================================================
# pgbench 동시성 부하 테스트 자동화 스크립트 (SERIALIZABLE 격리 수준)
# 스크립트 파일이 있는 디렉토리로 이동 (상대 경로의 sql 파일을 찾기 위함)
cd "$(dirname "$0")"

# 1. 데이터베이스 연결 정보 설정
DB_HOST="localhost"
DB_PORT="5432"
DB_USER="taeyeong"
DB_NAME="web_banking"
export PGPASSWORD="1234" # 비밀번호 입력 생략을 위한 환경변수 설정

# 2. 테스트 설정 (SERIALIZABLE 격리 수준 시나리오 파일)
SQL_FILE="pgbench_transfer_serializable.sql"
DURATION=60

echo "=========================================================="
echo " Starting pgbench Banking Load Tests (SERIALIZABLE Mode)"
echo " Duration: ${DURATION}s"
echo "=========================================================="
echo "Target Database: ${DB_NAME} on ${DB_HOST}:${DB_PORT}"
echo "SQL Scenario: ${SQL_FILE}"
echo "=========================================================="

# --------------------------------------------------------
# 시나리오 1: 10 VUs
# --------------------------------------------------------
echo ""
echo "[1/3] Running test with 10 Clients (2 threads)..."
echo "--------------------------------------------------------"
pgbench -h $DB_HOST -p $DB_PORT -U $DB_USER -c 10 -j 4 -T $DURATION -f $SQL_FILE $DB_NAME
sleep 3

# --------------------------------------------------------
# 시나리오 2: 50 VUs
# --------------------------------------------------------
echo ""
echo "[2/3] Running test with 50 Clients (4 threads)..."
echo "--------------------------------------------------------"
pgbench -h $DB_HOST -p $DB_PORT -U $DB_USER -c 50 -j 4 -T $DURATION -f $SQL_FILE $DB_NAME
sleep 3

# --------------------------------------------------------
# 시나리오 3: 100 VUs
# --------------------------------------------------------
echo ""
echo "[3/3] Running test with 100 Clients (8 threads)..."
echo "--------------------------------------------------------"
pgbench -h $DB_HOST -p $DB_PORT -U $DB_USER -c 100 -j 8 -T $DURATION -f $SQL_FILE $DB_NAME

echo ""
echo "=========================================================="
echo " All SERIALIZABLE pgbench load tests completed."
echo "=========================================================="
