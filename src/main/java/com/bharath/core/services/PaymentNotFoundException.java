package com.bharath.core.services;

/**
 * Thrown when an operation references a payment id that does not exist.
 */
public class PaymentNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PaymentNotFoundException(String id) {
		super("No payment found with id " + id);
	}
}
