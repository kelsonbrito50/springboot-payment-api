package com.bharath.core.model;

/**
 * Lifecycle of a payment. A payment is created as {@link #PENDING} and moves
 * exactly once to a terminal state.
 */
public enum PaymentStatus {

	PENDING,
	COMPLETED,
	FAILED;

	public boolean isTerminal() {
		return this != PENDING;
	}
}
