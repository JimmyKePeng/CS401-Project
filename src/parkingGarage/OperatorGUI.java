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
import java.io.FileWriter;
import java.io.IOException;
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

public class OperatorGUI implements Runnable {

	// private boolean isConnected = false;
	private boolean isLoggedIn = false;

	// Operator data
	// private Operator operator;
	private int garageID;
	private Report reportForSaving;
	OperatorGUILoginCB operatorLoginCallback;
	GUIgetReportCB operatorGetReportCallback;
	GUISearchTicketCB operatorGUISearchTicketCallback;
	OperatorGUISetRateCB operatorGUISetRateCallback;
	GUIgetReportByMonthYearCB getReportByMonthYearCallback;
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
	private JButton loadReportButton;
	private JTextField searchReportFilenameField;
	private JTextField filenameField;
	private JTextField monthField;
	private JTextField yearField;
	private JButton searchButton;
	private JTextField searchField;
	private JTextArea displayArea;
	private JButton setRateButton;
	private JTextField inputRateField;

	public OperatorGUI(int garageID, OperatorGUILoginCB operatorLoginCallback, GUIgetReportCB operatorGetReportCallback,
			GUISearchTicketCB operatorGUISearchTicketCallback, OperatorGUISetRateCB operatorGUISetRateCallback,
			GUIgetReportByMonthYearCB getReportByMonthYearCallback) {

		this.garageID = garageID;
		this.reportForSaving = null;
		this.operatorLoginCallback = operatorLoginCallback;
		this.operatorGetReportCallback = operatorGetReportCallback;
		this.operatorGUISearchTicketCallback = operatorGUISearchTicketCallback;
		this.operatorGUISetRateCallback = operatorGUISetRateCallback;
		this.getReportByMonthYearCallback = getReportByMonthYearCallback;

	}

	@Override
	public void run() {
		SwingUtilities.invokeLater(this::createWindow);

	}

	public static boolean operatorSetRateCallback(double rate) {
	    PGMS.HOURLY_RATE = rate;
	    return true;
	}

	private void ensureOperatorPasswordFileExists() {
	    File file = new File("username_pw.txt");
	    if (!file.exists()) {
	        try (FileWriter writer = new FileWriter(file)) {
	            writer.write("user,password");
	            System.out.println("Created default username_pw.txt with credentials: user,password");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}	
	
	// Creates and displays the main GUI window
	private void createWindow() {
		ensureOperatorPasswordFileExists();
		mainFrame = new JFrame("Parking Garage " + garageID + " Operator GUI");
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
	// Button: GetReport
	// License Plate:
	// Button: Search

	private void createDashboardPanel() {
		dashboardPanel = new JPanel(new BorderLayout(10, 10));
		dashboardPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		// Top panel - Controls
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// GetReport button
		JPanel reportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		reportPanel.add(new JLabel("Month: "));
		monthField = new JTextField(5);
		reportPanel.add(monthField);
		reportPanel.add(new JLabel("Year: "));
		yearField = new JTextField(5);
		reportPanel.add(yearField);
		getReportButton = new JButton("GetReport");
		getReportButton.setFont(new Font("Arial", Font.BOLD, 14));
		getReportButton.addActionListener(e -> getReport());
		reportPanel.add(getReportButton);
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

		// license plate Search panel
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		searchPanel.add(new JLabel("License Plate:"));
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
		inputRateField = new JTextField(15);
		inputRateField.setFont(new Font("Arial", Font.PLAIN, 14));
		setRatePanel.add(inputRateField);

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

	private void login() {
		// get operator log in username/pw from GUI
		String username = usernameField.getText().trim();
		String pw = new String(passwordField.getPassword());
		statusLabel.setText("");
		// call the callback function from ParkingGarage
		operatorLoginCallback.run(username, pw);

	}

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

	private void setRate() {
		SwingUtilities.invokeLater(() -> {
			String inputRate = inputRateField.getText().trim();
			double rate = Double.parseDouble(inputRate);
			if (rate > 0 && rate < 10) {
				operatorGUISetRateCallback.run(rate);
				inputRateField.setText("");
				JOptionPane.showMessageDialog(null, "New parking rate have been set to $" + rate + " per second",
						"Success", JOptionPane.INFORMATION_MESSAGE);
			} else {
				inputRateField.setText("");
				JOptionPane.showMessageDialog(null, "rate must be between 0 and $10 per second", "Invalid input",
						JOptionPane.INFORMATION_MESSAGE);
			}

		});
	}

	private void getReport() {
		String monthString = monthField.getText().trim();
		String yearString = yearField.getText().trim();
		int month = -1;
		int year = -1;

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
		if (month == -1 && year == -1) {
			operatorGetReportCallback.run(garageID);
		} else {
			getReportByMonthYearCallback.run(garageID, month, year);
		}

	}

	public void displayReport(Report report) {
		SwingUtilities.invokeLater(() -> {
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
				operatorGUISearchTicketCallback.run(licensePlate);
			}
			searchField.setText("");
		});
	}

	public void displayTicket(Ticket ticket) {

		SwingUtilities.invokeLater(() -> {
			if (ticket.getEntryTime() == null) {
				// that means this ticket not found;
				displayArea.setText(ticket.getLicensePlate() + " is not found");
				displayArea.setCaretPosition(0);
			} else {
				// use ReportFormatter to format the ticket
				String text = ShareFunctions.formatTicket(ticket);
				displayArea.setText(text);
				displayArea.setCaretPosition(0);
			}
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

}