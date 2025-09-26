package com.codebattlearena.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                // 공개 접근 허용
                .requestMatchers("/", "/api/auth/**", "/css/**", "/js/**", "/images/**").permitAll()
                
                // 학생 페이지: 모든 역할 접근 가능 (학생, 강사, 관리자)
                .requestMatchers("/student/**", "/api/student/**").permitAll()
                
                // 강사 페이지: 강사와 관리자만 접근 가능
                .requestMatchers("/teacher/**", "/api/teacher/**").permitAll()
                
                // 관리자 페이지: 관리자만 접근 가능
                .requestMatchers("/admin/**", "/api/admin/**").permitAll()
                
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
