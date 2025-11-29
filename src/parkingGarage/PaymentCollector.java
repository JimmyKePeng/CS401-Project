package parkingGarage;

public class PaymentCollector {

	private CreditCard card;

	public PaymentCollector(CreditCard card) {
		this.card = card;
	};

	// paymentCollect validate a credit card by check the number of digits.
	public boolean validatePayment() {
		return card.getCardNum().length() <= 20 && card.getCardNum().length() > 13;

	}
}