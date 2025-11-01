package com.victusstore.repository;

import com.victusstore.model.AdminActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdminActivityRepository extends JpaRepository<AdminActivity, Long> {
    List<AdminActivity> findByAdminEmail(String adminEmail);
    List<AdminActivity> findByEntityType(String entityType);
    List<AdminActivity> findByActionType(String actionType);
    Page<AdminActivity> findByAdminEmailOrderByCreatedAtDesc(String adminEmail, Pageable pageable);
    Page<AdminActivity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

