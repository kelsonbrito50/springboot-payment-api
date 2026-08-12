package com.bharath.core.dao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository over {@link PaymentEntity}. Package-private on purpose:
 * only {@link JpaPaymentDAO} may touch it, so the rest of the application keeps
 * depending on {@link PaymentDAO} rather than on Spring Data.
 */
interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {
}
