package com.pavanelthepu;

import lombok.Data;

@Data
public class TodoItem {

	private Integer id;
	
	private String description;
	
	private boolean status;
}
