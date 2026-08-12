package com.bharath.core;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.bharath.core.model.Payment;
import com.bharath.core.services.PaymentService;

/**
 * Walks through a short payment scenario on startup so the application does
 * something visible without a web layer.
 *
 * <p>Guarded by the {@code demo} profile so it never runs during tests:
 * {@code ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo}
 */
@Component
@Profile("demo")
public class DemoRunner implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoRunner.class);

	private final PaymentService paymentService;

	public DemoRunner(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@Override
	public void run(String... args) {
		Payment first = paymentService.createPayment(new BigDecimal("19.99"), "USD");
		Payment second = paymentService.createPayment(new BigDecimal("250.00"), "EUR");
		log.info("Created {}", first);
		log.info("Created {}", second);

		log.info("Completed {}", paymentService.complete(first.id()));
		log.info("Failed    {}", paymentService.fail(second.id()));

		try {
			paymentService.complete(first.id());
		}
		catch (IllegalStateException ex) {
			log.info("Rejected re-completion as expected: {}", ex.getMessage());
		}

		try {
			paymentService.createPayment(new BigDecimal("-5.00"), "USD");
		}
		catch (IllegalArgumentException ex) {
			log.info("Rejected negative amount as expected: {}", ex.getMessage());
		}

		log.info("Stored payments: {}", paymentService.findAll().size());
	}
}
