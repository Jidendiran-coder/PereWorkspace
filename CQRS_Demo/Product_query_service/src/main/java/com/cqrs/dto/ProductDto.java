package com.cqrs.dto;

import lombok.Data;

@Data
public class ProductDto {
	
	private String name;
	
	private String description;
	
	private double price;
}
