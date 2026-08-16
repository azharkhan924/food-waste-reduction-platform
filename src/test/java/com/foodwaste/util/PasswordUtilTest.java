package com.foodwaste.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void testBCryptHashingAndMatching() {
        String rawPassword = "MySecretPassword123!";
        String hash = PasswordUtil.hashPassword(rawPassword);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        assertTrue(PasswordUtil.checkPassword(rawPassword, hash));
        assertFalse(PasswordUtil.checkPassword("WrongPassword", hash));
    }
}
