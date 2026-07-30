package com.foodwaste.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.foodwaste.entity.Donation;
import com.foodwaste.service.DonationService;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class NgoController {

@Autowired
DonationService donationService;


@GetMapping("/ngo/dashboard")
public String dashboard(HttpSession session, Model model){

Long userId = (Long) session.getAttribute("userId");

if(userId == null){
return "redirect:/login";
}

String name = (String) session.getAttribute("userName");
model.addAttribute("name", name);

List<Donation> pendingDonations = donationService.getPendingDonations();
model.addAttribute("donations", pendingDonations);

return "ngo/dashboard";
}


@GetMapping("/ngo/accept/{id}")
public String acceptDonation(@PathVariable Long id, HttpSession session){

Long userId = (Long) session.getAttribute("userId");
String userName = (String) session.getAttribute("userName");

if(userId == null){
return "redirect:/login";
}

donationService.acceptDonation(id, userId, userName);

return "redirect:/ngo/dashboard";
}


@GetMapping("/ngo/pickup/{id}")
public String markPickedUp(@PathVariable Long id, HttpSession session){

Long userId = (Long) session.getAttribute("userId");

if(userId == null){
return "redirect:/login";
}

donationService.markPickedUp(id);

return "redirect:/ngo/history";
}


@GetMapping("/ngo/history")
public String history(HttpSession session, Model model){

Long userId = (Long) session.getAttribute("userId");

if(userId == null){
return "redirect:/login";
}

String name = (String) session.getAttribute("userName");
model.addAttribute("name", name);

List<Donation> myDonations = donationService.getDonationsByNgo(userId);
model.addAttribute("donations", myDonations);

long totalAccepted = myDonations.size();
int totalMeals = 0;
for(Donation d : myDonations){
totalMeals += d.getQty();
}
model.addAttribute("totalAccepted", totalAccepted);
model.addAttribute("totalMeals", totalMeals);

return "ngo/history";
}

}
