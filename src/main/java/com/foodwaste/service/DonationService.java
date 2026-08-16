package com.foodwaste.service;

import org.springframework.stereotype.Service;

import com.foodwaste.entity.Donation;
import com.foodwaste.entity.User;
import com.foodwaste.repository.DonationRepository;
import com.foodwaste.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DonationService {

    private static final Logger log = LoggerFactory.getLogger(DonationService.class);
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_ACCEPTED = "Accepted";

    private final DonationRepository donationRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;

    public DonationService(DonationRepository donationRepo, UserRepository userRepo, EmailService emailService) {
        this.donationRepo = donationRepo;
        this.userRepo = userRepo;
        this.emailService = emailService;
    }

    public void addDonation(Donation donation){

        donation.setStatus(STATUS_PENDING);
        donation.setCreatedAt(LocalDateTime.now());
        donationRepo.save(donation);

        List<User> ngoUsers = userRepo.findByRole("NGO");

        for(User ngo : ngoUsers){

            try {
                emailService.sendDonationAlert(
                    ngo.getEmail(),
                    donation.getFoodName(),
                    donation.getQty(),
                    donation.getPickupAddress(),
                    donation.getRestaurantName()
                );
                log.info("Email sent to NGO: {}", ngo.getEmail());
            } catch(Exception e) {
                log.error("Failed to send email to: {}", ngo.getEmail(), e);
            }
        }

    }

public List<Donation> getDonationsByRestaurant(Long restaurantId){
return donationRepo.findByRestaurantId(restaurantId);
}

public List<Donation> getPendingDonations(){
return donationRepo.findByStatus(STATUS_PENDING);
}

public List<Donation> getDonationsByNgo(Long ngoId){
return donationRepo.findByNgoId(ngoId);
}

public void acceptDonation(Long donationId, Long ngoId, String ngoName){

Donation d = donationRepo.findById(donationId).orElse(null);

if(d != null && STATUS_PENDING.equals(d.getStatus())){
d.setStatus(STATUS_ACCEPTED);
d.setNgoId(ngoId);
d.setNgoName(ngoName);
donationRepo.save(d);
}
}

public void markPickedUp(Long donationId){

Donation d = donationRepo.findById(donationId).orElse(null);

if(d != null && STATUS_ACCEPTED.equals(d.getStatus())){
d.setStatus("Picked Up");
donationRepo.save(d);
}
}

public void deleteDonation(Long id){
donationRepo.deleteById(id);
}

public long countByRestaurant(Long restaurantId){
return donationRepo.findByRestaurantId(restaurantId).size();
}

}
