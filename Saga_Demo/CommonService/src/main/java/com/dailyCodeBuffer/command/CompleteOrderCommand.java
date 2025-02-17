package com.dailyCodeBuffer.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompleteOrderCommand {

	@TargetAggregateIdentifier
	private String orderId;
	private String orderStatus = "COMPLETED";
}
