package testingArena;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import parkingGarage.Report;
import parkingGarage.Ticket;
import java.util.ArrayList;
import java.util.List;




class ReportTest {
	

	@Test 
	
	//this is testing if the constructor that takes a file actually works as intended when a missing file is passed in
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
	// I'm testing if the toString actually works if it contains the right information that we assigned in it
	void testToString() {
		
		 Ticket ticket = new Ticket();
		    
		 
		 ticket.setLicensePlate("NoWayWeHaveThisLicensePlate@123");
		 ticket.setFee(5.0);
		 ticket.setTicketPaid(true);

		 List<Ticket> tickets = new ArrayList<>();
		 tickets.add(ticket);

		 Report myReport = new Report(7, tickets);

		 String result = myReport.toString();

		 assertTrue(result.contains("7,"));
		 assertTrue(result.contains("NoWayWeHaveThisLicensePlate@123"));
		 assertTrue(result.contains("5.0"));
		 assertFalse(result.isEmpty());
		
		
	}
	
	
	
	
}