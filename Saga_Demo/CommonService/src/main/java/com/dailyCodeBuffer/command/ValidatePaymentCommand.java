package com.dailyCodeBuffer.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import com.dailyCodeBuffer.model.CardDetails;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidatePaymentCommand {

	@TargetAggregateIdentifier
	private String paymentId;
	
	private String orderId;
	
	private CardDetails cardDetails;
}
