package com.foodwaste.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.foodwaste.entity.Donation;
import com.foodwaste.service.DonationService;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class NgoController {

    private static final String ATTR_USER_ID = "userId";
    private static final String ATTR_USER_NAME = "userName";
    private static final String ATTR_DONATIONS = "donations";
    private static final String REDIRECT_LOGIN = "redirect:/login";

    private final DonationService donationService;

    public NgoController(DonationService donationService) {
        this.donationService = donationService;
    }

    @GetMapping("/ngo/dashboard")
    public String dashboard(HttpSession session, Model model){

        Long userId = (Long) session.getAttribute(ATTR_USER_ID);

        if(userId == null){
            return REDIRECT_LOGIN;
        }

        String name = (String) session.getAttribute(ATTR_USER_NAME);
        model.addAttribute("name", name);

        List<Donation> pendingDonations = donationService.getPendingDonations();
        model.addAttribute(ATTR_DONATIONS, pendingDonations);

        return "ngo/dashboard";
    }

    @GetMapping("/ngo/map")
    public String mapView(HttpSession session, Model model){

        Long userId = (Long) session.getAttribute(ATTR_USER_ID);
        if(userId == null){
            return REDIRECT_LOGIN;
        }

        String name = (String) session.getAttribute(ATTR_USER_NAME);
        model.addAttribute("name", name);

        List<Donation> pendingDonations = donationService.getPendingDonations();
        model.addAttribute(ATTR_DONATIONS, pendingDonations);

        return "ngo/map";
    }

    @GetMapping("/ngo/accept/{id}")
    public String acceptDonation(@PathVariable Long id, HttpSession session){

        Long userId = (Long) session.getAttribute(ATTR_USER_ID);
        String userName = (String) session.getAttribute(ATTR_USER_NAME);

        if(userId == null){
            return REDIRECT_LOGIN;
        }

        donationService.acceptDonation(id, userId, userName);

        return "redirect:/ngo/dashboard";
    }

    @GetMapping("/ngo/pickup/{id}")
    public String markPickedUp(@PathVariable Long id, HttpSession session){

        Long userId = (Long) session.getAttribute(ATTR_USER_ID);

        if(userId == null){
            return REDIRECT_LOGIN;
        }

        donationService.markPickedUp(id);

        return "redirect:/ngo/history";
    }

    @GetMapping("/ngo/history")
    public String history(HttpSession session, Model model){

        Long userId = (Long) session.getAttribute(ATTR_USER_ID);

        if(userId == null){
            return REDIRECT_LOGIN;
        }

        String name = (String) session.getAttribute(ATTR_USER_NAME);
        model.addAttribute("name", name);

        List<Donation> myDonations = donationService.getDonationsByNgo(userId);
        model.addAttribute(ATTR_DONATIONS, myDonations);

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
