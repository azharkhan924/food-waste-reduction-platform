package com.foodwaste.service;

import com.foodwaste.entity.Donation;
import com.foodwaste.entity.User;
import com.foodwaste.repository.DonationRepository;
import com.foodwaste.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationServiceTest {

    @Mock
    private DonationRepository donationRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DonationService donationService;

    private Donation sampleDonation;

    @BeforeEach
    void setUp() {
        sampleDonation = new Donation();
        sampleDonation.setId(1L);
        sampleDonation.setFoodName("Surplus Bread & Pastries");
        sampleDonation.setQty(25);
        sampleDonation.setPickupAddress("123 Bakery St");
        sampleDonation.setRestaurantId(10L);
        sampleDonation.setRestaurantName("Downtown Bakery");
        sampleDonation.setStatus("Pending");
    }

    @Test
    void testAddDonation_SetsPendingStatusAndAlertsNgos() {
        User ngoUser = new User("Hope NGO", "ngo@hope.org", "pass", "NGO");
        when(userRepo.findByRole("NGO")).thenReturn(List.of(ngoUser));
        when(donationRepo.save(any(Donation.class))).thenReturn(sampleDonation);

        donationService.addDonation(sampleDonation);

        assertEquals("Pending", sampleDonation.getStatus());
        assertNotNull(sampleDonation.getCreatedAt());
        verify(donationRepo, times(1)).save(sampleDonation);
        verify(emailService, times(1)).sendDonationAlert(
                "ngo@hope.org",
                "Surplus Bread & Pastries",
                25,
                "123 Bakery St",
                "Downtown Bakery"
        );
    }

    @Test
    void testAcceptDonation_WhenPending_Success() {
        when(donationRepo.findById(1L)).thenReturn(Optional.of(sampleDonation));
        when(donationRepo.save(any(Donation.class))).thenReturn(sampleDonation);

        donationService.acceptDonation(1L, 20L, "Helping Hands NGO");

        assertEquals("Accepted", sampleDonation.getStatus());
        assertEquals(20L, sampleDonation.getNgoId());
        assertEquals("Helping Hands NGO", sampleDonation.getNgoName());
        verify(donationRepo, times(1)).save(sampleDonation);
    }

    @Test
    void testAcceptDonation_WhenAlreadyAccepted_DoesNotChange() {
        sampleDonation.setStatus("Accepted");
        when(donationRepo.findById(1L)).thenReturn(Optional.of(sampleDonation));

        donationService.acceptDonation(1L, 20L, "Helping Hands NGO");

        verify(donationRepo, never()).save(sampleDonation);
    }

    @Test
    void testMarkPickedUp_WhenAccepted_Success() {
        sampleDonation.setStatus("Accepted");
        when(donationRepo.findById(1L)).thenReturn(Optional.of(sampleDonation));
        when(donationRepo.save(any(Donation.class))).thenReturn(sampleDonation);

        donationService.markPickedUp(1L);

        assertEquals("Picked Up", sampleDonation.getStatus());
        verify(donationRepo, times(1)).save(sampleDonation);
    }

    @Test
    void testGetPendingDonations() {
        when(donationRepo.findByStatus("Pending")).thenReturn(List.of(sampleDonation));

        List<Donation> result = donationService.getPendingDonations();

        assertEquals(1, result.size());
        assertEquals("Pending", result.get(0).getStatus());
    }

    @Test
    void testAddDonation_WhenEmailFails_ContinuesExecution() {
        User ngoUser = new User("Hope NGO", "ngo@hope.org", "pass", "NGO");
        when(userRepo.findByRole("NGO")).thenReturn(List.of(ngoUser));
        when(donationRepo.save(any(Donation.class))).thenReturn(sampleDonation);
        doThrow(new RuntimeException("SMTP down")).when(emailService).sendDonationAlert(anyString(), anyString(), anyInt(), anyString(), anyString());

        assertDoesNotThrow(() -> donationService.addDonation(sampleDonation));
        verify(donationRepo, times(1)).save(sampleDonation);
    }

    @Test
    void testGetDonationsByRestaurant() {
        when(donationRepo.findByRestaurantId(10L)).thenReturn(List.of(sampleDonation));

        List<Donation> result = donationService.getDonationsByRestaurant(10L);

        assertEquals(1, result.size());
    }

    @Test
    void testGetDonationsByNgo() {
        when(donationRepo.findByNgoId(20L)).thenReturn(List.of(sampleDonation));

        List<Donation> result = donationService.getDonationsByNgo(20L);

        assertEquals(1, result.size());
    }

    @Test
    void testCountByRestaurant() {
        when(donationRepo.findByRestaurantId(10L)).thenReturn(List.of(sampleDonation));

        long count = donationService.countByRestaurant(10L);

        assertEquals(1L, count);
    }

    @Test
    void testAcceptDonation_WhenNotFound() {
        when(donationRepo.findById(999L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> donationService.acceptDonation(999L, 20L, "NGO"));
        verify(donationRepo, never()).save(any(Donation.class));
    }

    @Test
    void testMarkPickedUp_WhenNotFoundOrPending() {
        when(donationRepo.findById(999L)).thenReturn(Optional.empty());
        donationService.markPickedUp(999L);

        sampleDonation.setStatus("Pending");
        when(donationRepo.findById(1L)).thenReturn(Optional.of(sampleDonation));
        donationService.markPickedUp(1L);

        verify(donationRepo, never()).save(sampleDonation);
    }

    @Test
    void testDeleteDonation() {
        doNothing().when(donationRepo).deleteById(1L);

        donationService.deleteDonation(1L);

        verify(donationRepo, times(1)).deleteById(1L);
    }
}
