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
class RestaurantControllerTest {

    @Mock
    private DonationService donationService;

    @InjectMocks
    private RestaurantController restaurantController;

    private MockHttpSession session;
    private Model model;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        model = new ConcurrentModel();
    }

    @Test
    void testDashboard_NoSession_RedirectsLogin() {
        String view = restaurantController.dashboard(session, model);
        assertEquals("redirect:/login", view);
    }

    @Test
    void testDashboard_WithSession_Success() {
        session.setAttribute("userId", 5L);
        session.setAttribute("userName", "Food Hub");

        Donation d1 = new Donation();
        d1.setQty(10);
        when(donationService.getDonationsByRestaurant(5L)).thenReturn(List.of(d1));

        String view = restaurantController.dashboard(session, model);

        assertEquals("restaurant/dashboard", view);
        assertEquals("Food Hub", model.getAttribute("name"));
        assertEquals(1L, model.getAttribute("totalDonations"));
        assertEquals(10, model.getAttribute("totalMeals"));
    }

    @Test
    void testAddDonationForm_NoSession() {
        assertEquals("redirect:/login", restaurantController.addDonationForm(session));
    }

    @Test
    void testAddDonationForm_WithSession() {
        session.setAttribute("userId", 5L);
        assertEquals("restaurant/add-donation", restaurantController.addDonationForm(session));
    }

    @Test
    void testSaveDonation_NoSession() {
        Donation d = new Donation();
        assertEquals("redirect:/login", restaurantController.saveDonation(d, session));
    }

    @Test
    void testSaveDonation_WithSession() {
        session.setAttribute("userId", 5L);
        session.setAttribute("userName", "Food Hub");

        Donation d = new Donation();
        String view = restaurantController.saveDonation(d, session);

        assertEquals("redirect:/restaurant/dashboard", view);
        assertEquals(5L, d.getRestaurantId());
        assertEquals("Food Hub", d.getRestaurantName());
        verify(donationService, times(1)).addDonation(d);
    }

    @Test
    void testDeleteDonation_NoSession() {
        assertEquals("redirect:/login", restaurantController.deleteDonation(1L, session));
    }

    @Test
    void testDeleteDonation_WithSession() {
        session.setAttribute("userId", 5L);
        String view = restaurantController.deleteDonation(1L, session);

        assertEquals("redirect:/restaurant/dashboard", view);
        verify(donationService, times(1)).deleteDonation(1L);
    }

    @Test
    void testHistory_NoSession() {
        assertEquals("redirect:/login", restaurantController.history(session, model));
    }

    @Test
    void testHistory_WithSession() {
        session.setAttribute("userId", 5L);
        session.setAttribute("userName", "Food Hub");

        when(donationService.getDonationsByRestaurant(5L)).thenReturn(List.of());

        String view = restaurantController.history(session, model);

        assertEquals("restaurant/history", view);
        assertEquals("Food Hub", model.getAttribute("name"));
    }
}
