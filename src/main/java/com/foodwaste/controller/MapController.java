package com.foodwaste.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.foodwaste.entity.Donation;
import com.foodwaste.service.DonationService;
import java.util.*;

@RestController
public class MapController {

@Autowired
DonationService donationService;

@GetMapping("/api/donations/pending")
public List<Map<String, Object>> getPendingForMap(){

    List<Donation> donations = donationService.getPendingDonations();
    List<Map<String, Object>> result = new ArrayList<>();

    for(Donation d : donations){
        if(d.getLatitude() != null && d.getLongitude() != null){
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("foodName", d.getFoodName());
            map.put("qty", d.getQty());
            map.put("expiry", d.getExpiry().toString());
            map.put("pickupAddress", d.getPickupAddress());
            map.put("restaurantName", d.getRestaurantName());
            map.put("lat", d.getLatitude());
            map.put("lng", d.getLongitude());
            result.add(map);
        }
    }
    return result;
}

}
