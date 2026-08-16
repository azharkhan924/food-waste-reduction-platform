package com.foodwaste.controller;

import com.foodwaste.entity.Donation;
import com.foodwaste.service.DonationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapControllerTest {

    @Mock
    private DonationService donationService;

    @InjectMocks
    private MapController mapController;

    @Test
    void testGetPendingForMap() {
        Donation d1 = new Donation();
        d1.setId(1L);
        d1.setFoodName("Apples");
        d1.setQty(20);
        d1.setExpiry(LocalDate.now());
        d1.setPickupAddress("Address 1");
        d1.setRestaurantName("Rest 1");
        d1.setLatitude(28.7041);
        d1.setLongitude(77.1025);

        Donation d2 = new Donation();
        d2.setId(2L);
        d2.setLatitude(null); // Should be skipped

        when(donationService.getPendingDonations()).thenReturn(List.of(d1, d2));

        List<Map<String, Object>> result = mapController.getPendingForMap();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).get("id"));
        assertEquals("Apples", result.get(0).get("foodName"));
        assertEquals(28.7041, result.get(0).get("lat"));
    }
}
