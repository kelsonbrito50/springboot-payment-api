package com.bharath.core.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bharath.core.dao.PaymentDAO;
import com.bharath.core.model.Payment;
import com.bharath.core.model.PaymentStatus;

/**
 * Unit tests for the business rules. No Spring context is started: the DAO is a
 * mock, which is the practical payoff of depending on the interface.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

	private static final Currency USD = Currency.getInstance("USD");

	@Mock
	private PaymentDAO dao;

	@InjectMocks
	private PaymentServiceImpl service;

	@Test
	void createPaymentStoresAPendingPayment() {
		when(dao.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Payment created = service.createPayment(new BigDecimal("42.50"), "USD");

		assertThat(created.id()).isNotBlank();
		assertThat(created.amount()).isEqualByComparingTo("42.50");
		assertThat(created.currency()).isEqualTo(USD);
		assertThat(created.status()).isEqualTo(PaymentStatus.PENDING);
		verify(dao).save(created);
	}

	@ParameterizedTest
	@ValueSource(strings = { "0", "-0.01", "-100" })
	void createPaymentRejectsNonPositiveAmounts(String amount) {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> service.createPayment(new BigDecimal(amount), "USD"))
				.withMessageContaining("greater than zero");

		verify(dao, never()).save(any());
	}

	@Test
	void createPaymentRejectsUnknownCurrency() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> service.createPayment(BigDecimal.TEN, "XYZZY"))
				.withMessageContaining("Unknown ISO 4217 currency code");

		verify(dao, never()).save(any());
	}

	@Test
	void completeMovesAPendingPaymentToCompleted() {
		Payment pending = new Payment("p-1", BigDecimal.TEN, USD, PaymentStatus.PENDING);
		when(dao.findById("p-1")).thenReturn(Optional.of(pending));
		when(dao.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Payment completed = service.complete("p-1");

		assertThat(completed.status()).isEqualTo(PaymentStatus.COMPLETED);
		assertThat(completed.id()).isEqualTo("p-1");
	}

	@Test
	void completeRejectsAPaymentThatAlreadyReachedATerminalState() {
		Payment alreadyDone = new Payment("p-2", BigDecimal.TEN, USD, PaymentStatus.COMPLETED);
		when(dao.findById("p-2")).thenReturn(Optional.of(alreadyDone));

		assertThatExceptionOfType(IllegalStateException.class)
				.isThrownBy(() -> service.complete("p-2"))
				.withMessageContaining("already COMPLETED");

		verify(dao, never()).save(any());
	}

	@Test
	void completeThrowsWhenThePaymentIsUnknown() {
		when(dao.findById("missing")).thenReturn(Optional.empty());

		assertThatExceptionOfType(PaymentNotFoundException.class)
				.isThrownBy(() -> service.complete("missing"))
				.withMessageContaining("missing");
	}

	@Test
	void failMovesAPendingPaymentToFailed() {
		Payment pending = new Payment("p-3", BigDecimal.ONE, USD, PaymentStatus.PENDING);
		when(dao.findById("p-3")).thenReturn(Optional.of(pending));
		when(dao.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assertThat(service.fail("p-3").status()).isEqualTo(PaymentStatus.FAILED);
	}
}
