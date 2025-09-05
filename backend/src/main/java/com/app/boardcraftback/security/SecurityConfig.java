package com.app.boardcraftback.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/",                 // 홈
            "/auth/**",          // (폼 화면 등)
            "/posts/**",         // 게시글 공개 영역
            "/api/v1/auth/signup"// 회원가입 API 열기
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스(정말 모두 허용)
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                        // CORS preflight 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 퍼블릭 엔드포인트
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // 그 외는 인증 필요
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
