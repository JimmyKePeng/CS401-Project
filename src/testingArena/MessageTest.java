package testingArena;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import parkingGarage.Message;
import parkingGarage.MsgTypes;
import parkingGarage.Operator;

class MessageTest {

	//getGarageID() by default constructor
	@Test
	void getGarageIDDCTest() {
		Message msg = new Message();
		assertEquals(0, msg.getGarageID());
	}
	
	//getGarageID() by parameterized constructor
	@ParameterizedTest
	@MethodSource("msgTypeandIntProvider")
	void getGarageIDPCTest(MsgTypes msgType, int garageIDs) {
		Message msg = new Message(msgType, garageIDs);
		assertEquals(msg.getGarageID(), garageIDs);
	}

	//getMsgType() by default constructor
	@Test
	void getMsgTypeDCTest() {
		Message msg = new Message();
		assertEquals(MsgTypes.UNDEFINED, msg.getMsgType());
	}
	
	//getMsgType() by Parameterized Constructor
	@ParameterizedTest
	@MethodSource("msgTypeandIntProvider")
	void getMsgTypePCTest(MsgTypes msgType, int garageIDs){
		Message msg = new Message(msgType, garageIDs);
		assertEquals(msgType, msg.getMsgType());
	}
	
	//getOperator()
	@Test
	void getOperatorTest() {
		Message msg = new Message();
		assertEquals(null, msg.getOperator());
	}
	@Test
	//setOperator()
	void setOperatorTest() {
		Message msg = new Message();
		Operator operator = new Operator();
		if(msg.getOperator() != null) fail("Message object incorrectly returned Operator object.");
		msg.setOperator(operator);
		assertEquals(operator, msg.getOperator());
	}
	static Stream<Arguments> msgTypeandIntProvider(){
		return Stream.of(
				Arguments.of(MsgTypes.GETREPORT, 0),
				Arguments.of(MsgTypes.NEWGARAGE, 1),
				Arguments.of(MsgTypes.OPERATORFAILURE, 2),
				Arguments.of(MsgTypes.TICKETPAID, 3)
				);
	}
}
