package com.api.project.Ecomerce.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Long categoryId;
}