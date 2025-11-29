package testingArena;

//RUNS ON JUNIT 5
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import parkingGarage.GateSensor;

class GateSensorTest {

	//getCarDetected()
	@Test
	void getCarDetectedTest() {
		GateSensor gateSensor = new GateSensor();
		assertFalse(gateSensor.getCarDetected());
	}
	//getisCarExited()
		@Test
		void isCarExitedTest() {
			GateSensor gateSensor = new GateSensor();
			assertFalse(gateSensor.isCarExited());
		}
	//reset()
	@Test
	void resetTest() {
		GateSensor gateSensor = new GateSensor();
		gateSensor.reset();
		assertTrue(gateSensor.getCarDetected() == false && gateSensor.isCarExited() == false);
	}
	//Unable to test run();
}
