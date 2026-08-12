package com.bharath.core.web;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a payment. Bean Validation catches the malformed
 * cases at the edge; the service still enforces the same rules, so it stays
 * correct when called from outside the web layer.
 */
public record CreatePaymentRequest(

		@NotNull(message = "amount is required")
		@DecimalMin(value = "0.00", inclusive = false, message = "amount must be greater than zero")
		BigDecimal amount,

		@NotBlank(message = "currency is required")
		@Size(min = 3, max = 3, message = "currency must be a 3-letter ISO 4217 code")
		String currency) {
}
