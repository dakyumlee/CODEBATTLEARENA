-- 기존 사용자 삭제 후 재생성
DELETE FROM users WHERE email IN ('admin@test.com', 'teacher@test.com', 'student@test.com', 'oicrcutie@gmail.com');

-- 테스트 계정 생성 (평문 비밀번호)
INSERT INTO users (name, email, password, role, online_status, created_at, last_activity) VALUES
('관리자', 'admin@test.com', '1234', 'ADMIN', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('김강사', 'teacher@test.com', '1234', 'TEACHER', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('박학생', 'student@test.com', '1234', 'STUDENT', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('원관리자', 'oicrcutie@gmail.com', 'aa667788!!', 'ADMIN', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
