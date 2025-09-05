package com.app.boardcraftback.security;

import lombok.RequiredArgsConstructor;
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
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AppUserDetailsService userDetailsService;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/",
            "/auth/**",
            "/posts/**",
            "/api/v1/auth/signup",
            "/swagger-ui/index.html",
    };

    @Bean
    AuthenticationFailureHandler authFailureHandler() {
        return (req, res, ex) -> {
            ex.printStackTrace(); // 콘솔에도 남김
            res.sendRedirect("/auth/signin?error=" + ex.getClass().getSimpleName());
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirect");
        successHandler.setDefaultTargetUrl("/");
        successHandler.setAlwaysUseDefaultTargetUrl(false);
        successHandler.setUseReferer(true);

        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)

                .userDetailsService(userDetailsService)

                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스(정말 모두 허용)
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()


                        // CORS preflight 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 퍼블릭 엔드포인트
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // 그 외는 인증 필요
                        .anyRequest().permitAll()
                )

                .formLogin(login -> login
                        .loginPage("/auth/signin")
                        .loginProcessingUrl("/auth/signin")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureHandler(authFailureHandler())
                )

                .logout(logout -> logout
                        .logoutUrl("/auth/signout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
