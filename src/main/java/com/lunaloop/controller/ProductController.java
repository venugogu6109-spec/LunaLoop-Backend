
package com.lunaloop.backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.lunaloop.backend.entity.Product;
import com.lunaloop.backend.service.ProductService;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    private final ProductService productService;

    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET all products
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // GET product by ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long id) {

        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST - add product using JSON (existing functionality)
    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }

    // POST - add product with an uploaded image
    @PostMapping(
            value = "/with-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Product addProductWithImage(
            @RequestParam("name") String name,
            @RequestParam("category") String category,
            @RequestParam(value = "description", required = false)
                    String description,
            @RequestParam("price") double price,
            @RequestParam("stock") int stock,
            @RequestParam("image") MultipartFile image) {

        if (name == null || name.isBlank()
                || category == null || category.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product name and category are required."
            );
        }

        if (!Double.isFinite(price) || price <= 0 || stock < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Price must be positive and stock cannot be negative."
            );
        }

        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a product image."
            );
        }

        if (image.getSize() > 5L * 1024 * 1024) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Image size must not exceed 5 MB."
            );
        }

        String contentType = image.getContentType();

        if (contentType == null
                || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Use a JPG, PNG or WebP image."
            );
        }

        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported image type."
            );
        };

        String filename = UUID.randomUUID() + extension;

        Path uploadDirectory = Paths.get(
                "uploads", "products"
        ).toAbsolutePath().normalize();

        Path destination = uploadDirectory.resolve(filename).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid upload path."
            );
        }

        try {
            Files.createDirectories(uploadDirectory);
            image.transferTo(destination);
        } catch (IOException | IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save the product image."
            );
        }

        String imageUrl =
                "http://localhost:8080/uploads/products/" + filename;

        Product product = new Product(
                name.trim(),
                category.trim(),
                description == null ? "" : description.trim(),
                price,
                stock,
                imageUrl
        );

        return productService.addProduct(product);
    }

    // PUT - update product
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        try {
            return ResponseEntity.ok(
                    productService.updateProduct(id, product)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE - delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id) {

        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // INCREASE STOCK
    @PutMapping("/{id}/stock/increase")
    public ResponseEntity<Product> increaseStock(
            @PathVariable Long id,
            @RequestBody StockRequest request) {

        try {
            return ResponseEntity.ok(
                    productService.increaseStock(
                            id, request.quantity
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DECREASE STOCK
    @PutMapping("/{id}/stock/decrease")
    public ResponseEntity<Product> decreaseStock(
            @PathVariable Long id,
            @RequestBody StockRequest request) {

        try {
            return ResponseEntity.ok(
                    productService.decreaseStock(
                            id, request.quantity
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Request body for stock operations
    public static class StockRequest {

        public int quantity;

        public StockRequest() {
        }

        public StockRequest(int quantity) {
            this.quantity = quantity;
        }
    }
}
