package testingArena;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import parkingGarage.Report;
import parkingGarage.Ticket;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;





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
	// my test here is to make sure that the method actually calculates the correct avg
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
	Ticket ticket1 = new Ticket();	
	Ticket ticket2 = new Ticket();
	
	ticket1.setFee(3.5);
	ticket2.setFee(4.2);
	
	List<Ticket> tickets = new ArrayList<>();
	
	tickets.add(ticket1);
	tickets.add(ticket2);
	
	Report myReport = new Report(7,tickets);
	
      double total = myReport.getTotalFee();
      
      assertEquals(7.7,total,.0001);
	
	
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