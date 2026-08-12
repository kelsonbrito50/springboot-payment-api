package com.bharath.core.services;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.bharath.core.dao.PaymentDAO;
import com.bharath.core.model.Payment;
import com.bharath.core.model.PaymentStatus;

@Service
public class PaymentServiceImpl implements PaymentService {

	private final PaymentDAO dao;

	/**
	 * Constructor injection: the dependency is final, the object is never
	 * half-built, and the class can be unit tested with a plain mock and no
	 * Spring context at all.
	 */
	public PaymentServiceImpl(PaymentDAO dao) {
		this.dao = dao;
	}

	@Override
	public Payment createPayment(BigDecimal amount, String currencyCode) {
		if (amount == null || amount.signum() <= 0) {
			throw new IllegalArgumentException("Payment amount must be greater than zero, got " + amount);
		}
		Payment payment = new Payment(UUID.randomUUID().toString(), amount, toCurrency(currencyCode),
				PaymentStatus.PENDING);
		return dao.save(payment);
	}

	@Override
	public Payment complete(String id) {
		return transitionTo(id, PaymentStatus.COMPLETED);
	}

	@Override
	public Payment fail(String id) {
		return transitionTo(id, PaymentStatus.FAILED);
	}

	@Override
	public Optional<Payment> findById(String id) {
		return dao.findById(id);
	}

	@Override
	public List<Payment> findAll() {
		return dao.findAll();
	}

	private Payment transitionTo(String id, PaymentStatus target) {
		Payment payment = dao.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
		return dao.save(payment.withStatus(target));
	}

	private static Currency toCurrency(String currencyCode) {
		try {
			return Currency.getInstance(currencyCode);
		}
		catch (NullPointerException | IllegalArgumentException ex) {
			throw new IllegalArgumentException("Unknown ISO 4217 currency code: " + currencyCode, ex);
		}
	}
}
