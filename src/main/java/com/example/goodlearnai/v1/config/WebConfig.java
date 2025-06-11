package com.example.goodlearnai.v1.config;




import com.example.goodlearnai.v1.interceptor.JwtInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Mouse
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/v1/**")
                .excludePathPatterns("/v1/users/login","/v1/users/register", "/v1/verification-codes/**","/v1/schools/get-schools","/v1/users/forgot-password");
    }
}