package parkingGarage;

import java.io.BufferedReader;
//java.io.*
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//java.net.*
import java.net.ServerSocket;
import java.net.Socket;
//java.util.*
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//Parking Garage Management System (PGMS)
public class PGMS {

	// Private Variables
	private static int garageCount = 0;

	// store the online Parking Garage Handler on dictionary, map it by integer.
	private static final ConcurrentMap<Integer, ClientHandler> clientsByGarageId = new ConcurrentHashMap<>();

	// fileLockhandler to prevent two or more functions trying to access the same
	// file at the same time.
	private final static Object fileLockHandler = new Object();

	private static PGMSOwnerGUI ownerGUI;

	// Program Main Section
	public static void main(String[] args) {

		ServerSocket server = null;

		try {
			checkTotalGarages(); // and the number of garages in txt to garageCount;
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Building a Server GUI for owner to operate;
		// Create the callback functions and pass into the thread(OwnerGUI), so the
		// thread can have GUI do certain tasks for the GUI
		PGMSOwnerGUISetRateCB setRateCallback = PGMS::getSetRateCallback;
		GUISearchTicketCB GUISearchTicketCallback = PGMS::getSearchTicketCallback;
		GUIgetReportCB ownerGetReportCallback = PGMS::getOwnerGetReportCallback;
		GUIgetReportByMonthYearCB ownerGetReportByMonthYearCallback = PGMS::getOwnerGetReportByMonthYearCallback;

		// initialize the GUI and run it on a thread
		ownerGUI = new PGMSOwnerGUI(garageCount, setRateCallback, GUISearchTicketCallback, ownerGetReportCallback,
				ownerGetReportByMonthYearCallback);
		new Thread(ownerGUI).start();
		// end of building Server GUI

		try {
			server = new ServerSocket(GlobalVariables.port); // Run server on Socket 7777
			server.setReuseAddress(true);

			while (true) { // Run server perpetually
				Socket client = server.accept();

				// ClientHandler object created using the connected socket
				ClientHandler clientSock = new ClientHandler(client);

				new Thread(clientSock).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (server != null) {
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// Multi-thread Client Handler
	private static class ClientHandler implements Runnable {

		// Private Variables
		private final Socket clientSocket;
		private int garageID; // garageID is the identifier for each garages
		boolean loggedIn = false;
		private MsgTypes msgType;

		private ObjectOutputStream out;
		private ObjectInputStream in;

		// Constructor
		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			try {
				// Create ObjectOutputStream from the OutPutStream
				out = new ObjectOutputStream(clientSocket.getOutputStream());
				out.flush();
				// Create ObjectInputStream from the InputStream
				in = new ObjectInputStream(clientSocket.getInputStream());

				// Create Message Object
				Message inMsg;
				Message outMsg;

				// When received a Message from client, check what type of Message it is
				// then jump to the appropriate task
				while ((inMsg = (Message) in.readObject()) != null) {

					// Attain Message Type
					msgType = inMsg.getMsgType();

					if (!loggedIn) { // If not logged in
						if (msgType == MsgTypes.NEWGARAGE) { // and MsgType == NEWGARAGE
							garageID = garageCount++; // Increase Garage Count and Assign that to Garage ID
							createNewGarage(garageID); // Run Function createNewGarage with Current Garage ID

						} else if (msgType == MsgTypes.GARAGELOGIN) { // If garage existed
							garageID = inMsg.getGarageID(); // get the garageID from incoming Messages
						}

						loggedIn = true; // Set this Garage to logged in
						clientsByGarageId.put(garageID, this); // add the client to the clients hashmap in PGMS

						// Create new Message object with MsgType
						outMsg = new Message(MsgTypes.SUCCESS, garageID);
						out.writeObject(outMsg); // Send response to Client

					} else {
						switch (msgType) { // If the garage is Logged In, Check MsgType and do specific task

						// adds Ticket to this garage unpaid txt file
						// Creates new ticket with RECEIVED MsgType w/ Garage ID and respond to client
						case NEWTICKET: {
							addNewTicketToFile(inMsg);
							// Response to Client
							outMsg = new Message(MsgTypes.RECEIVED, garageID);
							send(outMsg);
							break;
						}

						// MsgType LOOKUPUNPAIDTICKET will pull up a random unpaid ticket and sent it
						// back to client
						// it will pull up a random unpaid ticket and sent it back to client
						case LOOKUPUNPAIDTICKET: {
							Ticket ticket = lookUpUnpaidTicket(garageID, inMsg);
							if (ticket != null) {
								outMsg = new Message(MsgTypes.LOOKUPUNPAIDTICKET, garageID);
								outMsg.setTicket(ticket);
								send(outMsg);
							}
							break;
						}

						// incoming Message contain a paid ticket
						// store it in file and remove it from unpaid file
						case TICKETPAID: {
							ticketIsPaid(inMsg);
							break;
						}

						// authenticate operator and send success/fail back to client
						case OPERATORLOGIN: {
							if (isOperatorAuthenticated(inMsg)) { // success
								outMsg = new Message(MsgTypes.OPERATORSUCCESS, garageID);
								System.out.println("correct pw");
								send(outMsg);
							} else { // or fail
								outMsg = new Message(MsgTypes.OPERATORFAILURE, garageID);
								System.out.println("wrong pw");
								send(outMsg);
							}
							break;

						}

						// pull all history tickets for a garage and send report back to client
						case GETREPORT: {
							outMsg = new Message(MsgTypes.GETREPORT, garageID);
							List<Ticket> reportTickets = loadpaidTicket();
							Report report = null;
							if (reportTickets.size() != 0) {
								report = new Report(garageID, reportTickets);
							}
							Operator operator = new Operator();
							operator.setReport(report);
							outMsg.setOperator(operator);
							send(outMsg);
							break;
						}

						// pull all history tickets for a garage with filters of month/year
						// and send it back to client
						case GETREPORTBYMONTHYEAR: {
							outMsg = new Message(MsgTypes.GETREPORTBYMONTHYEAR, garageID);
							int month = inMsg.getOperator().getReport().getMonth();
							int year = inMsg.getOperator().getReport().getYear();
							List<Ticket> copy = searchTicketByMonthYear(month, year);
							Report report = null;
							if (copy.size() != 0) {
								report = new Report(garageID, copy);
							}
							Operator operator = new Operator();
							operator.setReport(report);
							outMsg.setOperator(operator);
							send(outMsg);
							break;
						}

						// search a ticket and send it back to client
						case SEARCHTICKET: {
							Ticket ticket = searchTicket(inMsg.getTicket());
							outMsg = new Message(MsgTypes.SEARCHTICKET, garageID);
							outMsg.setTicket(ticket);
							send(outMsg);
							break;
						}
						default:
							throw new IllegalArgumentException("Unexpected value: " + msgType);
						}

					}
				}

			} catch (EOFException eof) {

			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			} finally {
				try {
					clientSocket.close();
				} catch (IOException ignore) {
				}
			}
		}

		// Function to Create a new Garage with a garage ID
		private void createNewGarage(int garageID) throws IOException {

			String fileNamePaid = Integer.toString(garageID) + GlobalVariables.paidFilename;
			String fileNameUnpaid = Integer.toString(garageID) + GlobalVariables.unpaidFilename;

			// Creates both text files with garageID to store paid and unpaid tickets
			try (FileWriter writerPaid = new FileWriter(fileNamePaid, true);
					FileWriter writerUnpaid = new FileWriter(fileNameUnpaid, true)) {
				System.out.println("Created New Garage # " + garageID);
			}

			// update the total number of garages on server file
			synchronized (fileLockHandler) {
				// numberOfGarage.txt store the total number of registered parking garages
				// when PGMS runs, it will read the number stored on numberOfGarage.txt and
				// store it on garageCount
				try (FileWriter writer = new FileWriter(GlobalVariables.numberOfGarageFilename)) {
					writer.write(String.valueOf(garageCount));
				}
			}
		}

		// Adds new Ticket to file
		private void addNewTicketToFile(Message inMsg) throws IOException {
			synchronized (fileLockHandler) {
				String fileNameUnpaid = Integer.toString(garageID) + GlobalVariables.unpaidFilename; // Find appropriate
																										// file name
				try (FileWriter writer = new FileWriter(fileNameUnpaid, true)) { // Opens and append to file
					writer.write(inMsg.getTicket().toString()); // Writes Ticket information to file
				}
			}

		}

		// Lookup Unpaid Ticket
		private Ticket lookUpUnpaidTicket(int garageID, Message inMsg) throws IOException {
			List<Ticket> unPaidList = loadUnpaidTicket();
			Ticket ticket = null;
			if (unPaidList != null && !unPaidList.isEmpty()) { // when the list contain tickets
				Random random = new Random();
				int index = random.nextInt(unPaidList.size());
				ticket = unPaidList.get(index); // Grab a random ticket to return

				// Must set GUI id to the appropriate GUI id, so client know which GUI is
				// requesting unpaid ticket
				ticket.setGuiID(inMsg.getTicket().getGuiID());
			}
			return ticket;
		}

		private void ticketIsPaid(Message inMsg) throws IOException {

			Ticket ticket = inMsg.getTicket();
			String fileNamePaid = Integer.toString(garageID) + GlobalVariables.paidFilename;
			// Find appropriate file name

			// add the paid ticket to paid txt file
			synchronized (fileLockHandler) {
				try (FileWriter writer = new FileWriter(fileNamePaid, true)) {
					writer.write(ticket.toString());
				}
			}

			// remove the ticket from unpaid ticket txt
			if (ticket != null) {
				// read everyline of the file and check ticket
				// add the line of string that doesnt match the "ticket" to a string builder and
				// write it back to the file
				String fileNameUnpaid = Integer.toString(garageID) + GlobalVariables.unpaidFilename;
				synchronized (fileLockHandler) {
					StringBuilder fileInfo = new StringBuilder();

					try (BufferedReader reader = new BufferedReader(new FileReader(fileNameUnpaid))) {
						String line;
						while ((line = reader.readLine()) != null) {
							Ticket fileTicket = new Ticket(line);
							if (!fileTicket.getLicensePlate().equals(ticket.getLicensePlate())) {
								// i need to remove the "line(ticket)" from the txt file.
								fileInfo.append(line).append(System.lineSeparator());
							}
						}
					}
					try (FileWriter writer = new FileWriter(fileNameUnpaid)) { // Opens file
						writer.write(fileInfo.toString()); // Writes to file Ticket information
					}
				}
			}
		}

		// helper function that load the unpaid txt file into a list and return it
		private List<Ticket> loadUnpaidTicket() {
			String fileNameUnpaid = Integer.toString(garageID) + GlobalVariables.unpaidFilename;
			List<Ticket> unPaidList = new ArrayList<Ticket>();
			File file = new File(fileNameUnpaid);
			synchronized (fileLockHandler) {
				try (Scanner scanner = new Scanner(file)) {
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine().trim();
						Ticket ticket = new Ticket(line);
						unPaidList.add(ticket);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			return unPaidList;
		}

		// helper function that load the paid txt file into a list and return it
		private List<Ticket> loadpaidTicket() {
			String fileNamepaid = Integer.toString(garageID) + GlobalVariables.paidFilename;
			List<Ticket> PaidList = new ArrayList<Ticket>();
			File file = new File(fileNamepaid);
			synchronized (fileLockHandler) {
				try (Scanner scanner = new Scanner(file)) {
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine().trim();
						Ticket ticket = new Ticket(line);
						PaidList.add(ticket);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			return PaidList;
		}

		// authenticate Operator username/pw.
		private boolean isOperatorAuthenticated(Message inMsg) throws FileNotFoundException, IOException {
			String operatorUsername = inMsg.getOperator().getUsername();
			String operatorPw = inMsg.getOperator().getPassword();
			synchronized (fileLockHandler) {
				try (BufferedReader reader = new BufferedReader(new FileReader(GlobalVariables.operatorPwFilename))) {
					String line;
					while ((line = reader.readLine()) != null) {
						String[] parts = line.split(",");
						if (operatorUsername.equals(parts[0]) && operatorPw.equals(parts[1])) {
							return true;
						}

					}
				}
			}
			return false;
		}

		// When Operator search ticket, Server will load paid/unpaid tickets from file
		// and return the ticket if exist, else return the original ticket
		private Ticket searchTicket(Ticket targetTicket) {
			Ticket ticket = targetTicket;
			List<Ticket> tickets = loadpaidTicket();
			for (Ticket t : tickets) {
				if (t.getLicensePlate().equals(ticket.getLicensePlate())) {
					return t;
				}
			}
			tickets = loadpaidTicket();
			for (Ticket t : tickets) {
				if (t.getLicensePlate().equals(ticket.getLicensePlate())) {
					return t;
				}
			}
			return ticket;
		}

		// helper function that will send the given Msg to client
		public void send(Message msg) throws IOException {
			synchronized (out) { // ensure only one thread writes at a time
				try {
					out.writeObject(msg);
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private List<Ticket> searchTicketByMonthYear(int month, int year) {
			List<Ticket> source = loadpaidTicket();
			// read ticket from file and store on source;

			// the reason we read it from file to source first, is because we don't want to
			// keep the file open for too long for this thread
			// then filter the month/year
			List<Ticket> copy = new ArrayList<>();
			if (!source.isEmpty()) {
				for (Ticket ticket : source) {
					int ticketMonth = ticket.getEntryTime().getMonthValue();
					int ticketYear = ticket.getEntryTime().getYear();

					boolean matches = true;
					// filter month, if month is given and month doesnt match, do not add
					if (month != -1 && ticketMonth != month) {
						matches = false;
					}
					// filter year, if year is given and year doesnt match, do not add
					if (year != -1 && ticketYear != year) {
						matches = false;
					}

					if (matches) {
						copy.add(ticket);
					}
				}

			}
			return copy;
		}
	}

	private static void checkTotalGarages() throws IOException {
		// check if the server is running the first time. if its running the first time,
		// create the file and save 0 on it since theres 0 registered garages. otherwise
		// load the total of garage from file to garageCount;
		File file = new File(GlobalVariables.numberOfGarageFilename);
		if (file.exists()) {
			String garageNumber;
			try (Scanner scanner = new Scanner(file)) {
				garageNumber = scanner.nextLine().trim();
				garageCount = Integer.parseInt(garageNumber);
			}
		} else {
			try (FileWriter writer = new FileWriter(GlobalVariables.numberOfGarageFilename)) {
				writer.write(String.valueOf(garageCount));
			}
		}
	}

	// =============== PGMS OwnerGUI callback functions ===================

	// When owner entered garageID and rate on OwnerGUI, OwnerGUI will call this
	// function. it will create a Message type of SETRATE, so client know the server
	// is trying to set the rate for the garage. if successful send Message to
	// client, return true
	public static boolean getSetRateCallback(int garageID, double rate) {
		try {
			ClientHandler handler = clientsByGarageId.get(garageID);
			if (handler == null) {
				System.out.println("Garage " + garageID + " not connected.");
				return false;
			}
			Message msg = new Message(MsgTypes.SETRATE, garageID);
			Ticket ticket = new Ticket(garageID, rate);
			msg.setTicket(ticket);

			handler.send(msg);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// When owner entered licensePlate on OwnerGUI and press "Search" button,
	// OwnerGUI will call this function. it will search all the garage paid/unpaid
	// txt files, if license plate match, return the ticket
	public static Ticket getSearchTicketCallback(String licensePlate) {
		for (int i = 0; i < garageCount; i++) { // loop over all the garage txt files

			String fileNamePaid = Integer.toString(i) + GlobalVariables.paidFilename;
			String fileNameUnpaid = Integer.toString(i) + GlobalVariables.unpaidFilename;

			File file = new File(fileNamePaid);
			synchronized (fileLockHandler) {
				try (Scanner scanner = new Scanner(file)) {
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine().trim();
						Ticket ticket = new Ticket(line);
						if (ticket.getLicensePlate().equals(licensePlate)) {
							return ticket;
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			file = new File(fileNameUnpaid);
			synchronized (fileLockHandler) {
				try (Scanner scanner = new Scanner(file)) {
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine().trim();
						Ticket ticket = new Ticket(line);
						if (ticket.getLicensePlate().equals(licensePlate)) {
							return ticket;
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

		}
		return null;
	};

	// When owner entered garageID on OwnerGUI, and press "Get Full Report" button
	// OwnerGUI will call this function. it will add all the paid tickets in the
	// given garageID to a report and return the report
	public static Report getOwnerGetReportCallback(int garageID) {
		String fileNamePaid = Integer.toString(garageID) + GlobalVariables.paidFilename;

		File file = new File(fileNamePaid);
		Report report = null;
		if (file.exists()) {
			List<Ticket> paidList = new ArrayList<Ticket>();

			synchronized (fileLockHandler) {
				try (Scanner scanner = new Scanner(file)) {
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine().trim();
						Ticket ticket = new Ticket(line);
						paidList.add(ticket);
					}
					report = new Report(garageID, paidList);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		return report;
	}

	// When owner entered garageID/month/year on OwnerGUI, and press "Get Report"
	// button OwnerGUI will call this function. it will add all the paid tickets in
	// the given garageID that match the month/year to report and return report
	public static Report getOwnerGetReportByMonthYearCallback(int garageID, int month, int year) {
		String fileNamePaid = Integer.toString(garageID) + GlobalVariables.paidFilename;
		File file = new File(fileNamePaid);
		Report report = null;

		if (file.exists()) {
			List<Ticket> paidList = new ArrayList<>();
			synchronized (fileLockHandler) {
				try (Scanner scanner = new Scanner(file)) {
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine().trim();
						Ticket ticket = new Ticket(line);

						int ticketMonth = ticket.getEntryTime().getMonthValue();
						int ticketYear = ticket.getEntryTime().getYear();

						boolean matches = true;
						// filter month, if month is given and month doesnt match, do not add
						if (month != -1 && ticketMonth != month) {
							matches = false;
						}
						// filter year, if year is given and year doesnt match, do not add
						if (year != -1 && ticketYear != year) {
							matches = false;
						}

						if (matches) {
							paidList.add(ticket);
						}
					}

					if (!paidList.isEmpty()) {
						report = new Report(garageID, paidList);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		return report;
	}

}
