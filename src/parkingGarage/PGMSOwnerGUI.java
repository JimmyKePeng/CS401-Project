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

	// After GUI displayed a report, there's an option to save this report
	private Report reportForSaving;

	// Callback functions, when buttons are clicked on GUI, it will call one
	// of these functions to trigger Server to do certain task
	PGMSOwnerGUISetRateCB setRateCallback;
	GUISearchTicketCB GUISearchTicketCallback;
	GUIgetReportCB ownerGetReportCallback;
	GUIgetReportByMonthYearCB ownerGetReportByMonthYearCallback;

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
	private JButton getReportByMonthYearButton;
	private JTextField searchReportFilenameField;
	private JTextField garageNumSearchField;
	private JTextField searchField;
	private JTextField filenameField;
	private JTextField monthField;
	private JTextField yearField;

	private JTextArea displayArea;
	private JButton setRateButton;
	private JTextField inputRateField;
	private JTextField inputRateForGarage;

	// GUI be given these callback functions to be able to function
	public PGMSOwnerGUI(int totalGarages, PGMSOwnerGUISetRateCB setRateCallback,
			GUISearchTicketCB GUISearchTicketCallback, GUIgetReportCB ownerGetReportCallback,
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

	// Creates and displays the main GUI window
	private void createWindow() {
		mainFrame = new JFrame("Owner of PGMS GUI");
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.setSize(1200, 600);
		mainFrame.setLocationRelativeTo(null);

		// Create both panels, so we can switch panel after successful login
		createLoginPanel(); // scroll to the bottom to see implementation
		createDashboardPanel();

		mainFrame.setContentPane(loginPanel);
		mainFrame.setVisible(true);
	}

	// This function will be called when login button is clicked on login screen
	private void login() {
		// get Owner log in username/pw from GUI
		String username = usernameField.getText().trim();
		String pw = new String(passwordField.getPassword());
		statusLabel.setText("");
		File file = new File(ownerPwFileName);
		try (Scanner scanner = new Scanner(file)) {
			// open the txt file and check if username/pw exist
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				String[] parts = line.split(",");
				if (username.equals(parts[0]) && pw.equals(parts[1])) {
					loggedInSuccess();
				}
			}
			loggedInFail();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// This function will be called when logout button is clicked on dashboard
	// exit the dashboard and back to log in screen
	private void logout() {
		SwingUtilities.invokeLater(() -> {
			displayArea.setText("");
			searchField.setText("");
			mainFrame.setContentPane(loginPanel);
			mainFrame.revalidate();
			mainFrame.repaint();
		});
	}

	// pw/username is authenticated
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

	// This function will be called when setRate button is clicked on dashboard
	// User must enter Garage#, so the program knows which garage to set the new
	// rate
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
			// check if grageID and rate is valid
			try {
				rate = Double.parseDouble(inputRate);
				garageID = Integer.parseInt(garageNumber);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(null, "Input must be number or decimals", "Invalid input",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			// if its a reasonable rate and garageID is within the total of Garages
			if (rate > 0 && rate < 10 && garageID < totalGarages) {
				inputRateField.setText("");
				inputRateForGarage.setText("");

				// call the callback function on PGMS, and PGMS will send Message to client to
				// change their rate
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

	// This function will be called when getReport button is clicked on dashboard
	// Must enter GarageID to get report for a specific garage
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

		// call callback function, so PGMS can get the report
		Report report = ownerGetReportCallback.run(garageID);

		// display the report from PGMS
		displayReport(report);
	}

	public void displayReport(Report report) {
		SwingUtilities.invokeLater(() -> {
			if (report != null) {
				this.reportForSaving = report;
				// use ReportFormatter to format the report to a string to display on GUI
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
				// call the callback function to have PGMS search a ticket in ALL garages
				Ticket ticket = GUISearchTicketCallback.run(licensePlate);
				if (ticket == null) {
					displayArea.setText(licensePlate + " is not found");
					displayArea.setCaretPosition(0);
				} else {
					// use ReportFormatter to format the ticket to a string to display on GUI
					String text = ShareFunctions.formatTicket(ticket);
					displayArea.setText(text);
					displayArea.setCaretPosition(0);
				}
			}
			searchField.setText("");
		});
	}

	// This function will be called when saveReport button is clicked on dashboard
	// this button will be enable if only if getReport button is clicked
	private void saveReport() {
		String filename = filenameField.getText().trim();

		// save the report in a txt file with the input filename
		// ShareFunctions.saveReport is used by OperatorGUI and OwnerGUI to save the
		// given report and filename
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

	// This function will be called when loadReport button is clicked on dashboard
	private void loadReport() {
		String filename = searchReportFilenameField.getText().trim();

		// user must input a filename to load the report from txt file
		Report report = ShareFunctions.loadReport(filename);
		searchReportFilenameField.setText("");
		if (report != null && report.getGarageId() != -1) {
			displayReport(report);
		} else {
			JOptionPane.showMessageDialog(null, "Report does not exist", "Failed", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	// This function will be called when getReport button is clicked on dashboard
	private void getReport() {
		String garageNumber = garageNumSearchField.getText().trim();
		String monthString = monthField.getText().trim();
		String yearString = yearField.getText().trim();
		int garageID;
		int month = -1;
		int year = -1;

		// garage number must be valid integer, so we know which garage report to get
		try {
			garageID = Integer.parseInt(garageNumber);
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(null, "Garage# must be integer", "Invalid input",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		// get input month and validate month
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

		// get input year and validate year
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
		// call callback function, and it will return a report with the
		// filters(garageID,month,year)
		Report report = ownerGetReportByMonthYearCallback.run(garageID, month, year);
		displayReport(report);
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
		setRatePanel.add(new JLabel("Enter New Rate Per Seconds: "));
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
	}
}