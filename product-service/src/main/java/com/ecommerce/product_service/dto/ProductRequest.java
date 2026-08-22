package com.ecommerce.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {
    
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private BigDecimal price;

    //checking code confirm 


    @NotNull
    private Long categoryId;
}
