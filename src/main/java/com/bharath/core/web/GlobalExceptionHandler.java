package com.bharath.core.web;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.bharath.core.services.PaymentNotFoundException;

/**
 * Maps domain exceptions onto HTTP status codes so controllers stay free of
 * error-handling branches. Responses use RFC 9457 {@code application/problem+json}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/** Unknown payment id. */
	@ExceptionHandler(PaymentNotFoundException.class)
	public ProblemDetail handleNotFound(PaymentNotFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, "Payment not found", ex.getMessage());
	}

	/** Invalid amount or unrecognised currency code. */
	@ExceptionHandler(IllegalArgumentException.class)
	public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
		return problem(HttpStatus.BAD_REQUEST, "Invalid payment", ex.getMessage());
	}

	/** Transition attempted on a payment that already reached a terminal state. */
	@ExceptionHandler(IllegalStateException.class)
	public ProblemDetail handleConflict(IllegalStateException ex) {
		return problem(HttpStatus.CONFLICT, "Invalid state transition", ex.getMessage());
	}

	/** Bean Validation failures on the request body. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		String detail = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return problem(HttpStatus.BAD_REQUEST, "Validation failed", detail);
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setTitle(title);
		return problemDetail;
	}
}
