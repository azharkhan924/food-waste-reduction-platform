package com.foodwaste.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.foodwaste.entity.Donation;
import java.util.List;
import java.time.LocalDateTime;

public interface DonationRepository extends JpaRepository<Donation, Long>{

List<Donation> findByRestaurantId(Long restaurantId);

List<Donation> findByStatus(String status);

List<Donation> findByNgoId(Long ngoId);

long countByStatus(String status);

List<Donation> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

List<Donation> findByStatusAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end);

@Query("SELECT COALESCE(SUM(d.qty), 0) FROM Donation d")
long sumTotalQty();

@Query("SELECT COALESCE(SUM(d.qty), 0) FROM Donation d WHERE d.status = ?1")
long sumQtyByStatus(String status);

long countByNgoIdIsNotNull();

}
