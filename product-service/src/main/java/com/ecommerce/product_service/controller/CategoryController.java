package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.dto.CategoryRequest;
import com.ecommerce.product_service.dto.CategoryResponse;
import com.ecommerce.product_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor

public class CategoryController {
    
private final CategoryService categoryService;

@PostMapping
public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request){
    return ResponseEntity.ok(categoryService.create(request));
}

@GetMapping
public ResponseEntity<List<CategoryResponse>> getAll(){
    return ResponseEntity.ok(categoryService.getAll());
}

}
