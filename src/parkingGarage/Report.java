package parkingGarage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Report implements Serializable {

	// Private variables
	private int garageID;
	private int avgStayTime;
	private double totalFee;
	private LocalDate creationDate;
	private List<Ticket> tickets;

	// Private variables used to search report with filters only,
	// the rest of the time is -1;
	private int month;
	private int year;

	// Default Constructor
	public Report() {
		this.month = -1;
		this.year = -1;
		this.garageID = 0;
		this.avgStayTime = 0;
		this.totalFee = 0;
		this.creationDate = LocalDate.now();
	}

	// Parameterized Constructor, create report with given garageID and Ticket list
	public Report(int garageID, List<Ticket> tickets) {
		this.month = -1;
		this.year = -1;
		this.garageID = garageID;
		this.tickets = tickets; // PAIDTICKETS
		this.creationDate = LocalDate.now();
		this.avgStayTime = 0;
		this.totalFee = 0;
		calculateAvgStayTime();
		calculateTotalFee();
	}

	// use this constructor to load report from txt file
	public Report(String fileName) {
		File file = new File(fileName);

		if (file.exists()) {
			try (Scanner scanner = new Scanner(file)) {
				// Parse header line, first line on the txt file will contain
				// garageID,avgStayTime,totalFee, creationDate because we used the toString
				// method to write to txt file, we are doing the reverse now
				String header = scanner.nextLine().trim();
				String[] parts = header.split(",");

				this.garageID = Integer.parseInt(parts[0]);
				this.avgStayTime = Integer.parseInt(parts[1]);
				this.totalFee = Double.parseDouble(parts[2]);
				this.creationDate = LocalDate.parse(parts[3]);

				// Parse ticket lines
				this.tickets = new ArrayList<>();
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine().trim();
					if (line.isEmpty())
						continue;
					Ticket t = new Ticket(line);
					tickets.add(t);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			// if no report file is found, set everything to -1. so we know this is a null
			// report
			this.garageID = -1;
			this.avgStayTime = -1;
			this.totalFee = -1;
			this.creationDate = null;
		}
		this.month = -1;
		this.year = -1;
	}

	// Function to calculate average stay time
	private void calculateAvgStayTime() {
		if (tickets == null || tickets.isEmpty()) {
			avgStayTime = 0;
			return;
		}
		Duration duration = Duration.ofSeconds(0);
		for (Ticket t : tickets) {
			// Add amount of every ticket duration of stay into 'duration'
			duration = duration.plus(t.getDurationOfStay());
		}

		// get the avg stay time
		avgStayTime = (int) duration.getSeconds() / tickets.size();

	}

	// Function to calculate total amount of fees by adding all fees
	private void calculateTotalFee() {
		if (tickets == null)
			return;
		for (Ticket t : tickets) {
			totalFee += t.getFee();
		}

		// round it to two decimal
		totalFee = Math.round(totalFee * 100.0) / 100.0;
	}

	public int getGarageId() {
		return garageID;
	}

	public int getAvgStayTIme() {
		return avgStayTime;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public double getTotalFee() {
		return totalFee;
	}

	public LocalDate creationDate() {
		return creationDate;
	}

	public List<Ticket> getTickets() {
		return tickets;
	}

	@Override
	public String toString() {
		// StringBuilder object to append all parts of this report into a string
		StringBuilder sb = new StringBuilder();
		sb.append(garageID).append(",").append(avgStayTime).append(",").append(totalFee).append(",")
				.append(creationDate).append("\n");

		// Append all tickets, one per line
		for (Ticket t : tickets) {
			sb.append(t.toString());
		}

		return sb.toString();

	}

	/// ================== UN-USED methods ==================
	// return a string contain the basic information for the report
	public String getReportInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append(garageID).append(",").append(avgStayTime).append(",").append(totalFee).append(",")
				.append(creationDate).append("\n");
		return sb.toString();
	}

	// turn all the tickets in the report to a string
	public String getTicketStrings() {
		StringBuilder sb = new StringBuilder();
		// Append all tickets, one per line
		for (Ticket t : tickets) {
			sb.append(t.toString());
		}

		return sb.toString();
	}

}
