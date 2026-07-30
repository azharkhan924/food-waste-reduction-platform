package com.foodwaste.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodwaste.entity.Donation;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long>{

List<Donation> findByRestaurantId(Long restaurantId);

List<Donation> findByStatus(String status);

List<Donation> findByNgoId(Long ngoId);

}
