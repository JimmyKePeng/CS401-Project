package parkingGarage;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShareFunctions {

	// Formats an entire report to string
	public static String formatReport(Report report) {
		if (report == null) {
			return "No report available.\n";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Report for garage #").append(report.getGarageId()).append("\n");
		sb.append("----------------------------------------------------\n");
		sb.append("Average stay time: ").append(report.getAvgStayTIme()).append(" mins\n");
		sb.append("Total cars: ").append(report.getTickets().size()).append("\n");
		sb.append("Total fee: $").append(String.format("%.2f", report.getTotalFee())).append("\n");

		List<Ticket> tickets = report.getTickets();
		for (Ticket t : tickets) {
			sb.append(formatTicket(t));
		}

		return sb.toString();
	}

	// Formats a single ticket to string
	public static String formatTicket(Ticket ticket) {
		if (ticket == null) {
			return "\n[null ticket]\n";
		}

		String entryTime = (ticket.getEntryTime() != null)
				? ticket.getEntryTime().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"))
				: "null";

		String exitTime = (ticket.getExitTime() != null)
				? ticket.getExitTime().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"))
				: "null";

		String isPaid = ticket.isTicketPaid() ? "true" : "false";

		String duration = (ticket.getDurationOfStay() == null) ? "null"
				: String.valueOf(ticket.getDurationOfStay().getSeconds() / 60);

		StringBuilder sb = new StringBuilder();
		sb.append("\nLicense Plate: ").append(ticket.getLicensePlate());
		sb.append("  Garage #: ").append(ticket.getGarageID());
		sb.append("  Paid?: ").append(isPaid);
		sb.append("  Entry Time: ").append(entryTime);
		sb.append("  Exit Time: ").append(exitTime);
		sb.append("  Duration: ").append(duration).append(" mins");
		sb.append("  Fee: $").append(String.format("%.2f", ticket.getFee()));

		return sb.toString();
	}

	public static boolean saveReport(Report report, String filename) {
		if (!filename.isEmpty()) {
			filename = filename + ".txt";
			try (FileWriter writer = new FileWriter(filename, true)) {
				writer.write(report.toString()); // Writes to file Ticket information
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}

	public static Report loadReport(String filename) {
		Report report = null;

		if (!filename.isEmpty()) {
			filename = filename + ".txt";
			report = new Report(filename);
		}
		return report;
	}

}
