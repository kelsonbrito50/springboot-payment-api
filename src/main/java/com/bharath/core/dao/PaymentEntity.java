package com.bharath.core.dao;

import java.math.BigDecimal;
import java.util.UUID;

import com.bharath.core.model.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Database representation of a payment. Deliberately separate from the
 * {@link com.bharath.core.model.Payment} domain record: the entity is mutable
 * because JPA requires it, while the domain type stays immutable. Translation
 * between the two lives in {@link PaymentMapper}.
 */
@Entity
@Table(name = "payments")
public class PaymentEntity {

	@Id
	private UUID id;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	/** ISO 4217 code, stored as three characters rather than a currency object. */
	@Column(nullable = false, length = 3)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private PaymentStatus status;

	/** Required by JPA. */
	protected PaymentEntity() {
	}

	public PaymentEntity(UUID id, BigDecimal amount, String currency, PaymentStatus status) {
		this.id = id;
		this.amount = amount;
		this.currency = currency;
		this.status = status;
	}

	public UUID getId() {
		return id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}
}
