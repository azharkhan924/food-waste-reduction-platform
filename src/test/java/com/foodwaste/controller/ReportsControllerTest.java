package com.foodwaste.controller;

import com.foodwaste.entity.Donation;
import com.foodwaste.repository.DonationRepository;
import com.foodwaste.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportsControllerTest {

    @Mock
    private DonationRepository donationRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private ReportsController reportsController;

    private MockHttpSession session;
    private Model model;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        model = new ConcurrentModel();
    }

    @Test
    void testReports_NotAdmin() {
        assertEquals("redirect:/login", reportsController.reports(session, model));
    }

    @Test
    void testReports_AdminSuccess() {
        session.setAttribute("userRole", "Admin");

        when(donationRepo.count()).thenReturn(100L);
        when(donationRepo.countByStatus("Pending")).thenReturn(20L);
        when(donationRepo.countByStatus("Accepted")).thenReturn(30L);
        when(donationRepo.countByStatus("Picked Up")).thenReturn(50L);

        when(donationRepo.sumTotalQty()).thenReturn(500L);
        when(donationRepo.sumQtyByStatus("Picked Up")).thenReturn(250L);

        when(userRepo.countByRole("NGO")).thenReturn(10L);
        when(userRepo.countByRole("Restaurant")).thenReturn(15L);
        when(donationRepo.countByNgoIdIsNotNull()).thenReturn(80L);

        Donation d = new Donation();
        d.setQty(10);
        when(donationRepo.findByCreatedAtBetween(any(), any())).thenReturn(List.of(d));

        String view = reportsController.reports(session, model);

        assertEquals("admin/reports", view);
        assertEquals(100L, model.getAttribute("totalDonations"));
        assertEquals(500L, model.getAttribute("totalFood"));
        assertEquals(250L, model.getAttribute("foodSaved"));
        assertEquals(500L, model.getAttribute("meals"));
        assertNotNull(model.getAttribute("months"));
        assertNotNull(model.getAttribute("counts"));
        assertNotNull(model.getAttribute("foodPerMonth"));
    }
}
