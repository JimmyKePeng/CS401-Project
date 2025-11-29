package testingArena;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import parkingGarage.PGMS;

class PGMSTest {

	//PGMS Has a Client Handler Perhaps we would want to run to be able to test the saving/loading
	
	@Test
    void testGetOwnerReportByMonthYear_NoGarage_ReturnsNull() {
        Report report = PGMS.getOwnerGetReportByMonthYearCallback(50, 5, 2024);
        assertNull(report);
    }
	
	
	
	

}
