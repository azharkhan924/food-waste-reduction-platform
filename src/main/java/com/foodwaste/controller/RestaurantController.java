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
public class RestaurantController {

@Autowired
DonationService donationService;


@GetMapping("/restaurant/dashboard")
public String dashboard(HttpSession session, Model model){

Long userId = (Long) session.getAttribute("userId");

if(userId == null){
return "redirect:/login";
}

String name = (String) session.getAttribute("userName");
model.addAttribute("name", name);

List<Donation> donations = donationService.getDonationsByRestaurant(userId);
model.addAttribute("donations", donations);

long totalDonations = donations.size();
int totalMeals = 0;
for(Donation d : donations){
totalMeals += d.getQty();
}
model.addAttribute("totalDonations", totalDonations);
model.addAttribute("totalMeals", totalMeals);

return "restaurant/dashboard";
}


@GetMapping("/restaurant/add-donation")
public String addDonationForm(HttpSession session){

Long userId = (Long) session.getAttribute("userId");
if(userId == null){
return "redirect:/login";
}

return "restaurant/add-donation";
}


@PostMapping("/restaurant/add-donation")
public String saveDonation(@ModelAttribute Donation donation, HttpSession session){

Long userId = (Long) session.getAttribute("userId");
String userName = (String) session.getAttribute("userName");

if(userId == null){
return "redirect:/login";
}

donation.setRestaurantId(userId);
donation.setRestaurantName(userName);

donationService.addDonation(donation);

return "redirect:/restaurant/dashboard";
}


@GetMapping("/restaurant/delete/{id}")
public String deleteDonation(@PathVariable Long id, HttpSession session){

Long userId = (Long) session.getAttribute("userId");
if(userId == null){
return "redirect:/login";
}

donationService.deleteDonation(id);

return "redirect:/restaurant/dashboard";
}


@GetMapping("/restaurant/history")
public String history(HttpSession session, Model model){

Long userId = (Long) session.getAttribute("userId");
if(userId == null){
return "redirect:/login";
}

String name = (String) session.getAttribute("userName");
model.addAttribute("name", name);

List<Donation> donations = donationService.getDonationsByRestaurant(userId);
model.addAttribute("donations", donations);

return "restaurant/history";
}

}
