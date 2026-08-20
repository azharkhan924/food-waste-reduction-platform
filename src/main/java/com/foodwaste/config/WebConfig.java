package com.foodwaste.config;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:8080",
                        "http://localhost:3000",
                        "https://food-waste-reduction-platform-production.up.railway.app"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            private final SecureRandom secureRandom = new SecureRandom();

            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                byte[] nonceBytes = new byte[16];
                secureRandom.nextBytes(nonceBytes);
                String nonce = Base64.getEncoder().encodeToString(nonceBytes);
                request.setAttribute("cspNonce", nonce);

                String csp = "default-src 'self'; "
                        + "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://unpkg.com https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; "
                        + "style-src 'self' 'unsafe-inline' https://unpkg.com https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://fonts.googleapis.com; "
                        + "img-src 'self' data: blob: https://*.tile.openstreetmap.org https://raw.githubusercontent.com https://cdnjs.cloudflare.com; "
                        + "connect-src 'self' https://router.project-osrm.org https://nominatim.openstreetmap.org; "
                        + "font-src 'self' data: https://fonts.gstatic.com https://unpkg.com https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; "
                        + "base-uri 'self'; "
                        + "form-action 'self'; "
                        + "frame-ancestors 'none'; "
                        + "object-src 'none'";

                response.setHeader("Content-Security-Policy", csp);
                return true;
            }
        });
    }
}
