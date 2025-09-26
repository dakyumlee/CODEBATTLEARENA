-- 테이블이 있는지 확인하고 데이터 삽입
INSERT INTO users (name, email, password, role, created_at, online_status) 
VALUES ('관리자', 'oicrcutie@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRJpart4nrY15y.GYJqhH9i2ODbNRvHRkVN.Vm', 'ADMIN', CURRENT_TIMESTAMP, false)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (name, email, password, role, created_at, online_status) 
VALUES ('김강사', 'teacher@test.com', '$2a$10$N9qo8uLOickgx2ZMRJpart4nrY15y.GYJqhH9i2ODbNRvHRkVN.Vm', 'TEACHER', CURRENT_TIMESTAMP, false)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (name, email, password, role, created_at, online_status) 
VALUES ('김학생', 'student@test.com', '$2a$10$N9qo8uLOickgx2ZMRJpart4nrY15y.GYJqhH9i2ODbNRvHRkVN.Vm', 'STUDENT', CURRENT_TIMESTAMP, false)
ON CONFLICT (email) DO NOTHING;
