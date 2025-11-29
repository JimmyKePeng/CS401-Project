package parkingGarage;

/*PaymentCollector, or the payment processor, would also realistically 
	receive information from us, the vendor, to process payment.
	Payment processor would manipulate information received 
	though this is would be abstract to us, since their API would
	be the most we would associate with. 
	*/
public class PaymentCollector {

	private CreditCard card;

	public PaymentCollector(CreditCard card) {
		this.card = card;
	};

	public boolean validatePayment() {
		return card.getCardNum().length() <= 20 && card.getCardNum().length() > 13; //validates length of CC 
	}
}