package com.foodwaste.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foodwaste.entity.Donation;
import com.foodwaste.entity.User;
import com.foodwaste.repository.DonationRepository;
import com.foodwaste.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DonationService {

@Autowired
private DonationRepository donationRepo;

@Autowired
private UserRepository userRepo;

@Autowired
private EmailService emailService;

public void addDonation(Donation donation){

donation.setStatus("Pending");
donation.setCreatedAt(LocalDateTime.now());
donationRepo.save(donation);

emailService.notifyNgos(donation);
}

public List<Donation> getDonationsByRestaurant(Long restaurantId){
return donationRepo.findByRestaurantId(restaurantId);
}

public List<Donation> getPendingDonations(){
return donationRepo.findByStatus("Pending");
}

public List<Donation> getDonationsByNgo(Long ngoId){
return donationRepo.findByNgoId(ngoId);
}

public void acceptDonation(Long donationId, Long ngoId, String ngoName){

Donation d = donationRepo.findById(donationId).orElse(null);

if(d != null && d.getStatus().equals("Pending")){
d.setStatus("Accepted");
d.setNgoId(ngoId);
d.setNgoName(ngoName);
donationRepo.save(d);
}
}

public void markPickedUp(Long donationId){

Donation d = donationRepo.findById(donationId).orElse(null);

if(d != null && d.getStatus().equals("Accepted")){
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
