package com.bharath.core.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bharath.core.model.Payment;
import com.bharath.core.model.PaymentStatus;
import com.bharath.core.services.PaymentNotFoundException;
import com.bharath.core.services.PaymentService;

/**
 * Web-layer tests. Only the MVC slice is loaded and the service is mocked, so
 * these cover routing, serialisation, validation and status-code mapping —
 * not business rules.
 */
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

	private static final Currency USD = Currency.getInstance("USD");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PaymentService paymentService;

	@Test
	void createReturns201WithLocationHeader() throws Exception {
		Payment created = new Payment("p-1", new BigDecimal("19.99"), USD, PaymentStatus.PENDING);
		when(paymentService.createPayment(any(), eq("USD"))).thenReturn(created);

		mockMvc.perform(post("/api/payments")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"amount": 19.99, "currency": "USD"}"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/payments/p-1"))
				.andExpect(jsonPath("$.id").value("p-1"))
				.andExpect(jsonPath("$.currency").value("USD"))
				.andExpect(jsonPath("$.status").value("PENDING"));
	}

	@Test
	void createRejectsNonPositiveAmountBeforeReachingTheService() throws Exception {
		mockMvc.perform(post("/api/payments")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"amount": -5, "currency": "USD"}"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("greater than zero")));

		verify(paymentService, never()).createPayment(any(), any());
	}

	@Test
	void createRejectsMalformedCurrencyCode() throws Exception {
		mockMvc.perform(post("/api/payments")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"amount": 10, "currency": "DOLLARS"}"""))
				.andExpect(status().isBadRequest());

		verify(paymentService, never()).createPayment(any(), any());
	}

	@Test
	void findByIdReturnsThePayment() throws Exception {
		when(paymentService.findById("p-1"))
				.thenReturn(Optional.of(new Payment("p-1", BigDecimal.TEN, USD, PaymentStatus.PENDING)));

		mockMvc.perform(get("/api/payments/p-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("p-1"));
	}

	@Test
	void findByIdReturns404WhenMissing() throws Exception {
		when(paymentService.findById("missing")).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/payments/missing"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Payment not found"));
	}

	@Test
	void findAllReturnsTheCollection() throws Exception {
		when(paymentService.findAll()).thenReturn(List.of(
				new Payment("p-1", BigDecimal.TEN, USD, PaymentStatus.PENDING),
				new Payment("p-2", BigDecimal.ONE, USD, PaymentStatus.COMPLETED)));

		mockMvc.perform(get("/api/payments"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[1].status").value("COMPLETED"));
	}

	@Test
	void completeReturnsTheUpdatedPayment() throws Exception {
		when(paymentService.complete("p-1"))
				.thenReturn(new Payment("p-1", BigDecimal.TEN, USD, PaymentStatus.COMPLETED));

		mockMvc.perform(post("/api/payments/p-1/complete"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"));
	}

	@Test
	void completeReturns409WhenAlreadyTerminal() throws Exception {
		when(paymentService.complete("p-1"))
				.thenThrow(new IllegalStateException("Payment p-1 is already COMPLETED and cannot become COMPLETED"));

		mockMvc.perform(post("/api/payments/p-1/complete"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Invalid state transition"));
	}

	@Test
	void completeReturns404ForAnUnknownPayment() throws Exception {
		when(paymentService.complete("missing")).thenThrow(new PaymentNotFoundException("missing"));

		mockMvc.perform(post("/api/payments/missing/complete"))
				.andExpect(status().isNotFound());
	}

	@Test
	void failReturnsTheUpdatedPayment() throws Exception {
		when(paymentService.fail("p-1"))
				.thenReturn(new Payment("p-1", BigDecimal.TEN, USD, PaymentStatus.FAILED));

		mockMvc.perform(post("/api/payments/p-1/fail"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("FAILED"));
	}
}
