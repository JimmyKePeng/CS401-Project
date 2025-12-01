package testingArena;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import parkingGarage.Report;
import parkingGarage.ShareFunctions;
import parkingGarage.Ticket;

class ShareFunctionsTest {

	// fully initialize ticket
	private Ticket makeTicket(String plate, int garageId) {
		Ticket t = new Ticket();
		t.setLicensePlate(plate);
		t.setGarageID(garageId);

		// set entry and exit time
		LocalDateTime entry = LocalDateTime.now().minusMinutes(30);
		LocalDateTime exit = LocalDateTime.now();
		t.setEntryTime(entry);
		t.setExitTime(exit);

		// manually compute duration 
		t.setDurationOfStay(Duration.between(entry, exit));

		// Fee
		t.setFee(4.50);

		return t;
	}

	// formatTime() test all fields/format (hr/min/sec)
	@Test
	void formatTimeTest() {
		assertEquals("1h 1m 1s", ShareFunctions.formatTime(3661));
		assertEquals("59s", ShareFunctions.formatTime(59));
		assertEquals("2m 5s", ShareFunctions.formatTime(125));
	}

	// formatDuration() test
	@Test
	void formatDurationTest() {
		Duration d = Duration.ofSeconds(3661);
		assertEquals("1h 1m 1s", ShareFunctions.formatDuration(d));
	}

	// formatTicket() test all fields
	@Test
	void formatTicketTest() {
		Ticket t = makeTicket("ABC123", 5);
		String out = ShareFunctions.formatTicket(t);

		assertTrue(out.contains("ABC123"));
		assertTrue(out.contains("Garage #: 5"));
		assertTrue(out.contains("Duration:"));
	}

	@Test
	void formatTicketNullTest() {
		assertEquals("\n[null ticket]\n", ShareFunctions.formatTicket(null));
	}

	// formatReport() test
	@Test
	void formatReportTest() {
		List<Ticket> list = new ArrayList<>();
		list.add(makeTicket("XYZ999", 3));

		Report report = new Report(3, list);

		String out = ShareFunctions.formatReport(report);

		assertTrue(out.contains("Report for garage #3"));
		assertTrue(out.contains("Total cars: 1"));
	}

	@Test
	void formatReportNullTest() {
		assertEquals("No report available.\n", ShareFunctions.formatReport(null));
	}

	// saveReport() test
	@Test
	void saveReportTest() {
		List<Ticket> list = new ArrayList<>();
		list.add(makeTicket("AAA111", 2));

		Report report = new Report(2, list);

		// Temporary test file
		String filename = "testReportFile";
		File f = new File(filename + ".txt");
		if (f.exists())
			f.delete();

		boolean saved = ShareFunctions.saveReport(report, filename);
		assertTrue(saved);
		assertTrue(f.exists());

		// Cleanup
		f.delete();
	}

	@Test
	void saveReportEmptyNameTest() {
		List<Ticket> list = new ArrayList<>();
		list.add(makeTicket("BBB222", 1));

		Report report = new Report(1, list);

		boolean saved = ShareFunctions.saveReport(report, "");
		assertFalse(saved);
	}

	// loadReport() test
	@Test
	void loadReportValidNameTest() {
		List<Ticket> list = new ArrayList<>();
		list.add(makeTicket("CCC333", 7));

		Report report = new Report(7, list);

		String filename = "loadTestReport";
		File f = new File(filename + ".txt");
		if (f.exists())
			f.delete();

		// Save report
		assertTrue(ShareFunctions.saveReport(report, filename));

		// Load report
		Report loaded = ShareFunctions.loadReport(filename);
		assertNotNull(loaded);
		assertEquals(7, loaded.getGarageId());
		assertTrue(loaded.getTickets().size() >= 1);

		// Cleanup
		f.delete();
	}

	@Test
	void loadReportEmptyNameTest() {
		assertNull(ShareFunctions.loadReport(""));
	}

}
