package com.foodwaste.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityRateLimitFilterTest {

    @Mock
    private IpAbuseGuard ipAbuseGuard;

    @Mock
    private RateLimitingService rateLimitingService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private SecurityRateLimitFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void testDoFilter_StaticResource_BypassesRateLimiting() throws ServletException, IOException {
        request.setRequestURI("/css/style.css");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(ipAbuseGuard, never()).isBlocked(any());
    }

    @Test
    void testDoFilter_NormalAllowedRequest() throws ServletException, IOException {
        request.setRequestURI("/login");
        request.setMethod("GET");
        when(ipAbuseGuard.isBlocked(any())).thenReturn(false);
        when(rateLimitingService.tryConsume(any(), eq(RateLimitTier.GLOBAL))).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(ipAbuseGuard, times(1)).recordRequest(any());
    }

    @Test
    void testDoFilter_BlockedIp_Returns403Forbidden() throws ServletException, IOException {
        request.setRequestURI("/login");
        request.setMethod("POST");
        when(ipAbuseGuard.isBlocked(any())).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("Forbidden"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilter_RateLimitExceeded_Api_Returns429Json() throws ServletException, IOException {
        request.setRequestURI("/api/donations/pending");
        request.setMethod("GET");
        when(ipAbuseGuard.isBlocked(any())).thenReturn(false);
        when(rateLimitingService.tryConsume(any(), eq(RateLimitTier.API))).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertEquals(429, response.getStatus());
        assertTrue(response.getContentAsString().contains("Too Many Requests"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilter_RateLimitExceeded_WebHtml_Returns429Html() throws ServletException, IOException {
        request.setRequestURI("/login");
        request.setMethod("POST");
        when(ipAbuseGuard.isBlocked(any())).thenReturn(false);
        when(rateLimitingService.tryConsume(any(), eq(RateLimitTier.AUTH))).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertEquals(429, response.getStatus());
        assertTrue(response.getContentAsString().contains("<!DOCTYPE html>"));
        verify(filterChain, never()).doFilter(request, response);
    }
}
