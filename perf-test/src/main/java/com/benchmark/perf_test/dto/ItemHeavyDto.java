package com.benchmark.perf_test.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemHeavyDto {
    private Long id;
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description; // ~5KB field for heavy payload testing
    private Long categoryId;
}
