package testingArena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import parkingGarage.Report;
import parkingGarage.Ticket;

class ReportTest {

	@Test

	// this is testing if the constructor that takes a file actually works as
	// intended when a missing file is passed in
	void testConstructorWithMissingFile() {

		String NoFileExist = "Nowaythisfileexistsinoursystem3000@noway.txt";
		Report myReport = new Report(NoFileExist);

		assertEquals(-1, myReport.getGarageId());
		assertEquals(-1, myReport.getAvgStayTIme());
		assertEquals(-1, myReport.getTotalFee(), 0.0001);
		assertNull(myReport.creationDate());
		assertEquals(-1, myReport.getMonth());
		assertEquals(-1, myReport.getYear());

	}

	@Test
	// my test here is to make sure that the method actually calculates the correct
	// avg
	void testAvgStayTimeCalculation() {

		Ticket ticket1 = new Ticket();
		ticket1.setDurationOfStay(Duration.ofSeconds(60));

		Ticket ticket2 = new Ticket();
		ticket2.setDurationOfStay(Duration.ofSeconds(120));

		List<Ticket> tickets = new ArrayList<>();
		tickets.add(ticket1);
		tickets.add(ticket2);

		Report myReport = new Report(5, tickets);

		// average should be (60 + 120) / 2 = 90 seconds
		assertEquals(90, myReport.getAvgStayTIme());
	}

	@Test

	void testTotalfee() {
		List<Ticket> tickets = new ArrayList<>();
		double totalFee = 0.0;
		for (int i = 0; i < 100; i++) {
			Ticket ticket = new Ticket("NoWayWeHaveThisLicensePlate" + i, 1);
			ticket.calculateFee(1);
			ticket.setTicketPaid();
			tickets.add(ticket);
			totalFee += ticket.getFee();
		}
		Report myReport = new Report(7, tickets);

		double totalFeeFromReport = myReport.getTotalFee();

		assertEquals(totalFeeFromReport, totalFee);

	}

	@Test
	// I'm testing if the toString actually works if it contains the right
	// information that we assigned in it
	void testToString() {
		List<Ticket> tickets = new ArrayList<>();

		for (int i = 0; i < 100; i++) {
			Ticket ticket = new Ticket("NoWayWeHaveThisLicensePlate" + i, 1);
			ticket.calculateFee(1);
			ticket.setTicketPaid();
			tickets.add(ticket);
		}

		// report contain paid tickets only. so we need to set the ticket to paid.
		Report myReport = new Report(7, tickets);

		String result = myReport.toString();
		for (int i = 0; i < 100; i++) {
			assertTrue(result.contains("7,"));
			assertTrue(result.contains("NoWayWeHaveThisLicensePlate" + i));
			assertTrue(result.contains("1"));
			assertFalse(result.isEmpty());
		}
	}

}