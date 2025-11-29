package parkingGarage;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

public class Ticket implements Serializable {

	// Private variables
	private int garageID;
	private String licensePlate;
	private double fee;
	private boolean paid;
	private LocalDateTime entryTime;
	private LocalDateTime exitTime;
	private Duration durationOfStay;
	private int GuiID;

	// this rate is used for setting rate on garage only.
	// the rest of the time is 0;
	private double rate;

	// Default Constructor
	public Ticket() {
	}

	public Ticket(int garageID, double rate) {
		this.licensePlate = "";
		this.entryTime = null;
		this.paid = false;
		this.garageID = garageID;
		this.fee = 0;
		this.exitTime = null;
		this.durationOfStay = null;
		this.rate = rate;
	}

	// Parameterized Constructor using licensePlate and garageID (Programmer
	// Defined)
	public Ticket(String licensePlate, int garageID) {
		this.licensePlate = licensePlate;
		this.entryTime = LocalDateTime.now();
		this.paid = false;
		this.garageID = garageID;
		this.fee = 0;
		this.exitTime = null;
		this.durationOfStay = null;
		this.rate = 0;
	}

	// Parameterized Constructor using string from txt file read
	// we used toString to save on txt file, this is the reverse
	public Ticket(String stringFromTxtFile) {
		String[] parts = stringFromTxtFile.split(",", -1);
		garageID = Integer.parseInt(parts[0].trim());
		licensePlate = parts[1].trim();
		fee = Double.parseDouble(parts[2].trim());
		paid = Boolean.parseBoolean(parts[3].trim());

		// scroll all the way down to see implementation
		this.entryTime = parseDate(parts[4]);
		this.exitTime = parseDate(parts[5]);
		this.durationOfStay = parseDuration(parts[6]);

	}

	// Function to set exit time
	private void setExitTime() {
		this.exitTime = LocalDateTime.now();
		durationOfStay = Duration.between(entryTime, exitTime);
	}

	// Function to calculate fee, rate passed in by the client
	public void calculateFee(double ratePerS) {
		this.rate = ratePerS;
		setExitTime();
		int s = (int) durationOfStay.getSeconds();
		fee = s * ratePerS;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public double getFee() {
		return fee;
	}

	public void setFee(double fee) {
		this.fee = fee;
	}

	public void setGuiID(int id) {
		GuiID = id;
	}

	public int getGuiID() {
		return GuiID;
	}

	public void setGarageID(int id) {
		garageID = id;
	}

	public int getGarageID() {
		return garageID;
	}

	public Duration getDurationOfStay() {
		return durationOfStay;
	}

	public void setDurationOfStay(Duration duration) {
		this.durationOfStay = duration;
	}

	public boolean isTicketPaid() {
		return paid;
	}

	public void setTicketPaid() {
		paid = true;
	}

	public void setTicketPaid(boolean paid) {
		this.paid = paid;
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;

	}

	public String getLicensePlate() {
		return licensePlate;
	}

	public void setEntryTime(LocalDateTime entryTime) {
		this.entryTime = entryTime;
	}

	public void setExitTime(LocalDateTime exitTime) {
		this.exitTime = exitTime;
	}

	public LocalDateTime getEntryTime() {
		return entryTime;
	}

	public LocalDateTime getExitTime() {
		return exitTime;
	}

	// Create a String from the information from the Ticket variables, using ',' as
	// a separator
	private String isNull(Object o) {
		return o == null ? "null" : o.toString();
	}

	@Override
	public String toString() {
		return garageID + "," + licensePlate + "," + fee + "," + paid + "," + isNull(entryTime) + "," + isNull(exitTime)
				+ "," + isNull(durationOfStay) + "\n";
	}

	// parse a string from txt file to LocalDateTime
	private LocalDateTime parseDate(String s) {
		if (s == null)
			return null;
		s = s.trim();
		if (s.isEmpty() || s.equalsIgnoreCase("null"))
			return null;
		return LocalDateTime.parse(s);
	}

	// parse a string from txt file to Duration
	private Duration parseDuration(String s) {
		if (s == null)
			return null;
		s = s.trim();
		if (s.isEmpty() || s.equalsIgnoreCase("null"))
			return null;
		return Duration.parse(s);
	}

}
