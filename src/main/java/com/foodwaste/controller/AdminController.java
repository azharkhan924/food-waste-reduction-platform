package com.foodwaste.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.foodwaste.entity.Donation;
import com.foodwaste.entity.User;
import com.foodwaste.repository.DonationRepository;
import com.foodwaste.repository.UserRepository;
import com.foodwaste.security.IpAbuseGuard;
import com.foodwaste.security.LoginAttemptService;
import com.foodwaste.util.PasswordUtil;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AdminController {

    private static final String ROLE_ADMIN = "Admin";
    private static final String ATTR_USER_ROLE = "userRole";
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_ADMIN_USERS = "redirect:/admin/users";
    private static final String REDIRECT_ADMIN_SECURITY = "redirect:/admin/security";

    private final UserRepository userRepo;
    private final DonationRepository donationRepo;
    private final LoginAttemptService loginAttemptService;
    private final IpAbuseGuard ipAbuseGuard;

    public AdminController(UserRepository userRepo,
                           DonationRepository donationRepo,
                           LoginAttemptService loginAttemptService,
                           IpAbuseGuard ipAbuseGuard) {
        this.userRepo = userRepo;
        this.donationRepo = donationRepo;
        this.loginAttemptService = loginAttemptService;
        this.ipAbuseGuard = ipAbuseGuard;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model){

        String role = (String) session.getAttribute(ATTR_USER_ROLE);
        if(role == null || !ROLE_ADMIN.equals(role)) {
            return REDIRECT_LOGIN;
        }

        model.addAttribute("name", session.getAttribute("userName"));

        model.addAttribute("totalUsers", userRepo.count());
        model.addAttribute("totalRestaurants", userRepo.countByRole("Restaurant"));
        model.addAttribute("totalNgos", userRepo.countByRole("NGO"));
        model.addAttribute("blockedUsers", userRepo.countByBlocked(true));
        model.addAttribute("lockedUsers", loginAttemptService.getLockedUsers().size());
        model.addAttribute("blockedIpsCount", ipAbuseGuard.getBlockedIps().size());

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

        String role = (String) session.getAttribute(ATTR_USER_ROLE);
        if(role == null || !ROLE_ADMIN.equals(role)) {
            return REDIRECT_LOGIN;
        }

        if(filterRole != null && !filterRole.isEmpty()) {
            model.addAttribute("users", userRepo.findByRole(filterRole));
        } else {
            model.addAttribute("users", userRepo.findAll());
        }

        model.addAttribute("filterRole", filterRole);
        return "admin/users";
    }

    @PostMapping("/admin/add-user")
    public String addUser(@RequestParam String name, @RequestParam String email,
            @RequestParam String password, @RequestParam String role, HttpSession session){

        if(!ROLE_ADMIN.equals(session.getAttribute(ATTR_USER_ROLE))) {
            return REDIRECT_LOGIN;
        }

        if(!userRepo.existsByEmail(email)){
            userRepo.save(new User(name, email, PasswordUtil.hashPassword(password), role));
        }
        return REDIRECT_ADMIN_USERS;
    }

    @GetMapping("/admin/block/{id}")
    public String blockUser(@PathVariable Long id, HttpSession session){
        if(!ROLE_ADMIN.equals(session.getAttribute(ATTR_USER_ROLE))) {
            return REDIRECT_LOGIN;
        }

        User u = userRepo.findById(id).orElse(null);
        if(u != null){
            u.setBlocked(!u.isBlocked());
            userRepo.save(u);
        }
        return REDIRECT_ADMIN_USERS;
    }

    @PostMapping("/admin/update-user")
    public String updateUser(@RequestParam Long id, @RequestParam String name,
            @RequestParam String email, @RequestParam String role, HttpSession session){

        if(!ROLE_ADMIN.equals(session.getAttribute(ATTR_USER_ROLE))) {
            return REDIRECT_LOGIN;
        }

        User u = userRepo.findById(id).orElse(null);
        if(u != null){
            u.setName(name);
            u.setEmail(email);
            u.setRole(role);
            userRepo.save(u);
        }
        return REDIRECT_ADMIN_USERS;
    }

    @GetMapping("/admin/delete-user/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session){
        if(!ROLE_ADMIN.equals(session.getAttribute(ATTR_USER_ROLE))) {
            return REDIRECT_LOGIN;
        }
        userRepo.deleteById(id);
        return REDIRECT_ADMIN_USERS;
    }

    @GetMapping("/admin/donations")
    public String donations(@RequestParam(required=false) String statusFilter,
            @RequestParam(required=false) String period, HttpSession session, Model model){

        if(!ROLE_ADMIN.equals(session.getAttribute(ATTR_USER_ROLE))) {
            return REDIRECT_LOGIN;
        }

        List<Donation> list;
        LocalDateTime now = LocalDateTime.now();

        if(period != null && !period.isEmpty()){
            LocalDateTime start;
            if ("today".equals(period)) {
                start = now.toLocalDate().atStartOfDay();
            } else if ("week".equals(period)) {
                start = now.minusDays(7);
            } else {
                start = now.minusDays(30);
            }

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

    @GetMapping("/admin/security")
    public String security(HttpSession session, Model model){
        String role = (String) session.getAttribute(ATTR_USER_ROLE);
        if(role == null || !ROLE_ADMIN.equals(role)) {
            return REDIRECT_LOGIN;
        }

        model.addAttribute("lockedUsers", loginAttemptService.getLockedUsers());
        model.addAttribute("blockedIps", ipAbuseGuard.getBlockedIps());

        return "admin/security";
    }

    @PostMapping("/admin/security/unlock-user/{id}")
    public String unlockUser(@PathVariable Long id, HttpSession session){
        if(!ROLE_ADMIN.equals(session.getAttribute(ATTR_USER_ROLE))) {
            return REDIRECT_LOGIN;
        }

        loginAttemptService.unlockUser(id);
        return REDIRECT_ADMIN_SECURITY;
    }

    @PostMapping("/admin/security/unblock-ip")
    public String unblockIp(@RequestParam String ip, HttpSession session){
        if(!ROLE_ADMIN.equals(session.getAttribute(ATTR_USER_ROLE))) {
            return REDIRECT_LOGIN;
        }

        ipAbuseGuard.unblockIp(ip);
        return REDIRECT_ADMIN_SECURITY;
    }

}
