package com.lunaloop.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.lunaloop.backend.entity.CustomOrder;
import com.lunaloop.backend.repository.CustomOrderRepository;

@RestController
@RequestMapping("/api/custom-orders")
@CrossOrigin(origins = "http://localhost:5173")
public class CustomOrderController {

    private final CustomOrderRepository customOrderRepository;

    public CustomOrderController(CustomOrderRepository customOrderRepository) {
        this.customOrderRepository = customOrderRepository;
    }

    @PostMapping
    public CustomOrder createCustomOrder(@RequestBody CustomOrder order) {
        if (order.getCustomerName() == null || order.getCustomerName().isBlank()
                || order.getMobile() == null || order.getMobile().isBlank()
                || order.getProductType() == null || order.getProductType().isBlank()
                || order.getDescription() == null || order.getDescription().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Name, mobile, product type, and description are required."
            );
        }

        order.setStatus("NEW");
order.setAdminNotes(null);

        return customOrderRepository.save(order);
    }

    @GetMapping
    public List<CustomOrder> getAllCustomOrders() {
        return customOrderRepository.findAll();
    }

    @PutMapping("/{id}")
    public CustomOrder updateCustomOrder(
            @PathVariable Long id,
            @RequestBody CustomOrder updatedOrder) {

        CustomOrder existingOrder = customOrderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Custom order not found."
                ));

        if (updatedOrder.getStatus() != null
                && !updatedOrder.getStatus().isBlank()) {
            existingOrder.setStatus(updatedOrder.getStatus());
        }

        existingOrder.setAdminNotes(updatedOrder.getAdminNotes());

        return customOrderRepository.save(existingOrder);
    }
}