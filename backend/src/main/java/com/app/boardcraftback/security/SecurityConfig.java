package com.app.boardcraftback.security;

import com.app.boardcraftback.security.service.AppUserDetailsService;
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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

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
    public AuthenticationSuccessHandler loginSuccessHandler() {
        var base = new SavedRequestAwareAuthenticationSuccessHandler();
        base.setTargetUrlParameter("redirect");
        base.setDefaultTargetUrl("/");
        base.setAlwaysUseDefaultTargetUrl(false);
        base.setUseReferer(false);

        return (request, response, authentication) -> {
            var cache = new HttpSessionRequestCache();
            var saved = cache.getRequest(request, response);

            if (saved != null) { // 보호 리소스에서 튕겨온 케이스
                base.onAuthenticationSuccess(request, response, authentication);
                return;
            }

            // 직접 /auth/signin에서 로그인한 케이스 처리
            String redirect = request.getParameter("redirect");
            if (redirect != null && redirect.startsWith("/") && !redirect.startsWith("//")
                    && !redirect.startsWith("/auth/signin")) {
                response.sendRedirect(redirect);
            } else {
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

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
                        .anyRequest().authenticated()
                )

                .formLogin(login -> login
                        .loginPage("/auth/signin")
                        .loginProcessingUrl("/auth/signin")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler())
                        .failureUrl("/auth/signin?error=true")
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
