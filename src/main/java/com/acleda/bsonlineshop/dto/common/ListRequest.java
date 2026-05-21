package com.acleda.bsonlineshop.dto.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ListRequest {
    private int pageNumber = 0;
    private int size = 10;
    private String sortProperty = "createdAt";
    private String sortDirection = "DESC";
    private String search;
    private Map<String, String> filters; // dynamic filters e.g. category, status
    private java.math.BigDecimal minPrice;
    private java.math.BigDecimal maxPrice;
}