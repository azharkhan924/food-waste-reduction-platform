package com.foodwaste.controller;

import com.foodwaste.entity.User;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private DonationRepository donationRepo;

    @InjectMocks
    private AdminController adminController;

    private MockHttpSession session;
    private Model model;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        model = new ConcurrentModel();
    }

    @Test
    void testDashboard_NotAdmin() {
        assertEquals("redirect:/login", adminController.dashboard(session, model));
    }

    @Test
    void testDashboard_AdminSuccess() {
        session.setAttribute("userRole", "Admin");
        session.setAttribute("userName", "Admin");

        when(userRepo.count()).thenReturn(10L);
        when(userRepo.countByRole("Restaurant")).thenReturn(6L);
        when(userRepo.countByRole("NGO")).thenReturn(3L);
        when(userRepo.countByBlocked(true)).thenReturn(1L);

        when(donationRepo.count()).thenReturn(20L);
        when(donationRepo.countByStatus("Pending")).thenReturn(5L);
        when(donationRepo.countByStatus("Accepted")).thenReturn(10L);
        when(donationRepo.countByStatus("Picked Up")).thenReturn(5L);
        when(donationRepo.findByCreatedAtBetween(any(), any())).thenReturn(List.of());
        when(donationRepo.findAll()).thenReturn(List.of());

        String view = adminController.dashboard(session, model);

        assertEquals("admin/dashboard", view);
        assertEquals(10L, model.getAttribute("totalUsers"));
        assertEquals(20L, model.getAttribute("totalDonations"));
    }

    @Test
    void testUsers_NotAdmin() {
        assertEquals("redirect:/login", adminController.users(null, session, model));
    }

    @Test
    void testUsers_FilteredRole() {
        session.setAttribute("userRole", "Admin");
        when(userRepo.findByRole("NGO")).thenReturn(List.of());

        String view = adminController.users("NGO", session, model);

        assertEquals("admin/users", view);
        assertEquals("NGO", model.getAttribute("filterRole"));
    }

    @Test
    void testUsers_All() {
        session.setAttribute("userRole", "Admin");
        when(userRepo.findAll()).thenReturn(List.of());

        String view = adminController.users(null, session, model);

        assertEquals("admin/users", view);
    }

    @Test
    void testAddUser_NotAdmin() {
        assertEquals("redirect:/login", adminController.addUser("Name", "test@email.com", "pass", "NGO", session));
    }

    @Test
    void testAddUser_Success() {
        session.setAttribute("userRole", "Admin");
        when(userRepo.existsByEmail("test@email.com")).thenReturn(false);

        String view = adminController.addUser("Name", "test@email.com", "pass", "NGO", session);

        assertEquals("redirect:/admin/users", view);
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void testBlockUser_Toggle() {
        session.setAttribute("userRole", "Admin");
        User user = new User("User", "u@test.com", "pass", "NGO");
        user.setBlocked(false);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        String view = adminController.blockUser(1L, session);

        assertEquals("redirect:/admin/users", view);
        assertTrue(user.isBlocked());
        verify(userRepo, times(1)).save(user);
    }

    @Test
    void testUpdateUser_Success() {
        session.setAttribute("userRole", "Admin");
        User user = new User("Old", "old@test.com", "pass", "NGO");
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        String view = adminController.updateUser(1L, "New", "new@test.com", "Restaurant", session);

        assertEquals("redirect:/admin/users", view);
        assertEquals("New", user.getName());
        assertEquals("new@test.com", user.getEmail());
        assertEquals("Restaurant", user.getRole());
        verify(userRepo, times(1)).save(user);
    }

    @Test
    void testDeleteUser_Success() {
        session.setAttribute("userRole", "Admin");

        String view = adminController.deleteUser(1L, session);

        assertEquals("redirect:/admin/users", view);
        verify(userRepo, times(1)).deleteById(1L);
    }

    @Test
    void testDonations_NotAdmin() {
        assertEquals("redirect:/login", adminController.donations(null, null, session, model));
    }

    @Test
    void testDonations_FilterPeriodToday() {
        session.setAttribute("userRole", "Admin");
        when(donationRepo.findByStatusAndCreatedAtBetween(eq("Pending"), any(), any())).thenReturn(List.of());

        String view = adminController.donations("Pending", "today", session, model);

        assertEquals("admin/donations", view);
    }

    @Test
    void testDonations_FilterPeriodWeek() {
        session.setAttribute("userRole", "Admin");
        when(donationRepo.findByCreatedAtBetween(any(), any())).thenReturn(List.of());

        String view = adminController.donations(null, "week", session, model);

        assertEquals("admin/donations", view);
    }

    @Test
    void testDonations_FilterPeriodMonth() {
        session.setAttribute("userRole", "Admin");
        when(donationRepo.findByCreatedAtBetween(any(), any())).thenReturn(List.of());

        String view = adminController.donations(null, "month", session, model);

        assertEquals("admin/donations", view);
    }

    @Test
    void testDonations_FilterStatusOnly() {
        session.setAttribute("userRole", "Admin");
        when(donationRepo.findByStatus("Pending")).thenReturn(List.of());

        String view = adminController.donations("Pending", null, session, model);

        assertEquals("admin/donations", view);
    }

    @Test
    void testDonations_All() {
        session.setAttribute("userRole", "Admin");
        when(donationRepo.findAll()).thenReturn(List.of());

        String view = adminController.donations(null, null, session, model);

        assertEquals("admin/donations", view);
    }
}
