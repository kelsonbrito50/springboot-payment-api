package com.bharath.core.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.bharath.core.ContainerConfig;
import com.bharath.core.model.Payment;
import com.bharath.core.model.PaymentStatus;

/**
 * Integration tests for the persistence layer against a real Postgres, with the
 * Flyway migrations applied. These cover the things a mock cannot: that the
 * entity matches the migrated schema, and that the type conversions in
 * {@code PaymentMapper} survive a round trip.
 */
@SpringBootTest
@Import(ContainerConfig.class)
class JpaPaymentDAOTest {

	private static final Currency EUR = Currency.getInstance("EUR");

	@Autowired
	private PaymentDAO dao;

	@Test
	void savedPaymentSurvivesARoundTrip() {
		Payment saved = dao.save(payment(PaymentStatus.PENDING));

		assertThat(dao.findById(saved.id())).contains(saved);
	}

	@Test
	void amountKeepsItsScaleThroughTheDatabase() {
		Payment saved = dao.save(new Payment(
				UUID.randomUUID().toString(), new BigDecimal("19.99"), EUR, PaymentStatus.PENDING));

		assertThat(dao.findById(saved.id())).get()
				.extracting(Payment::amount)
				.isEqualTo(new BigDecimal("19.99"));
	}

	@Test
	void currencyIsRestoredAsACurrencyNotAString() {
		Payment saved = dao.save(payment(PaymentStatus.PENDING));

		assertThat(dao.findById(saved.id())).get()
				.extracting(Payment::currency)
				.isEqualTo(EUR);
	}

	@Test
	void savingAnExistingIdUpdatesRatherThanDuplicating() {
		Payment pending = dao.save(payment(PaymentStatus.PENDING));
		int before = dao.findAll().size();

		dao.save(pending.withStatus(PaymentStatus.COMPLETED));

		assertThat(dao.findAll()).hasSize(before);
		assertThat(dao.findById(pending.id())).get()
				.extracting(Payment::status)
				.isEqualTo(PaymentStatus.COMPLETED);
	}

	@Test
	void findByIdIsEmptyForAnUnknownId() {
		assertThat(dao.findById(UUID.randomUUID().toString())).isEmpty();
	}

	@Test
	void findByIdIsEmptyForAMalformedIdRatherThanThrowing() {
		assertThat(dao.findById("not-a-uuid")).isEmpty();
	}

	@Test
	void findAllReturnsEverythingStored() {
		Payment first = dao.save(payment(PaymentStatus.PENDING));
		Payment second = dao.save(payment(PaymentStatus.FAILED));

		assertThat(dao.findAll())
				.extracting(Payment::id)
				.contains(first.id(), second.id());
	}

	private static Payment payment(PaymentStatus status) {
		return new Payment(UUID.randomUUID().toString(), new BigDecimal("10.00"), EUR, status);
	}
}
