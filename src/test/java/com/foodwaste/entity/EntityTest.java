package com.foodwaste.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testDonationEntity() {
        Donation d = new Donation();
        d.setId(100L);
        d.setFoodName("Pasta");
        d.setQty(30);
        d.setExpiry(LocalDate.of(2026, 8, 20));
        d.setPickupAddress("Main St");
        d.setStatus("Pending");
        d.setRestaurantId(1L);
        d.setRestaurantName("Italiano");
        d.setNgoId(2L);
        d.setNgoName("Care NGO");
        LocalDateTime now = LocalDateTime.now();
        d.setCreatedAt(now);
        d.setLatitude(12.34);
        d.setLongitude(56.78);

        assertEquals(100L, d.getId());
        assertEquals("Pasta", d.getFoodName());
        assertEquals(30, d.getQty());
        assertEquals(LocalDate.of(2026, 8, 20), d.getExpiry());
        assertEquals("Main St", d.getPickupAddress());
        assertEquals("Pending", d.getStatus());
        assertEquals(1L, d.getRestaurantId());
        assertEquals("Italiano", d.getRestaurantName());
        assertEquals(2L, d.getNgoId());
        assertEquals("Care NGO", d.getNgoName());
        assertEquals(now, d.getCreatedAt());
        assertEquals(12.34, d.getLatitude());
        assertEquals(56.78, d.getLongitude());
    }

    @Test
    void testUserEntity() {
        User u1 = new User();
        u1.setId(1L);
        u1.setName("Alice");
        u1.setEmail("alice@test.com");
        u1.setPassword("hash");
        u1.setRole("NGO");
        u1.setBlocked(true);

        assertEquals(1L, u1.getId());
        assertEquals("Alice", u1.getName());
        assertEquals("alice@test.com", u1.getEmail());
        assertEquals("hash", u1.getPassword());
        assertEquals("NGO", u1.getRole());
        assertTrue(u1.isBlocked());

        User u2 = new User("Bob", "bob@test.com", "pass", "Restaurant");
        assertEquals("Bob", u2.getName());
        assertEquals("bob@test.com", u2.getEmail());
        assertEquals("pass", u2.getPassword());
        assertEquals("Restaurant", u2.getRole());
        assertFalse(u2.isBlocked());
    }
}
