

package com.paylite.paymentservice.modules.payment;

import com.paylite.paymentservice.modules.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByPaymentId(String paymentId);

    boolean existsByPaymentId(String paymentId);

    @Query("SELECT p FROM Payment p WHERE p.paymentId = :paymentId")
    Optional<Payment> findPaymentByPaymentId(@Param("paymentId") String paymentId);
}

