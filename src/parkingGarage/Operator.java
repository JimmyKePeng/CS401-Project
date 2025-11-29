package parkingGarage;

import java.io.Serializable;

public class Operator implements Serializable {
	private String username;
	private String password;
	private Report report;
	private int garageID;

	public Operator() {
		this.username = null;
		this.password = null;
		this.report = null;
		this.garageID = 0;
	}

	public Operator(String username, String password, int garageID) {
		this.username = username;
		this.password = password;
		this.report = null;
		this.garageID = garageID;
	}

	public Operator(String username, String password, Report report, int garageID) {
		this.username = username;
		this.password = password;
		this.report = report;
		this.garageID = garageID;

	}

	public void setReport(Report report) {
		this.report = report;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Report getReport() {
		return report;
	}

	public int getGarageID() {
		return garageID;
	}

}
