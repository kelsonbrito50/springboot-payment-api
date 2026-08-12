package com.bharath.core.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.bharath.core.model.Payment;

/**
 * Map-backed {@link PaymentDAO}. Data lives only for the life of the JVM, which
 * keeps the demo dependency-free; a JDBC or JPA implementation would drop in
 * here without any change to {@code PaymentService}.
 */
@Repository
public class InMemoryPaymentDAO implements PaymentDAO {

	private final Map<String, Payment> store = new ConcurrentHashMap<>();

	@Override
	public Payment save(Payment payment) {
		store.put(payment.id(), payment);
		return payment;
	}

	@Override
	public Optional<Payment> findById(String id) {
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public List<Payment> findAll() {
		return List.copyOf(store.values());
	}
}
