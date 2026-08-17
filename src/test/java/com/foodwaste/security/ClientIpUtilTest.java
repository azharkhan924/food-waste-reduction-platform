package com.foodwaste.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientIpUtilTest {

    @Test
    void testGetClientIp_NullRequest() {
        assertEquals("127.0.0.1", ClientIpUtil.getClientIp(null));
    }

    @Test
    void testGetClientIp_FromXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");

        assertEquals("203.0.113.195", ClientIpUtil.getClientIp(request));
    }

    @Test
    void testGetClientIp_FromXRealIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "198.51.100.42");

        assertEquals("198.51.100.42", ClientIpUtil.getClientIp(request));
    }

    @Test
    void testGetClientIp_FromRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.1");

        assertEquals("192.0.2.1", ClientIpUtil.getClientIp(request));
    }
}
