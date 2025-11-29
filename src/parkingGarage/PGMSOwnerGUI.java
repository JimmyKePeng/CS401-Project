package parkingGarage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


public class PGMSOwnerGUI implements Runnable {

	// private boolean isConnected = false;
	private boolean isLoggedIn = false;
	private final Object fileLockHandler = new Object();
	private String ownerPwFileName = "owner_pw.txt";
	private int totalGarages;
	private Report reportForSaving;
	PGMSOwnerGUISetRateCB setRateCallback;
	GUISearchTicketCB GUISearchTicketCallback;
	GUIgetReportCB ownerGetReportCallback;
	private GUIgetReportByMonthYearCB ownerGetReportByMonthYearCallback;
	
	// GUI components
	private JFrame mainFrame;
	private JPanel loginPanel;
	private JPanel dashboardPanel;

	// Login components
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JButton loginButton;
	private JLabel statusLabel;

	// Dashboard components
	private JButton logoutButton;
	private JButton getReportButton;
	private JButton saveReportButton;
	private JButton searchButton;
	private JButton loadReportButton;
	private JTextField searchReportFilenameField;
	private JTextField garageNumSearchField;
	private JTextField searchField;
	private JTextField filenameField;
	private JTextField monthField;

	private JTextArea displayArea;
	private JButton setRateButton;
	private JTextField inputRateField;
	private JTextField inputRateForGarage;


	private JTextField yearField;
	private JButton getReportByMonthYearButton;

	public PGMSOwnerGUI(int totalGarages,
		    PGMSOwnerGUISetRateCB setRateCallback,
		    GUISearchTicketCB GUISearchTicketCallback,
		    GUIgetReportCB ownerGetReportCallback,
		    GUIgetReportByMonthYearCB ownerGetReportByMonthYearCallback) {
				this.totalGarages = totalGarages;
				this.setRateCallback = setRateCallback;
				this.GUISearchTicketCallback = GUISearchTicketCallback;
				this.ownerGetReportCallback = ownerGetReportCallback;
				this.ownerGetReportByMonthYearCallback = ownerGetReportByMonthYearCallback;
	}

	@Override
	public void run() {
		SwingUtilities.invokeLater(this::createWindow);

	}
	
	private void ensureOwnerPasswordFileExists() {
		File file = new File(ownerPwFileName);
		if (!file.exists()) {
			try (FileWriter writer = new FileWriter(file)) {
				writer.write("user, password");  // default username and password
				System.out.println("Created default owner_pw.txt with credentials: user, password");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	

	// Creates and displays the main GUI window
	private void createWindow() {
		mainFrame = new JFrame("Owner of PGMS GUI");
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.setSize(1200, 600);
		mainFrame.setLocationRelativeTo(null);

		// Create both panels
		createLoginPanel();
		createDashboardPanel();

		mainFrame.setContentPane(loginPanel);
		mainFrame.setVisible(true);

	}

	private void createLoginPanel() {
		loginPanel = new JPanel(new GridBagLayout());
		loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Username label
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		loginPanel.add(new JLabel("Username:"), gbc);

		// Username field
		usernameField = new JTextField(20);
		usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		loginPanel.add(usernameField, gbc);

		// Password label
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		loginPanel.add(new JLabel("Password:"), gbc);

		// Password field
		passwordField = new JPasswordField(20);
		passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		loginPanel.add(passwordField, gbc);

		// Login button
		loginButton = new JButton("login");
		loginButton.setFont(new Font("Arial", Font.BOLD, 14));
		loginButton.setPreferredSize(new Dimension(100, 30));
		loginButton.addActionListener(e -> login());
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		loginPanel.add(loginButton, gbc);

		// Status label
		statusLabel = new JLabel("");
		statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		gbc.gridy = 3;
		loginPanel.add(statusLabel, gbc);

	}

	// Creates the operator dashboard panel
	private void createDashboardPanel() {
		dashboardPanel = new JPanel(new BorderLayout(10, 10));
		dashboardPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		// Top panel - Controls
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// GetReport button
		JPanel reportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		reportPanel.add(new JLabel("Enter Garage #: "));
		garageNumSearchField = new JTextField(15);
		garageNumSearchField.setFont(new Font("Arial", Font.PLAIN, 14));
		reportPanel.add(garageNumSearchField);

		getReportButton = new JButton("Get Full Report");
		getReportButton.setFont(new Font("Arial", Font.BOLD, 14));
		getReportButton.addActionListener(e -> getFullReport());
		reportPanel.add(getReportButton);

		reportPanel.add(new JLabel("Or Get Report by Month: "));
		monthField = new JTextField(5);
		reportPanel.add(monthField);
		reportPanel.add(new JLabel("Year: "));
		yearField = new JTextField(5);
		reportPanel.add(yearField);

		getReportByMonthYearButton = new JButton("Get Report");
		getReportByMonthYearButton.setFont(new Font("Arial", Font.BOLD, 14));
		getReportByMonthYearButton.addActionListener(e -> getReport());
		reportPanel.add(getReportByMonthYearButton);
		controlPanel.add(reportPanel);

		// Save Report
		JPanel saveReportButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		saveReportButtonPanel.add(new JLabel("Enter filename: "));
		filenameField = new JTextField(15);
		filenameField.setFont(new Font("Arial", Font.PLAIN, 14));
		saveReportButtonPanel.add(filenameField);

		saveReportButton = new JButton("Save Current Report");
		saveReportButton.setFont(new Font("Arial", Font.BOLD, 14));
		saveReportButton.setEnabled(false);
		saveReportButton.addActionListener(e -> saveReport());
		saveReportButtonPanel.add(saveReportButton);
		controlPanel.add(saveReportButtonPanel);

		// load Report
		JPanel loadReportButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		loadReportButtonPanel.add(new JLabel("Enter filename: "));
		searchReportFilenameField = new JTextField(15);
		searchReportFilenameField.setFont(new Font("Arial", Font.PLAIN, 14));
		loadReportButtonPanel.add(searchReportFilenameField);

		loadReportButton = new JButton("Load Report");
		loadReportButton.setFont(new Font("Arial", Font.BOLD, 14));
		loadReportButton.addActionListener(e -> loadReport());
		loadReportButtonPanel.add(loadReportButton);
		controlPanel.add(loadReportButtonPanel);

		// Lisense plate Search panel
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		searchPanel.add(new JLabel("License  Plate:"));
		searchField = new JTextField(15);
		searchField.setFont(new Font("Arial", Font.PLAIN, 14));
		searchPanel.add(searchField);

		searchButton = new JButton("Search");
		searchButton.setFont(new Font("Arial", Font.BOLD, 14));
		searchButton.addActionListener(e -> searchTicket());
		searchPanel.add(searchButton);
		controlPanel.add(searchPanel);

		// Set rate panel
		JPanel setRatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		setRatePanel.add(new JLabel("Enter New Rate Per Hour: "));
		inputRateField = new JTextField(8);
		inputRateField.setFont(new Font("Arial", Font.PLAIN, 14));
		setRatePanel.add(inputRateField);
		setRatePanel.add(new JLabel("For Garage #: "));
		inputRateForGarage = new JTextField(8);
		inputRateForGarage.setFont(new Font("Arial", Font.PLAIN, 14));
		setRatePanel.add(inputRateForGarage);

		setRateButton = new JButton(" Set  ");
		setRateButton.setFont(new Font("Arial", Font.BOLD, 14));
		setRateButton.addActionListener(e -> setRate());
		setRatePanel.add(setRateButton);
		controlPanel.add(setRatePanel);

		// Logout panel
		JPanel logoutButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		logoutButton = new JButton("logout");
		logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
		logoutButton.addActionListener(e -> logout());
		logoutButtonPanel.add(logoutButton);
		controlPanel.add(logoutButtonPanel);
		controlPanel.add(Box.createVerticalStrut(15));

		dashboardPanel.add(controlPanel, BorderLayout.NORTH);

		// Center - Display area for results
		displayArea = new JTextArea();
		displayArea.setEditable(false);
		displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		displayArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		JScrollPane scrollPane = new JScrollPane(displayArea);
		dashboardPanel.add(scrollPane, BorderLayout.CENTER);
		
		// Get Report By Month/Year panel
				JPanel getReportByMonthYearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				getReportByMonthYearPanel.add(new JLabel("Garage #: "));
				JTextField garageNumMonthYearField = new JTextField(5);
				garageNumMonthYearField.setFont(new Font("Arial", Font.PLAIN, 14));
				getReportByMonthYearPanel.add(garageNumMonthYearField);
				getReportByMonthYearPanel.add(new JLabel("Month (1-12): "));
				monthField = new JTextField(5);
				monthField.setFont(new Font("Arial", Font.PLAIN, 14));
				getReportByMonthYearPanel.add(monthField);
				getReportByMonthYearPanel.add(new JLabel("Year (e.g., 2025): "));
				yearField = new JTextField(6);
				yearField.setFont(new Font("Arial", Font.PLAIN, 14));
				getReportByMonthYearPanel.add(yearField);
				getReportByMonthYearButton = new JButton("Get Report (Month/Year)");
				getReportByMonthYearButton.setFont(new Font("Arial", Font.BOLD, 14));
				getReportByMonthYearButton.addActionListener(e -> {
				    String garageText = garageNumMonthYearField.getText().trim();
				    String monthText = monthField.getText().trim();
				    String yearText = yearField.getText().trim();
				    if (garageText.isEmpty() || monthText.isEmpty() || yearText.isEmpty()) {
				        JOptionPane.showMessageDialog(null, 
				            "Please enter garage #, month, and year", 
				            "Missing Input", 
				            JOptionPane.WARNING_MESSAGE);
				        return;
				    }
				    try {
				        int garageID = Integer.parseInt(garageText);
				        int month = Integer.parseInt(monthText);
				        int year = Integer.parseInt(yearText);
				        Report report = ownerGetReportByMonthYearCallback.run(garageID, month, year);
				        displayReport(report);
				    } catch (NumberFormatException ex) {
				        JOptionPane.showMessageDialog(null, 
				            "Month, Year, and Garage must be numbers.", 
				            "Invalid Input", 
				            JOptionPane.WARNING_MESSAGE);
				    }
				});
				getReportByMonthYearPanel.add(getReportByMonthYearButton);
				controlPanel.add(getReportByMonthYearPanel);
			}

	private void login() {
		// make file for user/password before reading 
		ensureOwnerPasswordFileExists();
		// Declare 'file'before using it
		File file = new File(ownerPwFileName);
		// show where the file is being searched for (troubleshooting) 
		System.out.println("Working directory: " + new File(".").getAbsolutePath());
		System.out.println("Looking for owner_pw.txt at: " + file.getAbsolutePath());
		// get Owner log in username/pw from GUI
		String username = usernameField.getText().trim();
		String pw = new String(passwordField.getPassword());
		statusLabel.setText("");
		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				String[] parts = line.split(",");
				String fileUser = parts[0].trim();	// make sure no whitespace
				String filePw = parts[1].trim();
				if (username.equals(fileUser) && pw.equals(filePw)) {
					loggedInSuccess();
				}
			}
			loggedInFail();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//		loggedInSuccess();
	}

	// logout owner
	private void logout() {
		SwingUtilities.invokeLater(() -> {
			displayArea.setText("");
			searchField.setText("");
			mainFrame.setContentPane(loginPanel);
			mainFrame.revalidate();
			mainFrame.repaint();
		});
	}

	public void loggedInSuccess() {
		SwingUtilities.invokeLater(() -> {
			isLoggedIn = true;
			passwordField.setText("");
			usernameField.setText("");
			mainFrame.setContentPane(dashboardPanel);
			mainFrame.revalidate();
			mainFrame.repaint();
		});

	}

	public void loggedInFail() {
		SwingUtilities.invokeLater(() -> {
			statusLabel.setText("Invalid username or password");
			passwordField.setText("");
			usernameField.setText("");
		});
	}

	// set rate for specific garage
	private void setRate() {
		SwingUtilities.invokeLater(() -> {
			String inputRate = inputRateField.getText().trim();
			String garageNumber = inputRateForGarage.getText().trim();
			if (inputRate.isEmpty() || garageNumber.isEmpty()) {
				JOptionPane.showMessageDialog(null, "Please enter both a rate and a garage number.", "Missing input",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			double rate;
			int garageID;
			try {
				rate = Double.parseDouble(inputRate);
				garageID = Integer.parseInt(garageNumber);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(null, "Input must be number or decimals", "Invalid input",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			if (rate > 0 && rate < 10 && garageID < totalGarages) {
				inputRateField.setText("");
				inputRateForGarage.setText("");
				boolean isSetRateSuccess = setRateCallback.run(garageID, rate);
				if (isSetRateSuccess) {
					JOptionPane.showMessageDialog(null,
							"New parking rate have been set to $" + rate + " per second for Garage #" + garageID,
							"Success", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null,
							"Failed to send rate update. Garage may be offline or disconnected.", "Send error",
							JOptionPane.INFORMATION_MESSAGE);
				}

			} else {
				inputRateField.setText("");
				inputRateForGarage.setText("");
				JOptionPane.showMessageDialog(null, "Invalid rate or Invalid Garage number", "Invalid input",
						JOptionPane.INFORMATION_MESSAGE);
			}

		});
	}

	// get report for specific garage
	private void getFullReport() {
		String garageNumber = garageNumSearchField.getText().trim();
		int garageID;
		try {
			garageID = Integer.parseInt(garageNumber);
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(null, "Garage must be integer", "Invalid input",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		// callback function
		Report report = ownerGetReportCallback.run(garageID);
		displayReport(report);
	}

	public void displayReport(Report report) {
		SwingUtilities.invokeLater(() -> {
//			garageNumSearchField.setText("");
			if (report != null) {
				this.reportForSaving = report;
				// use ReportFormatter to format the report
				String text = ShareFunctions.formatReport(report);
				displayArea.setText(text);
				saveReportButton.setEnabled(true);
			} else {
				displayArea.setText("No report found");
			}
			displayArea.setCaretPosition(0);
		});

	}

	private void searchTicket() {
		SwingUtilities.invokeLater(() -> {
			String licensePlate = searchField.getText().trim().toUpperCase();
			if (!licensePlate.equals("")) {
				// call the callback function
				Ticket ticket = GUISearchTicketCallback.run(licensePlate);
				if (ticket == null) {

					displayArea.setText(licensePlate + " is not found");
					displayArea.setCaretPosition(0);
				} else {
					// use ReportFormatter to format the ticket
					String text = ShareFunctions.formatTicket(ticket);
					displayArea.setText(text);
					displayArea.setCaretPosition(0);
				}
			}
			searchField.setText("");
		});
	}

	private void saveReport() {
		String filename = filenameField.getText().trim();
		boolean isSaved = ShareFunctions.saveReport(this.reportForSaving, filename);
		if (isSaved) {
			filenameField.setText("");
			saveReportButton.setEnabled(false);
			JOptionPane.showMessageDialog(null, "Report saved to " + filename, "Success",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "Filename can't be empty", "Failed", JOptionPane.INFORMATION_MESSAGE);
		}

	}

	private void loadReport() {
		String filename = searchReportFilenameField.getText().trim();
		Report report = ShareFunctions.loadReport(filename);
		searchReportFilenameField.setText("");
		if (report != null && report.getGarageId() != -1) {
			displayReport(report);
		} else {
			JOptionPane.showMessageDialog(null, "Report does not exist", "Failed", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void getReport() {
		String garageNumber = garageNumSearchField.getText().trim();
		String monthString = monthField.getText().trim();
		String yearString = yearField.getText().trim();
		int garageID;
		int month = -1;
		int year = -1;
		try {
			garageID = Integer.parseInt(garageNumber);
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(null, "Garage# must be integer", "Invalid input",
					JOptionPane.INFORMATION_MESSAGE);
			return; // garage number must be valid integer
		}
		try {
			month = Integer.parseInt(monthString);
		} catch (NumberFormatException e) {
		}
		if (month != -1) {
			if (month < 1 || month > 12) {
				JOptionPane.showMessageDialog(null, "Month must be between 1 - 12", "Invalid input",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}
		try {
			year = Integer.parseInt(yearString);
		} catch (NumberFormatException e) {
		}
		if (year != -1) {
			if (year < 1900 || year > 2200) {
				JOptionPane.showMessageDialog(null, "Invalid year", "Invalid input", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

		}
		// callback function
		Report report = ownerGetReportByMonthYearCallback.run(garageID, month, year);
		displayReport(report);
	}

}