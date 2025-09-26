-- 데이터베이스가 없으면 생성
SELECT 'CREATE DATABASE codebattlearena'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'codebattlearena')\gexec
