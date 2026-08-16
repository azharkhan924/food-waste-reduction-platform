package com.foodwaste.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.foodwaste.entity.Donation;
import com.foodwaste.repository.DonationRepository;
import com.foodwaste.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.ArrayList;
import java.time.YearMonth;

@Controller
public class ReportsController {

    private final DonationRepository donationRepo;
    private final UserRepository userRepo;

    public ReportsController(DonationRepository donationRepo, UserRepository userRepo) {
        this.donationRepo = donationRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/admin/reports")
    public String reports(HttpSession session, Model model){

        String role = (String) session.getAttribute("userRole");
        if(role == null || !"Admin".equals(role)) {
            return "redirect:/login";
        }

        long totalDonations = donationRepo.count();
        long pending = donationRepo.countByStatus("Pending");
        long accepted = donationRepo.countByStatus("Accepted");
        long pickedUp = donationRepo.countByStatus("Picked Up");

        model.addAttribute("totalDonations", totalDonations);
        model.addAttribute("pending", pending);
        model.addAttribute("accepted", accepted);
        model.addAttribute("pickedUp", pickedUp);

        long totalFood = donationRepo.sumTotalQty();
        long foodSaved = donationRepo.sumQtyByStatus("Picked Up");

        model.addAttribute("totalFood", totalFood);
        model.addAttribute("foodSaved", foodSaved);
        model.addAttribute("meals", foodSaved * 2);

        model.addAttribute("totalNgos", userRepo.countByRole("NGO"));
        model.addAttribute("totalRestaurants", userRepo.countByRole("Restaurant"));
        model.addAttribute("ngoClaimed", donationRepo.countByNgoIdIsNotNull());

        List<String> months = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        List<Long> foodPerMonth = new ArrayList<>();

        for(int i = 5; i >= 0; i--){
            YearMonth ym = YearMonth.now().minusMonths(i);
            months.add(ym.getMonth().toString().substring(0, 3));

            List<Donation> list = donationRepo.findByCreatedAtBetween(
                ym.atDay(1).atStartOfDay(),
                ym.atEndOfMonth().atTime(23, 59, 59)
            );
            counts.add((long) list.size());

            long kg = 0;
            for(Donation d : list) {
                kg += d.getQty();
            }
            foodPerMonth.add(kg);
        }

        model.addAttribute("months", months);
        model.addAttribute("counts", counts);
        model.addAttribute("foodPerMonth", foodPerMonth);

        return "admin/reports";
    }
}
