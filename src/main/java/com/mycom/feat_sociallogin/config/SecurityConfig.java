package com.mycom.feat_sociallogin.config;

import com.mycom.feat_sociallogin.filter.CustomLogoutFilter;
import com.mycom.feat_sociallogin.filter.JWTFilter;
import com.mycom.feat_sociallogin.oauth2.CustomOAuth2UserService;
import com.mycom.feat_sociallogin.oauth2.CustomSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;

    private final JWTFilter jwtFilter;
    private final CustomLogoutFilter customLogoutFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (REST API 스타일에서는 보통 사용하지 않음)
                .csrf(csrf -> csrf.disable())
                // 폼 로그인 비활성화 (소셜 로그인만 사용)
                .formLogin(formLogin -> formLogin.disable())
                // HTTP 기본 인증 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())
                // CORS 설정
                .cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(Collections.singletonList("http://localhost:8080")); // React 프론트엔드 URL
                    configuration.setAllowedMethods(Collections.singletonList("*")); // 모든 HTTP 메서드 허용
                    configuration.setAllowCredentials(true); // 쿠키 허용
                    configuration.setAllowedHeaders(Collections.singletonList("*")); // 모든 헤더 허용
                    configuration.setMaxAge(3600L); // CORS 캐싱 시간 설정
                    return configuration;
                }))
                // 소셜 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)) // 사용자 정보 처리
                        .successHandler(customSuccessHandler)) // 로그인 성공 시 처리
                // JWT 인증 필터 추가
                // .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // 로그아웃 필터 추가
                // .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class)
                .addFilterBefore(customLogoutFilter, LogoutFilter.class)
                // 권한별 요청 처리
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/reissue","/","/oauth2/authorization/kakao","/oauth2/authorization/google").permitAll() // 토큰 재발급은 인증 없이 접근 가능
                        .anyRequest().authenticated()) // 그 외의 요청은 인증 필요
                // 세션 정책: Stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
