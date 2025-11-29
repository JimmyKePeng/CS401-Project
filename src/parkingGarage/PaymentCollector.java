package parkingGarage;

public class PaymentCollector {

	private CreditCard card;

	public PaymentCollector(CreditCard card) {
		this.card = card;
	};

	public boolean validatePayment() {

		return card.getCardNum().length() <= 20 && card.getCardNum().length() > 13;

	}
}