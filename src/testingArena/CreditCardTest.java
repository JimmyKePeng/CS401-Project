package testingArena;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.RepeatedTest;

import parkingGarage.CreditCard;

class CreditCardTest {

	//getCardNum()
	//Each instance of CreditCredit generates new number
	@RepeatedTest(100)
	void getCardNumTest() {
		CreditCard cc = new CreditCard();
		
		char[] ccString = cc.getCardNum().toCharArray();	//converts to char array
		for(int x = 0; x < ccString.length; x++) {
			if(ccString[x] == ' ') break;
			if(!Character.isDigit(ccString[x])) fail(ccString[x] + " is not a valid digit.");
			//validates credit card digits, fails if not digit
		}
		
		assertTrue(cc.getCardNum() == "0000" || cc.getCardNum().length() == 20);
		//Checks if user-made 'invalid' output or valid credit card length 
	}
}
