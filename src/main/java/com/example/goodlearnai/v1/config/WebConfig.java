package com.example.goodlearnai.v1.config;

import com.example.goodlearnai.v1.interceptor.JwtInterceptor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Spring MVC Web配置类
 * @author Mouse
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * JWT拦截器
     */
    @Resource
    private JwtInterceptor jwtInterceptor;
    
    /**
     * 文件上传基本路径
     */
    @Value("${file.upload.base-path}")
    private String fileUploadBasePath;
    
    /**
     * 文件访问URL前缀
     */
    @Value("${file.upload.access-url}")
    private String fileAccessUrl;

    /**
     * 添加JWT拦截器
     * 
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册JWT拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/v1/**")
                // 排除不需要验证的路径
                .excludePathPatterns(
                        // 用户登录、注册和密码找回接口
                        "/v1/users/login", 
                        "/v1/users/register", 
                        "/v1/users/forgot-password",
                        // 验证码相关接口
                        "/v1/verification-codes/**",
                        "/v1/captcha/generate",
                        // 学校信息查询接口
                        "/v1/schools/get-schools",
                        // 静态资源文件访问路径
                        fileAccessUrl + "**"
                );
    }

    /**
     * 添加静态资源处理器
     * 
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保文件上传目录以'/'结尾
        String normalizedPath = fileUploadBasePath;
        if (!normalizedPath.endsWith("/") && !normalizedPath.endsWith("\\")) {
            normalizedPath = normalizedPath + File.separator;
        }
        
        // 配置文件访问映射
        registry.addResourceHandler(fileAccessUrl + "**")
                .addResourceLocations("file:" + normalizedPath);
        
        // 保留原有的静态资源映射
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + File.separator + "images" + File.separator);
    }
}