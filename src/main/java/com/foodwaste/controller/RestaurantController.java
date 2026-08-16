package com.foodwaste.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.foodwaste.entity.Donation;
import com.foodwaste.service.DonationService;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class RestaurantController {

    private static final String ATTR_USER_ID = "userId";
    private static final String ATTR_USER_NAME = "userName";
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_DASHBOARD = "redirect:/restaurant/dashboard";

    private final DonationService donationService;

    public RestaurantController(DonationService donationService) {
        this.donationService = donationService;
    }

    @GetMapping("/restaurant/dashboard")
    public String dashboard(HttpSession session, Model model){

        Long userId = (Long) session.getAttribute(ATTR_USER_ID);

        if(userId == null){
            return REDIRECT_LOGIN;
        }

        String name = (String) session.getAttribute(ATTR_USER_NAME);
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

        Long userId = (Long) session.getAttribute(ATTR_USER_ID);
        if(userId == null){
            return REDIRECT_LOGIN;
        }

        return "restaurant/add-donation";
    }

    @PostMapping("/restaurant/add-donation")
    public String saveDonation(@ModelAttribute Donation donation, HttpSession session){

        Long userId = (Long) session.getAttribute(ATTR_USER_ID);
        String userName = (String) session.getAttribute(ATTR_USER_NAME);

        if(userId == null){
            return REDIRECT_LOGIN;
        }

        donation.setRestaurantId(userId);
        donation.setRestaurantName(userName);

        donationService.addDonation(donation);

        return REDIRECT_DASHBOARD;
    }

    @GetMapping("/restaurant/delete/{id}")
    public String deleteDonation(@PathVariable Long id, HttpSession session){

        Long userId = (Long) session.getAttribute(ATTR_USER_ID);
        if(userId == null){
            return REDIRECT_LOGIN;
        }

        donationService.deleteDonation(id);

        return REDIRECT_DASHBOARD;
    }

    @GetMapping("/restaurant/history")
    public String history(HttpSession session, Model model){

        Long userId = (Long) session.getAttribute(ATTR_USER_ID);
        if(userId == null){
            return REDIRECT_LOGIN;
        }

        String name = (String) session.getAttribute(ATTR_USER_NAME);
        model.addAttribute("name", name);

        List<Donation> donations = donationService.getDonationsByRestaurant(userId);
        model.addAttribute("donations", donations);

        return "restaurant/history";
    }

}
