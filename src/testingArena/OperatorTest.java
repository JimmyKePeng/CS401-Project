package testingArena;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import parkingGarage.Operator;
import parkingGarage.Report;

class OperatorTest {

	//getReport
	@Test
	void getReportTest() {
		Operator operator = new Operator();
		assertEquals(null,operator.getReport());
	}
	//getGarageID
	@ParameterizedTest
	@ValueSource(ints = {1,2,3,4,5,6,7})
	void getGarageIDTest(int values) {
		Operator operator = new Operator("Johnson", "Password123", values);
		assertEquals(values,operator.getGarageID());
	}
	//getUserName
	@ParameterizedTest
	@ValueSource(strings = {"Diego", "Kegang", "Matt", "Chris", "Tuan"})
	void getUserNameTest(String usernames) {
		Operator operator = new Operator(usernames, "password", 1);
		assertEquals(usernames, operator.getUsername());
	}
	//getPassword
	@ParameterizedTest
	@ValueSource(strings = {"password1", "password2", "password3", "password4", "password5"})
	void getPasswordTest(String passwords) {
		Operator operator = new Operator("John Doe", passwords, 1);
		assertEquals(passwords, operator.getPassword());
	}
	//setReport
	@Test
	void setReportTest() {
		Operator operator = new Operator();
		Report report = new Report();
		operator.setReport(report);
		assertEquals(report,operator.getReport());
	}
}
