package com.foodwaste.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.foodwaste.entity.User;
import com.foodwaste.entity.Donation;
import com.foodwaste.repository.UserRepository;
import com.foodwaste.repository.DonationRepository;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.time.LocalDateTime;

@Controller
public class AdminController {

@Autowired
UserRepository userRepo;

@Autowired
DonationRepository donationRepo;

@GetMapping("/admin/dashboard")
public String dashboard(HttpSession session, Model model){

String role = (String) session.getAttribute("userRole");
if(role == null || !role.equals("Admin")) return "redirect:/login";

model.addAttribute("name", session.getAttribute("userName"));

model.addAttribute("totalUsers", userRepo.count());
model.addAttribute("totalRestaurants", userRepo.countByRole("Restaurant"));
model.addAttribute("totalNgos", userRepo.countByRole("NGO"));
model.addAttribute("blockedUsers", userRepo.countByBlocked(true));

model.addAttribute("totalDonations", donationRepo.count());
model.addAttribute("pendingCount", donationRepo.countByStatus("Pending"));
model.addAttribute("acceptedCount", donationRepo.countByStatus("Accepted"));
model.addAttribute("pickedUpCount", donationRepo.countByStatus("Picked Up"));

LocalDateTime now = LocalDateTime.now();
model.addAttribute("todayDonations", donationRepo.findByCreatedAtBetween(now.toLocalDate().atStartOfDay(), now).size());
model.addAttribute("weeklyDonations", donationRepo.findByCreatedAtBetween(now.minusDays(7), now).size());
model.addAttribute("monthlyDonations", donationRepo.findByCreatedAtBetween(now.minusDays(30), now).size());

model.addAttribute("allDonations", donationRepo.findAll());

return "admin/dashboard";
}

@GetMapping("/admin/users")
public String users(@RequestParam(required=false) String filterRole, HttpSession session, Model model){

String role = (String) session.getAttribute("userRole");
if(role == null || !role.equals("Admin")) return "redirect:/login";

if(filterRole != null && !filterRole.isEmpty())
model.addAttribute("users", userRepo.findByRole(filterRole));
else
model.addAttribute("users", userRepo.findAll());

model.addAttribute("filterRole", filterRole);
return "admin/users";
}

@PostMapping("/admin/add-user")
public String addUser(@RequestParam String name, @RequestParam String email,
@RequestParam String password, @RequestParam String role, HttpSession session){

if(!"Admin".equals(session.getAttribute("userRole"))) return "redirect:/login";

if(!userRepo.existsByEmail(email)){
userRepo.save(new User(name, email, password, role));
}
return "redirect:/admin/users";
}

@GetMapping("/admin/block/{id}")
public String blockUser(@PathVariable Long id, HttpSession session){
if(!"Admin".equals(session.getAttribute("userRole"))) return "redirect:/login";

User u = userRepo.findById(id).orElse(null);
if(u != null){
u.setBlocked(!u.isBlocked());
userRepo.save(u);
}
return "redirect:/admin/users";
}

@PostMapping("/admin/update-user")
public String updateUser(@RequestParam Long id, @RequestParam String name,
@RequestParam String email, @RequestParam String role, HttpSession session){

if(!"Admin".equals(session.getAttribute("userRole"))) return "redirect:/login";

User u = userRepo.findById(id).orElse(null);
if(u != null){
u.setName(name);
u.setEmail(email);
u.setRole(role);
userRepo.save(u);
}
return "redirect:/admin/users";
}

@GetMapping("/admin/delete-user/{id}")
public String deleteUser(@PathVariable Long id, HttpSession session){
if(!"Admin".equals(session.getAttribute("userRole"))) return "redirect:/login";
userRepo.deleteById(id);
return "redirect:/admin/users";
}

@GetMapping("/admin/donations")
public String donations(@RequestParam(required=false) String statusFilter,
@RequestParam(required=false) String period, HttpSession session, Model model){

if(!"Admin".equals(session.getAttribute("userRole"))) return "redirect:/login";

List<Donation> list;
LocalDateTime now = LocalDateTime.now();

if(period != null && !period.isEmpty()){
LocalDateTime start = period.equals("today") ? now.toLocalDate().atStartOfDay() :
period.equals("week") ? now.minusDays(7) : now.minusDays(30);

list = (statusFilter != null && !statusFilter.isEmpty()) ?
donationRepo.findByStatusAndCreatedAtBetween(statusFilter, start, now) :
donationRepo.findByCreatedAtBetween(start, now);
} else {
list = (statusFilter != null && !statusFilter.isEmpty()) ?
donationRepo.findByStatus(statusFilter) : donationRepo.findAll();
}

model.addAttribute("donations", list);
model.addAttribute("statusFilter", statusFilter);
model.addAttribute("period", period);
model.addAttribute("totalShowing", list.size());
return "admin/donations";
}

}
