package testingArena;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ CreditCardTest.class, GateSensorTest.class, GateTest.class, LPRTest.class, MessageTest.class,
		OperatorTest.class, PaymentCollectorTest.class, ReportTest.class, ShareFunctionsTest.class, TicketTest.class })
public class AllTests {

}
