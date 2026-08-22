package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.dto.ProductRequest;
import com.ecommerce.product_service.dto.ProductResponse;
import com.ecommerce.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
private final ProductService productService;

@PostMapping
public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request){
    return ResponseEntity.ok(productService.create(request));
}

@GetMapping
public ResponseEntity<List<ProductResponse>> getAll(){
    return ResponseEntity.ok(productService.getAll());
}

@GetMapping("/{id}")
public ResponseEntity<ProductResponse> getById(@PathVariable Long id){
    return ResponseEntity.ok  (productService.getById(id));
}

@PutMapping("/{id}")
public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request){
    return ResponseEntity.ok(productService.update(id,request));
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete (@PathVariable Long id){
    productService.delete(id);
    return ResponseEntity.noContent().build();
}
}
