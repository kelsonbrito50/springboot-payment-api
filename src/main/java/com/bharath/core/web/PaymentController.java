package com.bharath.core.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bharath.core.model.Payment;
import com.bharath.core.services.PaymentNotFoundException;
import com.bharath.core.services.PaymentService;

import jakarta.validation.Valid;

/**
 * HTTP entry point. Holds no business logic of its own — it translates requests
 * into service calls and results into status codes.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping
	public ResponseEntity<Payment> create(@Valid @RequestBody CreatePaymentRequest request) {
		Payment created = paymentService.createPayment(request.amount(), request.currency());
		return ResponseEntity.created(URI.create("/api/payments/" + created.id())).body(created);
	}

	@GetMapping
	public List<Payment> findAll() {
		return paymentService.findAll();
	}

	@GetMapping("/{id}")
	public Payment findById(@PathVariable String id) {
		return paymentService.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
	}

	@PostMapping("/{id}/complete")
	public Payment complete(@PathVariable String id) {
		return paymentService.complete(id);
	}

	@PostMapping("/{id}/fail")
	public Payment fail(@PathVariable String id) {
		return paymentService.fail(id);
	}
}
