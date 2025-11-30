package testingArena;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import parkingGarage.Report;




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
	
	
	
	
}