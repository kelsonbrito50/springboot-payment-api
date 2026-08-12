package com.bharath.core.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * An immutable payment. State transitions return a new instance rather than
 * mutating this one, so a {@code Payment} can be shared freely across threads.
 */
public record Payment(String id, BigDecimal amount, Currency currency, PaymentStatus status) {

	public Payment {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(amount, "amount must not be null");
		Objects.requireNonNull(currency, "currency must not be null");
		Objects.requireNonNull(status, "status must not be null");
	}

	/**
	 * @return a copy of this payment in the given state
	 * @throws IllegalStateException if this payment has already reached a terminal state
	 */
	public Payment withStatus(PaymentStatus newStatus) {
		if (status.isTerminal()) {
			throw new IllegalStateException(
					"Payment " + id + " is already " + status + " and cannot become " + newStatus);
		}
		return new Payment(id, amount, currency, newStatus);
	}
}
