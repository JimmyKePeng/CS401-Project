package testingArena;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//RUNS ON JUNIT 5
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import parkingGarage.LicensePlateReader;
import parkingGarage.Location;

//Change Test Method Order due to static variables
@TestMethodOrder(OrderAnnotation.class)
class LPRTest {

	//getGarageID
	@ParameterizedTest
	@Order(2)
	@ValueSource(ints = {1,2,3,4,5,6})
	void getGarageIDTest(int values) {
		BlockingQueue<String> testQueue = new LinkedBlockingQueue<String>();
		LicensePlateReader lpr = 
				new LicensePlateReader(values, Location.Entry, testQueue);
		assertEquals(values, lpr.getGarageID());
	}
	//getLPRid
	@ParameterizedTest
	@Order(1) //first in order due to static count variable
	@ValueSource(ints = {1,2,3,4,5,6})
	void getLPRidTest(int values) {
		BlockingQueue<String> testQueue = new LinkedBlockingQueue<String>();
		LicensePlateReader lpr2 = 
				new LicensePlateReader(values, Location.Entry, testQueue);
		assertEquals((values-1), lpr2.getLPRid());
	}
	//randomPlate
	@RepeatedTest(100)
	@Order(3)
	void randomPlateTest() {
		LicensePlateReader lpr = new LicensePlateReader();
		char[] lprPlate = lpr.randomPlate().toCharArray();
		for(char x:lprPlate)
			if(!Character.isDigit(x) && !Character.isLetter(x)) fail(x + " is not alphanumeric");
		assertTrue(lpr.randomPlate().length() == 6);
		//Checks if alphanumeric and correct length
	}
	
	//Unable to test run(), stop()
}
