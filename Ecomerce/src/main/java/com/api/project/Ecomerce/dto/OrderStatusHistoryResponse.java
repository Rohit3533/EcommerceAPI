package com.api.project.Ecomerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryResponse {
    private Long id;
    private String status;
    private String note;
    private LocalDateTime createdAt;
    private String createdBy;
}
