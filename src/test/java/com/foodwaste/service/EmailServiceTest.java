package com.foodwaste.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void testSendOtp() {
        emailService.sendOtp("user@test.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertArrayEquals(new String[]{"user@test.com"}, msg.getTo());
        assertEquals("OTP for Password Reset", msg.getSubject());
        assertTrue(Objects.requireNonNull(msg.getText()).contains("123456"));
    }

    @Test
    void testSendDonationAlert() {
        emailService.sendDonationAlert(
                "ngo@test.org",
                "Apples",
                50,
                "Market St",
                "Fresh Bakery"
        );

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertArrayEquals(new String[]{"ngo@test.org"}, msg.getTo());
        assertTrue(Objects.requireNonNull(msg.getSubject()).contains("Apples"));
        assertTrue(Objects.requireNonNull(msg.getText()).contains("Fresh Bakery"));
        assertTrue(msg.getText().contains("50 servings"));
        assertTrue(msg.getText().contains("Market St"));
    }
}
