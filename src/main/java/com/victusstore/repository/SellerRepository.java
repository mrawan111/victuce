package com.victusstore.repository;

import com.victusstore.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    List<Seller> findByEmail(String email);
}
