package com.bharath.core.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.bharath.core.model.Payment;

/**
 * Business operations on payments.
 */
public interface PaymentService {

	/**
	 * Validates and stores a new payment in {@code PENDING} state.
	 *
	 * @param amount       must be greater than zero
	 * @param currencyCode ISO 4217 code, e.g. {@code "USD"}
	 * @throws IllegalArgumentException if the amount is not positive or the
	 *                                  currency code is not recognised
	 */
	Payment createPayment(BigDecimal amount, String currencyCode);

	/**
	 * Moves a pending payment to {@code COMPLETED}.
	 *
	 * @throws PaymentNotFoundException if no payment has the given id
	 * @throws IllegalStateException    if the payment already reached a terminal state
	 */
	Payment complete(String id);

	/**
	 * Moves a pending payment to {@code FAILED}.
	 *
	 * @throws PaymentNotFoundException if no payment has the given id
	 * @throws IllegalStateException    if the payment already reached a terminal state
	 */
	Payment fail(String id);

	Optional<Payment> findById(String id);

	List<Payment> findAll();
}
