package com.foodwaste.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityRateLimitFilter implements Filter {

    private final IpAbuseGuard ipAbuseGuard;
    private final RateLimitingService rateLimitingService;

    public SecurityRateLimitFilter(IpAbuseGuard ipAbuseGuard, RateLimitingService rateLimitingService) {
        this.ipAbuseGuard = ipAbuseGuard;
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Skip static resources
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/")
                || uri.endsWith(".ico") || uri.endsWith(".png") || uri.endsWith(".svg")
                || uri.endsWith(".woff") || uri.endsWith(".woff2")) {
            chain.doFilter(req, res);
            return;
        }

        String ip = ClientIpUtil.getClientIp(request);

        // 1. IP Hard-Block Check
        if (ipAbuseGuard.isBlocked(ip)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Your IP has been temporarily blocked due to excessive requests. Contact admin.\"}");
            return;
        }

        // 2. Record request for IP abuse tracking
        ipAbuseGuard.recordRequest(ip);

        // 3. Token Bucket Rate Limiting
        RateLimitTier tier = determineTier(uri, method);
        boolean allowed = rateLimitingService.tryConsume(ip, tier);

        if (!allowed) {
            response.setStatus(429); // 429 Too Many Requests
            response.setHeader("Retry-After", String.valueOf(tier.getRefillDuration().toSeconds()));

            if (uri.startsWith("/api/")) {
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
            } else {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write("<!DOCTYPE html><html><head><title>Too Many Requests</title>"
                        + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                        + "<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'></head>"
                        + "<body class='bg-light d-flex align-items-center justify-content-center min-vh-100'>"
                        + "<div class='card p-4 text-center shadow-sm' style='max-width: 450px;'>"
                        + "<h3 class='text-danger mb-3'>Too Many Requests</h3>"
                        + "<p class='text-muted'>You have made too many requests in a short period. Please wait a minute and try again.</p>"
                        + "<a href='/login' class='btn btn-primary mt-2'>Back to Login</a>"
                        + "</div></body></html>");
            }
            return;
        }

        chain.doFilter(req, res);
    }

    private RateLimitTier determineTier(String uri, String method) {
        if ("POST".equalsIgnoreCase(method)) {
            if ("/login".equals(uri) || "/register".equals(uri)) {
                return RateLimitTier.AUTH;
            }
            if ("/forgot-password".equals(uri) || "/verify-otp".equals(uri) || "/reset-password".equals(uri)) {
                return RateLimitTier.OTP;
            }
        }
        if (uri.startsWith("/api/")) {
            return RateLimitTier.API;
        }
        return RateLimitTier.GLOBAL;
    }
}
