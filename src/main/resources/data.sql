-- 기존 데이터 삭제 후 재삽입
DELETE FROM users WHERE email IN ('oicrcutie@gmail.com', 'student@test.com', 'teacher@test.com');

-- 관리자 계정
INSERT INTO users (name, email, password, role, online_status, created_at) 
VALUES ('관리자', 'oicrcutie@gmail.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN', false, CURRENT_TIMESTAMP);

-- 테스트 계정들
INSERT INTO users (name, email, password, role, online_status, created_at) 
VALUES 
('테스트학생', 'student@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', false, CURRENT_TIMESTAMP),
('테스트강사', 'teacher@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'TEACHER', false, CURRENT_TIMESTAMP);

-- 샘플 복습노트 데이터
DELETE FROM study_notes WHERE user_id = 1;
INSERT INTO study_notes (user_id, title, content, created_at, updated_at) 
VALUES 
(1, 'Java 기초 정리', '변수, 데이터 타입, 연산자에 대해 학습했습니다. int, String, boolean 등의 기본 타입과 참조 타입의 차이를 이해했습니다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '반복문 활용', 'for문과 while문의 차이점과 각각의 활용 사례를 정리했습니다. 배열과 함께 사용할 때 주의사항도 기록했습니다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '오늘 배운 알고리즘', '버블 정렬 알고리즘을 구현해보았습니다. 시간복잡도는 O(n²)이고, 인접한 원소들을 비교하여 정렬하는 방식입니다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
