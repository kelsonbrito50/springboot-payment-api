package com.bharath.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.bharath.core.dao.PaymentDAO;
import com.bharath.core.model.Payment;
import com.bharath.core.model.PaymentStatus;
import com.bharath.core.services.PaymentService;

/**
 * Verifies that the application context starts and that the beans are wired
 * together as expected. Business rules are covered by {@code PaymentServiceImplTest},
 * which needs no context.
 */
@SpringBootTest
class CoreApplicationTests {

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private PaymentDAO paymentDAO;

	@Test
	void contextLoadsAndInjectsCollaborators() {
		assertThat(paymentService).isNotNull();
		assertThat(paymentDAO).isNotNull();
	}

	@Test
	void createdPaymentIsVisibleThroughTheDao() {
		Payment created = paymentService.createPayment(new BigDecimal("19.99"), "USD");

		assertThat(paymentDAO.findById(created.id()))
				.contains(created);
		assertThat(created.status()).isEqualTo(PaymentStatus.PENDING);
	}
}
