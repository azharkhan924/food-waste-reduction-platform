package com.foodwaste.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5500")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                String csp = "default-src 'self'; "
                        + "script-src 'self' 'unsafe-inline' https://unpkg.com https://cdn.jsdelivr.net; "
                        + "style-src 'self' 'unsafe-inline' https://unpkg.com; "
                        + "img-src 'self' data: https://*.tile.openstreetmap.org; "
                        + "connect-src 'self'; "
                        + "font-src 'self'; "
                        + "frame-ancestors 'none'; "
                        + "object-src 'none'";

                response.setHeader("Content-Security-Policy", csp);
                return true;
            }
        });
    }
}
