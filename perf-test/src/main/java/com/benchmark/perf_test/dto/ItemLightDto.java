package com.benchmark.perf_test.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemLightDto {
    private Long id;
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
}
