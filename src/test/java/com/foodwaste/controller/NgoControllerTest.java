package com.foodwaste.controller;

import com.foodwaste.entity.Donation;
import com.foodwaste.service.DonationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NgoControllerTest {

    @Mock
    private DonationService donationService;

    @InjectMocks
    private NgoController ngoController;

    private MockHttpSession session;
    private Model model;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        model = new ConcurrentModel();
    }

    @Test
    void testDashboard_NoSession() {
        assertEquals("redirect:/login", ngoController.dashboard(session, model));
    }

    @Test
    void testDashboard_WithSession() {
        session.setAttribute("userId", 8L);
        session.setAttribute("userName", "Helping NGO");

        when(donationService.getPendingDonations()).thenReturn(List.of(new Donation()));

        String view = ngoController.dashboard(session, model);

        assertEquals("ngo/dashboard", view);
        assertEquals("Helping NGO", model.getAttribute("name"));
        assertNotNull(model.getAttribute("donations"));
    }

    @Test
    void testMapView_NoSession() {
        assertEquals("redirect:/login", ngoController.mapView(session, model));
    }

    @Test
    void testMapView_WithSession() {
        session.setAttribute("userId", 8L);
        session.setAttribute("userName", "Helping NGO");

        when(donationService.getPendingDonations()).thenReturn(List.of());

        String view = ngoController.mapView(session, model);

        assertEquals("ngo/map", view);
        assertEquals("Helping NGO", model.getAttribute("name"));
    }

    @Test
    void testAcceptDonation_NoSession() {
        assertEquals("redirect:/login", ngoController.acceptDonation(1L, session));
    }

    @Test
    void testAcceptDonation_WithSession() {
        session.setAttribute("userId", 8L);
        session.setAttribute("userName", "Helping NGO");

        String view = ngoController.acceptDonation(1L, session);

        assertEquals("redirect:/ngo/dashboard", view);
        verify(donationService, times(1)).acceptDonation(1L, 8L, "Helping NGO");
    }

    @Test
    void testMarkPickedUp_NoSession() {
        assertEquals("redirect:/login", ngoController.markPickedUp(1L, session));
    }

    @Test
    void testMarkPickedUp_WithSession() {
        session.setAttribute("userId", 8L);

        String view = ngoController.markPickedUp(1L, session);

        assertEquals("redirect:/ngo/history", view);
        verify(donationService, times(1)).markPickedUp(1L);
    }

    @Test
    void testHistory_NoSession() {
        assertEquals("redirect:/login", ngoController.history(session, model));
    }

    @Test
    void testHistory_WithSession() {
        session.setAttribute("userId", 8L);
        session.setAttribute("userName", "Helping NGO");

        Donation d = new Donation();
        d.setQty(20);
        when(donationService.getDonationsByNgo(8L)).thenReturn(List.of(d));

        String view = ngoController.history(session, model);

        assertEquals("ngo/history", view);
        assertEquals(1L, model.getAttribute("totalAccepted"));
        assertEquals(20, model.getAttribute("totalMeals"));
    }
}
