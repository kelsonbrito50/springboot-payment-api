package com.bharath.core.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bharath.core.model.Payment;

/**
 * Postgres-backed {@link PaymentDAO}. This is the only class aware that
 * persistence is JPA at all — the service layer still sees the interface.
 */
@Repository
public class JpaPaymentDAO implements PaymentDAO {

	private final PaymentJpaRepository repository;

	JpaPaymentDAO(PaymentJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public Payment save(Payment payment) {
		return PaymentMapper.toDomain(repository.save(PaymentMapper.toEntity(payment)));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Payment> findById(String id) {
		return parseId(id)
				.flatMap(repository::findById)
				.map(PaymentMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Payment> findAll() {
		return repository.findAll().stream()
				.map(PaymentMapper::toDomain)
				.toList();
	}

	/**
	 * A malformed id is a miss, not a crash — callers pass whatever arrived on
	 * the URL, and {@code /api/payments/banana} should be a 404.
	 */
	private static Optional<UUID> parseId(String id) {
		try {
			return Optional.of(UUID.fromString(id));
		}
		catch (IllegalArgumentException | NullPointerException ex) {
			return Optional.empty();
		}
	}
}
