package parkingGarage;

import java.util.Random;

/* Credit Card Class;
 * Its only public function is to return a String 
 containing an illegal or legal number of numbers
 that can be classified as a credit card number.
 
 * Generates Credit Card # upon instantiation. */

public class CreditCard {
	private String cardNum;

	//Public Constructor
	public CreditCard() {
		this.cardNum = generateCardNum();
	}

	//Private Function Generates legal or illegal CC #
	private String generateCardNum() {
		String str = "";
		Random random = new Random();

		// pretend to have an invalid credit card number;
		if (random.nextInt(6) % 5 == 0) {
			return "0000";
		}

		// else get 16 digit card number;
		for (int i = 1; i < 17; i++) {
			str += Integer.toString(random.nextInt(10));
			if (i % 4 == 0) {
				str += " ";
			}
		}
		return str;
	}
	
	//Public Function Returns CC#
	public String getCardNum() {
		return cardNum;
	}
}
