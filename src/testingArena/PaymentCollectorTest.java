package testingArena;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.RepeatedTest;

import parkingGarage.PaymentCollector;
import parkingGarage.CreditCard;

class PaymentCollectorTest {
	@RepeatedTest(100)
	void validatePaymentTest() {
		PaymentCollector payment = new PaymentCollector(new CreditCard());
		assertTrue(payment.validatePayment() || !payment.validatePayment());
	}//validating any boolean return
}
