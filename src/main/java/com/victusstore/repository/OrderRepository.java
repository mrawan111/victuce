package com.victusstore.repository;

import com.victusstore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByAccount_Email(String email);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.variant v LEFT JOIN FETCH v.product WHERE o.orderId = ?1")
    Order findOrderWithItems(Long orderId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.variant v LEFT JOIN FETCH v.product")
    List<Order> findAllOrdersWithItems();

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.variant v LEFT JOIN FETCH v.product WHERE o.account.email = ?1")
    List<Order> findOrdersByEmailWithItems(String email);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.variant v LEFT JOIN FETCH v.product WHERE o.orderId = ?1")
    Order findByIdWithItems(Long id);
}
