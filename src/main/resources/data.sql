DELETE FROM users WHERE email = 'oicrcutie@gmail.com';

INSERT INTO users (name, email, password, role, online_status, created_at) VALUES 
('관리자', 'oicrcutie@gmail.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN', false, CURRENT_TIMESTAMP);

INSERT INTO users (name, email, password, role, online_status, created_at) VALUES 
('테스트학생', 'student@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', false, CURRENT_TIMESTAMP),
('테스트강사', 'teacher@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'TEACHER', false, CURRENT_TIMESTAMP);
