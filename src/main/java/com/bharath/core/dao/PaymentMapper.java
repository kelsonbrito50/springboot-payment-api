package com.bharath.core.dao;

import java.util.Currency;
import java.util.UUID;

import com.bharath.core.model.Payment;

/**
 * Translates between the persistence and domain representations. Keeping this
 * in one place is what lets the domain model use {@link Currency} and an
 * immutable record while the table stores a three-character code.
 */
final class PaymentMapper {

	private PaymentMapper() {
	}

	static Payment toDomain(PaymentEntity entity) {
		return new Payment(
				entity.getId().toString(),
				entity.getAmount(),
				Currency.getInstance(entity.getCurrency()),
				entity.getStatus());
	}

	static PaymentEntity toEntity(Payment payment) {
		return new PaymentEntity(
				UUID.fromString(payment.id()),
				payment.amount(),
				payment.currency().getCurrencyCode(),
				payment.status());
	}
}
