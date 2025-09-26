package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InitController implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("========================================");
        System.out.println("코드배틀아레나 시스템 시작");
        System.out.println("========================================");
        
        createTestAccounts();
        
        long userCount = userRepository.count();
        System.out.println("현재 등록된 사용자 수: " + userCount + "명");
        System.out.println("========================================");
    }
    
    private void createTestAccounts() {
        // 관리자 계정 생성
        if (!userRepository.findByEmail("admin@test.com").isPresent()) {
            User admin = new User();
            admin.setName("관리자");
            admin.setEmail("admin@test.com");
            admin.setPassword("1234");
            admin.setRole(UserRole.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setOnlineStatus(false);
            userRepository.save(admin);
            System.out.println("✅ 관리자 계정 생성: admin@test.com / 1234");
        }
        
        // 강사 계정 생성
        if (!userRepository.findByEmail("teacher@test.com").isPresent()) {
            User teacher = new User();
            teacher.setName("김강사");
            teacher.setEmail("teacher@test.com");
            teacher.setPassword("1234");
            teacher.setRole(UserRole.TEACHER);
            teacher.setCreatedAt(LocalDateTime.now());
            teacher.setOnlineStatus(false);
            userRepository.save(teacher);
            System.out.println("✅ 강사 계정 생성: teacher@test.com / 1234");
        }
        
        // 학생 계정 생성
        if (!userRepository.findByEmail("student@test.com").isPresent()) {
            User student = new User();
            student.setName("김학생");
            student.setEmail("student@test.com");
            student.setPassword("1234");
            student.setRole(UserRole.STUDENT);
            student.setCreatedAt(LocalDateTime.now());
            student.setOnlineStatus(false);
            userRepository.save(student);
            System.out.println("✅ 학생 계정 생성: student@test.com / 1234");
        }
        
        // 원래 요청한 관리자 계정도 생성
        if (!userRepository.findByEmail("oicrcutie@gmail.com").isPresent()) {
            User originalAdmin = new User();
            originalAdmin.setName("원관리자");
            originalAdmin.setEmail("oicrcutie@gmail.com");
            originalAdmin.setPassword("aa667788!!");
            originalAdmin.setRole(UserRole.ADMIN);
            originalAdmin.setCreatedAt(LocalDateTime.now());
            originalAdmin.setOnlineStatus(false);
            userRepository.save(originalAdmin);
            System.out.println("✅ 원 관리자 계정 생성: oicrcutie@gmail.com / aa667788!!");
        }
    }
}
