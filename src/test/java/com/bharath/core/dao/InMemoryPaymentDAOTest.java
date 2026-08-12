package com.bharath.core.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bharath.core.model.Payment;
import com.bharath.core.model.PaymentStatus;

class InMemoryPaymentDAOTest {

	private static final Currency EUR = Currency.getInstance("EUR");

	private InMemoryPaymentDAO dao;

	@BeforeEach
	void setUp() {
		dao = new InMemoryPaymentDAO();
	}

	@Test
	void savedPaymentCanBeFoundById() {
		Payment payment = payment("a", PaymentStatus.PENDING);

		dao.save(payment);

		assertThat(dao.findById("a")).contains(payment);
	}

	@Test
	void findByIdIsEmptyForAnUnknownId() {
		assertThat(dao.findById("nope")).isEmpty();
	}

	@Test
	void savingTheSameIdReplacesTheStoredPayment() {
		dao.save(payment("a", PaymentStatus.PENDING));
		dao.save(payment("a", PaymentStatus.COMPLETED));

		assertThat(dao.findAll()).hasSize(1);
		assertThat(dao.findById("a")).get()
				.extracting(Payment::status)
				.isEqualTo(PaymentStatus.COMPLETED);
	}

	@Test
	void findAllReturnsEverythingStored() {
		dao.save(payment("a", PaymentStatus.PENDING));
		dao.save(payment("b", PaymentStatus.PENDING));

		assertThat(dao.findAll())
				.hasSize(2)
				.extracting(Payment::id)
				.containsExactlyInAnyOrder("a", "b");
	}

	private static Payment payment(String id, PaymentStatus status) {
		return new Payment(id, BigDecimal.TEN, EUR, status);
	}
}
