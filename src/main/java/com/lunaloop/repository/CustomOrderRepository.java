package com.lunaloop.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lunaloop.backend.entity.CustomOrder;

public interface CustomOrderRepository extends JpaRepository<CustomOrder, Long> {
}