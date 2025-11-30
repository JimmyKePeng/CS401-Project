package testingArena;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import parkingGarage.Ticket;
import java.time.Duration;
import java.time.LocalDateTime;

public class TicketTest {
    
    private Ticket ticket;
  
    // Test default constructor creates empty ticket
     
    @Test
    public void testDefaultConstructor() {
        ticket = new Ticket();
        assertNotNull(ticket);
    }
    
     // Test constructor with garageID and rate
    
    @Test
    public void testConstructorWithGarageIDAndRate() {
        ticket = new Ticket(1, 0.05);
        assertEquals(1, ticket.getGarageID());
        assertEquals(0.05, ticket.getRate());
        assertEquals("", ticket.getLicensePlate());
        assertFalse(ticket.isTicketPaid());
        assertEquals(0, ticket.getFee());
    }
    
     // Test constructor with license plate and garageID
     
    @Test
    public void testConstructorWithLicensePlateAndGarageID() {
        ticket = new Ticket("ABC123", 5);
        assertEquals("ABC123", ticket.getLicensePlate());
        assertEquals(5, ticket.getGarageID());
        assertFalse(ticket.isTicketPaid());
        assertNotNull(ticket.getEntryTime());
        assertEquals(0, ticket.getFee());
    }
    
     // Test constructor from string format
    
    @Test
    public void testConstructorFromString() {
        String ticketString = "1,XYZ789,10.5,true,2025-11-29T10:00:00,2025-11-29T12:00:00,PT2H\n";
        ticket = new Ticket(ticketString);
        
        assertEquals(1, ticket.getGarageID());
        assertEquals("XYZ789", ticket.getLicensePlate());
        assertEquals(10.5, ticket.getFee(), 0.01);
        assertTrue(ticket.isTicketPaid());
        assertNotNull(ticket.getEntryTime());
        assertNotNull(ticket.getExitTime());
        assertNotNull(ticket.getDurationOfStay());
    }
    
     // Test setGarageID and getGarageID
     
    @Test
    public void testSetAndGetGarageID() {
        ticket = new Ticket();
        ticket.setGarageID(10);
        assertEquals(10, ticket.getGarageID());
    }
    
     // Test setLicensePlate and getLicensePlate
   
    @Test
    public void testSetAndGetLicensePlate() {
        ticket = new Ticket();
        ticket.setLicensePlate("TEST123");
        assertEquals("TEST123", ticket.getLicensePlate());
    }
    
     // Test setFee and getFee

    @Test
    public void testSetAndGetFee() {
        ticket = new Ticket();
        ticket.setFee(25.50);
        assertEquals(25.50, ticket.getFee(), 0.01);
    }
    
     // Test setRate and getRate
     
    @Test
    public void testSetAndGetRate() {
        ticket = new Ticket();
        ticket.setRate(0.10);
        assertEquals(0.10, ticket.getRate(), 0.01);
    }
    
     // Test setGuiID and getGuiID
    
    @Test
    public void testSetAndGetGuiID() {
        ticket = new Ticket();
        ticket.setGuiID(99);
        assertEquals(99, ticket.getGuiID());
    }
    
     // Test setTicketPaid 
     
    @Test
    public void testSetTicketPaidNoParameter() {
        ticket = new Ticket();
        assertFalse(ticket.isTicketPaid());
        ticket.setTicketPaid();
        assertTrue(ticket.isTicketPaid());
    }
    
     // Test setTicketPaid with boolean parameter
     
    @Test
    public void testSetTicketPaidWithParameter() {
        ticket = new Ticket();
        ticket.setTicketPaid(true);
        assertTrue(ticket.isTicketPaid());
        ticket.setTicketPaid(false);
        assertFalse(ticket.isTicketPaid());
    }

     // Test isTicketPaid returns correct status
     
    @Test
    public void testIsTicketPaid() {
        ticket = new Ticket("ABC123", 1);
        assertFalse(ticket.isTicketPaid());
    }

     // Test setEntryTime and getEntryTime
     
    @Test
    public void testSetAndGetEntryTime() {
        ticket = new Ticket();
        LocalDateTime now = LocalDateTime.now();
        ticket.setEntryTime(now);
        assertEquals(now, ticket.getEntryTime());
    }

     // Test setExitTime and getExitTime
     
    @Test
    public void testSetAndGetExitTime() {
        ticket = new Ticket();
        LocalDateTime exitTime = LocalDateTime.now();
        ticket.setExitTime(exitTime);
        assertEquals(exitTime, ticket.getExitTime());
    }
    
     // Test setDurationOfStay and getDurationOfStay
     
    @Test
    public void testSetAndGetDurationOfStay() {
        ticket = new Ticket();
        Duration duration = Duration.ofHours(2);
        ticket.setDurationOfStay(duration);
        assertEquals(duration, ticket.getDurationOfStay());
    }
    
     // Test calculateFee method
     
    @Test
    public void testCalculateFee() {
        ticket = new Ticket("ABC123", 1);
        LocalDateTime entry = LocalDateTime.now().minusHours(2);
        ticket.setEntryTime(entry);
        
        ticket.calculateFee(0.01); 
        
        assertTrue(ticket.getFee() > 0);
        assertNotNull(ticket.getExitTime());
        assertNotNull(ticket.getDurationOfStay());
    }
    
     // Test calculateFee with different rates
    
    @Test
    public void testCalculateFeeWithDifferentRates() {
        ticket = new Ticket("TEST456", 2);
        LocalDateTime entry = LocalDateTime.now().minusSeconds(100);
        ticket.setEntryTime(entry);
        
        ticket.calculateFee(0.05);
        
        // Fee should be approximately 100 * 0.05 = 5.0
        assertTrue(ticket.getFee() >= 4.0 && ticket.getFee() <= 6.0);
    }
    
    // Test toString method format
    
    @Test
    public void testToStringFormat() {
        ticket = new Ticket("XYZ789", 3);
        ticket.setFee(15.75);
        ticket.setTicketPaid(true);
        
        String result = ticket.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("XYZ789"));
        assertTrue(result.contains("15.75"));
        assertTrue(result.contains("true"));
        assertTrue(result.contains("3"));
    }
    
     // Test toString with null values
    
    @Test
    public void testToStringWithNullValues() {
        ticket = new Ticket();
        String result = ticket.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("null"));
    }
    
     // Test ticket with unpaid status
    
    @Test
    public void testUnpaidTicket() {
        ticket = new Ticket("UNPAID123", 1);
        assertFalse(ticket.isTicketPaid());
        assertEquals(0, ticket.getFee());
    }
    
     // Test ticket with paid status
     
    @Test
    public void testPaidTicket() {
        ticket = new Ticket("PAID456", 1);
        ticket.setFee(20.00);
        ticket.setTicketPaid(true);
        
        assertTrue(ticket.isTicketPaid());
        assertEquals(20.00, ticket.getFee());
    }
    
     // Test entry time is set on creation
    
    @Test
    public void testEntryTimeSetOnCreation() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        ticket = new Ticket("TEST789", 1);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        LocalDateTime entryTime = ticket.getEntryTime();
        assertNotNull(entryTime);
        assertTrue(entryTime.isAfter(before) || entryTime.isEqual(before));
        assertTrue(entryTime.isBefore(after) || entryTime.isEqual(after));
    }
    
     // Test duration of stay calculation
    
    @Test
    public void testDurationOfStayCalculation() {
        ticket = new Ticket("DURATION123", 1);
        LocalDateTime entry = LocalDateTime.now().minusHours(3);
        ticket.setEntryTime(entry);
        
        ticket.calculateFee(0.01);
        
        Duration duration = ticket.getDurationOfStay();
        assertNotNull(duration);
        // Duration should be approximately 3 hours 
        long seconds = duration.getSeconds();
        assertTrue(seconds >= 10700 && seconds <= 10900);
    }
    
     // Test multiple fee calculations
    
    @Test
    public void testMultipleFeeCalculations() {
        ticket = new Ticket("MULTI123", 1);
        LocalDateTime entry = LocalDateTime.now().minusSeconds(100);
        ticket.setEntryTime(entry);
        
        ticket.calculateFee(0.05);
        double firstFee = ticket.getFee();
        
        ticket.calculateFee(0.10);
        double secondFee = ticket.getFee();
        
        // Second fee should be higher with higher rate
        assertTrue(secondFee > firstFee);
    }
    
     // Test zero fee for zero rate
    
    @Test
    public void testZeroFeeForZeroRate() {
        ticket = new Ticket("ZERO123", 1);
        LocalDateTime entry = LocalDateTime.now().minusHours(1);
        ticket.setEntryTime(entry);
        
        ticket.calculateFee(0.0);
        
        assertEquals(0.0, ticket.getFee());
    }
   
     // Test license plate with special characters
     
    @Test
    public void testLicensePlateWithSpecialCharacters() {
        ticket = new Ticket
        		();
        ticket.setLicensePlate("ABC-123");
        assertEquals("ABC-123", ticket.getLicensePlate());
    }
   
     // Test negative garage ID
    
    @Test
    public void testNegativeGarageID() {
        ticket = new Ticket();
        ticket.setGarageID(-1);
        assertEquals(-1, ticket.getGarageID());
    }
    
     // Test large fee value
    
    @Test
    public void testLargeFeeValue() {
        ticket = new Ticket();
        ticket.setFee(99999.99);
        assertEquals(99999.99, ticket.getFee(), 0.01);
    }
}
