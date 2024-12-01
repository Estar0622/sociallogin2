package com.mycom.feat_sociallogin.config;

import com.mycom.feat_sociallogin.interceptor.AuthorizationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthorizationInterceptor authorizationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /*
         * TODO: 로그인 체크 인터셉터 등록 (사실 이 역할을 SecurityConfig 에서 처리하므로, 이 예제에서는 사용하지 않음)
         *  cf. .authorizeHttpRequests(auth -> auth
         *              // (...)
         *              .anyRequest().authenticated()) // 그 외의 요청은 인증 필요
         */
        registry.addInterceptor(authorizationInterceptor)   // 인터셉터 등록 (여러 인터셉터 등록이 가능하며, 필요 시 순서 조정도 가능)
                .addPathPatterns("/**")                     // 인터셉터 적용 경로
                .excludePathPatterns("/");                  // 인터셉터 제외 경로
    }

}
