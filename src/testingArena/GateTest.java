package testingArena;

//RUINS ON JUNIT 5
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import parkingGarage.Gate;
import parkingGarage.Location;

class GateTest {

	//getGarageID
	@ParameterizedTest
	@ValueSource(ints = {1,2,3,4,5,6})
	void getGarageIDTest(int values) {
		Gate gate = new Gate(values, Location.Entry);
		assertEquals(values, gate.getGarageID());
	}
	//getGateID
	@ParameterizedTest
	@ValueSource(ints = {1,2,3,4,5,6})
	void getGateIDTest(int values) {
		Gate gate = new Gate(values, Location.Entry);
		assertEquals((values-1), gate.getGateID());
	}
	//getGateType
	@ParameterizedTest
	@EnumSource(Location.class)
	void getGateTypeTest(Location locations) {
		Gate gate = new Gate(99, locations);
		gate.getGateType();
	}
	//isGateOpen()
	@Test
	void isGateOpenTest() {
		Gate gate = new Gate(1, Location.Exit);
		assertNotEquals(true, gate.isGateOpen());
	}
	//Unable to test openGate() and run();
}
