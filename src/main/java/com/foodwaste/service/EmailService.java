package com.foodwaste.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.foodwaste.entity.Donation;
import com.foodwaste.entity.User;
import com.foodwaste.repository.UserRepository;

import java.util.List;

@Service
public class EmailService {

@Autowired(required = false)
private JavaMailSender mailSender;

@Autowired
private UserRepository userRepo;

public void sendMail(String to){
try {
if(mailSender == null) return;
SimpleMailMessage message=new SimpleMailMessage();
message.setTo(to);
message.setSubject("Testing Spring Boot Mail");
message.setText("Hello!\n\nThis email was sent from Food Waste Platform.");
mailSender.send(message);
} catch (Exception e) {
}
}

public void sendOtp(String to, String otp){
try {
if(mailSender == null) return;
SimpleMailMessage message=new SimpleMailMessage();
message.setTo(to);
message.setSubject("OTP for Password Reset");
message.setText("Your OTP is: " + otp + "\n\nUse this to reset your password.");
mailSender.send(message);
} catch (Exception e) {
}
}

@Async
public void notifyNgos(Donation donation){
try {
List<User> ngoUsers = userRepo.findByRole("NGO");
for(User ngo : ngoUsers){
try {
sendDonationAlert(ngo.getEmail(), donation.getFoodName(), donation.getQty(), donation.getPickupAddress(), donation.getRestaurantName());
} catch(Exception e){
}
}
} catch(Exception e){
}
}

public void sendDonationAlert(String to, String foodName, int qty, String pickupAddress, String restaurantName){
try {
if(mailSender == null) return;
SimpleMailMessage message = new SimpleMailMessage();
message.setFrom("khanazhar618190@gmail.com");
message.setTo(to);
message.setSubject("New Food Donation Available - " + foodName);
message.setText("Hi,\n\n"
        + "A new food donation has been added on the Food Waste Platform!\n\n"
        + "Food Item: " + foodName + "\n"
        + "Quantity: " + qty + " servings\n"
        + "Pickup Address: " + pickupAddress + "\n"
        + "Donated By: " + restaurantName + "\n\n"
        + "Login to the platform to accept this donation before someone else does.\n\n"
        + "Thank you,\nFood Waste Reduction Platform");
mailSender.send(message);
} catch (Exception e) {
}
}

}