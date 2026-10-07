package com.lunaloop.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lunaloop.backend.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}