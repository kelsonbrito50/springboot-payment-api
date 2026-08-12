package com.bharath.core.dao;

import java.util.List;
import java.util.Optional;

import com.bharath.core.model.Payment;

/**
 * Persistence boundary for {@link Payment}. The service layer depends only on
 * this interface, so the backing store can be swapped without touching business
 * logic.
 */
public interface PaymentDAO {

	/**
	 * Inserts or replaces the payment with a matching id.
	 *
	 * @return the stored payment
	 */
	Payment save(Payment payment);

	Optional<Payment> findById(String id);

	/**
	 * @return every stored payment, in no guaranteed order
	 */
	List<Payment> findAll();
}
