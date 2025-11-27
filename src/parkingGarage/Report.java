package parkingGarage;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

public class Report implements Serializable {
	private int garageID;
	private int avgStayTime;
	private double totalFee;
	private LocalDate creationDate;
	private ArrayList<Ticket> tickets;

	public Report() {
		this.garageID = 0;
		this.avgStayTime = 0;
		this.totalFee = 0;
		this.creationDate = LocalDate.now();
	}

	public Report(int garageID, ArrayList<Ticket> tickets) {
		this.garageID = garageID;
		this.tickets = tickets;
		this.creationDate = LocalDate.now();
		this.avgStayTime = 0;
		this.totalFee = 0;
		calculateAvgStayTime();
		calculateTotalFee();
	}

	private void calculateAvgStayTime() {
		Duration duration = Duration.ofSeconds(0);
		for (Ticket t : tickets) {
			duration = duration.plus(t.getDurationOfStay());
		}
		avgStayTime = (int) duration.getSeconds() / 60 / tickets.size();
	}

	private void calculateTotalFee() {
		for (Ticket t : tickets) {
			totalFee += t.getFee();
		}
	}

	@Override
	public String toString() {
		// Example line in txt file: 1,45,123.75,2025-11-01
		StringBuilder sb = new StringBuilder();
		sb.append(garageID).append(",").append(avgStayTime).append(",").append(totalFee).append(",")
				.append(creationDate).append("\n");

		// Add all tickets, one per line
		for (Ticket t : tickets) {
			sb.append(t.toString());
		}

		return sb.toString();
	}
}
