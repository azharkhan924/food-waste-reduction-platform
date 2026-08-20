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

    public SecurityRateLimitFilter(IpAbuseGuard ipAbuseGuard) {
        this.ipAbuseGuard = ipAbuseGuard;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();

        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/")
                || uri.endsWith(".ico") || uri.endsWith(".png") || uri.endsWith(".svg")
                || uri.endsWith(".woff") || uri.endsWith(".woff2")) {
            chain.doFilter(req, res);
            return;
        }

        String ip = ClientIpUtil.getClientIp(request);

        if (ipAbuseGuard.isBlocked(ip)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<!DOCTYPE html><html><head><title>Blocked</title>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                    + "<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'></head>"
                    + "<body class='bg-light d-flex align-items-center justify-content-center min-vh-100'>"
                    + "<div class='card p-4 text-center shadow-sm' style='max-width: 450px;'>"
                    + "<h3 class='text-danger mb-3'>IP Blocked</h3>"
                    + "<p class='text-muted'>Your IP has been temporarily blocked due to too many requests. "
                    + "Please try again after 3 hours or contact admin.</p>"
                    + "<a href='/login' class='btn btn-primary mt-2'>Back to Login</a>"
                    + "</div></body></html>");
            return;
        }

        ipAbuseGuard.recordRequest(ip);

        chain.doFilter(req, res);
    }
}
