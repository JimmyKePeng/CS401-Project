package parkingGarage;

import java.time.LocalDateTime;
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
	private static final List<List<Ticket>> PAIDTICKETS = new ArrayList<>();
	private static final List<List<Ticket>> UNPAIDTICKETS = new ArrayList<>();
	private static int garageCount = 0;
	static String numberOfGarageFileName = "numberOfGarage.txt";

	private static final ConcurrentMap<Integer, ClientHandler> clientsByGarageId = new ConcurrentHashMap<>();
	private final static Object fileLockHandler = new Object();
	private static PGMSOwnerGUI ownerGUI;
	public static double HOURLY_RATE = 1.00;     // default hourly rate
	public static double DAILY_MAX_HOURS = 5;    // max chargeable hours per day
	// Program Main Section
	public static void main(String[] args) {

	    ensureOperatorPasswordFileExists();
		// Lazy Instantiation of ServerSocket
		ServerSocket server = null;

		try {
			checkTotalGarages();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Building a Server GUI for owner to operate;
		PGMSOwnerGUISetRateCB setRateCallback = PGMS::getSetRateCallback;
		GUISearchTicketCB GUISearchTicketCallback = PGMS::getSearchTicketCallback;
		GUIgetReportCB ownerGetReportCallback = PGMS::getOwnerGetReportCallback;
		GUIgetReportByMonthYearCB ownerGetReportByMonthYearCallback = PGMS::getOwnerGetReportByMonthYearCallback;
		ownerGUI = new PGMSOwnerGUI(garageCount, setRateCallback, GUISearchTicketCallback, ownerGetReportCallback,
				ownerGetReportByMonthYearCallback);

		ownerGUI.run();
		// end of building Server GUI

		try {
			server = new ServerSocket(7777); // Run server on Socket 7777
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

	private static void ensureOperatorPasswordFileExists() {
	    File file = new File("username_pw.txt");
	    if (!file.exists()) {
	        try (FileWriter writer = new FileWriter(file)) {
	            writer.write("user,password");
	            System.out.println("Created default operator credentials: user,password");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}		
	
	private static class ClientHandler implements Runnable {
		
		// Private Variables
		private final Socket clientSocket;
		private int garageID;
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
				// While the receiving Message is not null
				while ((inMsg = (Message) in.readObject()) != null) {

					// Attain Message Type
					msgType = inMsg.getMsgType();

					if (!loggedIn) {
						if (msgType == MsgTypes.NEWGARAGE) { // If not logged in and MsgType == NEWGARAGE
							garageID = garageCount++; // Increase Garage Count and Assign that to Garage ID
							createNewGarage(garageID); // Run Function createNewGarage with Current Garage ID

						} else if (msgType == MsgTypes.GARAGELOGIN) { // If garage existed
							garageID = inMsg.getGarageID();
							loadGarage(garageID); // get the garageID and load tickets for garage
						}
						loggedIn = true; // Set this Garage to logged in

						clientsByGarageId.put(garageID, this); // add the client to the clients hashmap

						// Create new Message object with MsgType
						outMsg = new Message(MsgTypes.SUCCESS, garageID);
						out.writeObject(outMsg); // Send response to Client

					} else {
						switch (msgType) { // If the garage is Logged In, Check MsgType

						// MsgType NEWTICKET adds Ticket to File & Array UNPAIDTICKETS
						// Creates new ticket with RECEIVED MsgType w/ Garage ID to send to Client as
						// response
						case NEWTICKET: {
							addNewTicketToFile(inMsg);
							// Response to Client
							outMsg = new Message(MsgTypes.RECEIVED, garageID);
							out.writeObject(outMsg);
							out.flush();
							break;
						}

						case LOOKUPUNPAIDTICKET: {

						    // Extract GUI ID
						    Ticket requestTicket = inMsg.getTicket();
						    int guiID = requestTicket.getGuiID();

						    // Load unpaid ticket
						    Ticket t = lookUpUnpaidTicket(garageID, inMsg);

						    if (t == null) {
						        System.out.println("LOOKUP: No unpaid tickets for garage " + garageID);
						        Message emptyMsg = new Message(MsgTypes.LOOKUPUNPAIDTICKET, garageID);
						        emptyMsg.setTicket(null);
						        out.writeObject(emptyMsg);
						        out.flush();
						        break;
						    }

						    // Assign updated hourly rate and calculate fee
						    t.setRate(PGMS.HOURLY_RATE);
						    t.setExitTime(LocalDateTime.now());
						    t.calculateFee();

						    // Send ticket back to client GUI
						    Message outMsg1 = new Message(MsgTypes.LOOKUPUNPAIDTICKET, garageID);
						    t.setGuiID(guiID);
						    outMsg1.setTicket(t);
						    out.writeObject(outMsg1);
						    out.flush();
						    break;
						}


						case TICKETPAID: {
							ticketIsPaid(inMsg);
							break;
						}
						case OPERATORLOGIN: {
							if (isOperatorAuthenticated(inMsg)) {
								outMsg = new Message(MsgTypes.OPERATORSUCCESS, garageID);
								System.out.println("correct pw");
								out.writeObject(outMsg);
								out.flush();
							} else {
								outMsg = new Message(MsgTypes.OPERATORFAILURE, garageID);
								System.out.println("wrong pw");
								out.writeObject(outMsg);
								out.flush();
							}
							break;

						}
						
						case SETRATE: {
						    // Rate message coming from this garage's Operator GUI
						    Ticket t = inMsg.getTicket();
						    double newRate = t.getRate();
						    PGMS.HOURLY_RATE = newRate;  // update global hourly rate
						    System.out.println("PGMS: updated HOURLY_RATE to " + newRate +
						                       " from garage " + garageID);
						    break;
						}						
						
						case GETREPORT: {
							outMsg = new Message(MsgTypes.GETREPORT, garageID);
							List<Ticket> reportTickets = getReportTickets();
							Report report = null;
							if (reportTickets.size() != 0) {
								report = new Report(garageID, reportTickets);
							}
							Operator operator = new Operator();
							operator.setReport(report);
							outMsg.setOperator(operator);
							out.writeObject(outMsg);
							out.flush();
							break;
						}
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
							out.writeObject(outMsg);
							out.flush();
							break;
						}
						case SEARCHTICKET: {
							Ticket ticket = searchTicket(inMsg.getTicket());
							outMsg = new Message(MsgTypes.SEARCHTICKET, garageID);
							outMsg.setTicket(ticket);
							out.writeObject(outMsg);
							out.flush();
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
			while (UNPAIDTICKETS.size() <= garageID)
			    UNPAIDTICKETS.add(new ArrayList<>());
			while (PAIDTICKETS.size() <= garageID)
			    PAIDTICKETS.add(new ArrayList<>());
			String fileNamePaid = "garage#" + Integer.toString(garageID) + "_paid.txt";
			String fileNameUnpaid = "garage#" + Integer.toString(garageID) + "_unpaid.txt";

			// Creates both text files
			try (FileWriter writerPaid = new FileWriter(fileNamePaid, true);
					FileWriter writerUnpaid = new FileWriter(fileNameUnpaid, true)) {
				System.out.println("Created New Garage # " + garageID);
			}

			// save total number of garages on server
			synchronized (fileLockHandler) {
				try (FileWriter writer = new FileWriter(numberOfGarageFileName)) {
					writer.write(String.valueOf(garageCount));
				}
			}
		}

		// Adds new Ticket to file
		private void addNewTicketToFile(Message inMsg) throws IOException {
			synchronized (fileLockHandler) {
				String fileNameUnpaid = "garage#" + garageID + "_unpaid.txt"; // Find appropriate file name
				try (FileWriter writer = new FileWriter(fileNameUnpaid, true)) { // Opens file
					writer.write(inMsg.getTicket().toString()); // Writes to file Ticket information
				}
			}

		}

		private void handleSetRate(Message inMsg) throws IOException {
		    double newRate = inMsg.getTicket().getRate();
		    // Update server-side hourly rate
		    PGMS.HOURLY_RATE = newRate;
		    System.out.println("Server: Updated HOURLY_RATE to = " + newRate);
		    // Send SETRATE update back to this client
		    Message outMsg = new Message(MsgTypes.SETRATE, garageID);
		    outMsg.setTicket(inMsg.getTicket());
		    send(outMsg);
		}		
		
		private Ticket lookUpUnpaidTicket(int garageID, Message inMsg) throws IOException {

		    List<Ticket> tickets = UNPAIDTICKETS.get(garageID);

		    // return null safely if empty
		    if (tickets == null || tickets.isEmpty()) {
		        System.out.println("LOOKUP: No unpaid tickets found for garage " + garageID);
		        return null;
		    }

		    // Choose a random unpaid ticket
		    Random random = new Random();
		    int index = random.nextInt(tickets.size());
		    Ticket ticket = tickets.get(index);

		    // Copy the ticket safely
		    Ticket copy = new Ticket();
		    copy.setGarageID(garageID);
		    copy.setGuiID(inMsg.getTicket().getGuiID());
		    copy.setLicensePlate(ticket.getLicensePlate());
		    copy.setEntryTime(ticket.getEntryTime());

		    // assign updated rate
		    copy.setRate(PGMS.HOURLY_RATE);

		    return copy;
		}    
		
		private void ticketIsPaid(Message inMsg) throws IOException {

			Ticket ticket = inMsg.getTicket();
			String fileNamePaid = "garage#" + garageID + "_paid.txt"; // Find appropriate file name

			// add the paid ticket to file
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
				String fileNameUnpaid = "garage#" + garageID + "_unpaid.txt";
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

		private void loadGarage(int garageID) throws FileNotFoundException {
		    // load existing ticket from file to UNPAIDTICKETS and PAIDTICKETS for garage

		    // Format text file name
		    String fileNamePaid = "garage#" + Integer.toString(garageID) + "_paid.txt";
		    String fileNameUnpaid = "garage#" + Integer.toString(garageID) + "_unpaid.txt";

		    List<Ticket> paidList = new ArrayList<Ticket>();
		    List<Ticket> unPaidList = new ArrayList<Ticket>();

		    // read ticket from file and add to list;
		    File file = new File(fileNamePaid);
		    synchronized (fileLockHandler) {
		        try (Scanner scanner = new Scanner(file)) {
		            while (scanner.hasNextLine()) {
		                String line = scanner.nextLine().trim();
		                Ticket ticket = new Ticket(line);
		                paidList.add(ticket);
		            }
		        }
		    }

		    file = new File(fileNameUnpaid);
		    synchronized (fileLockHandler) {
		        try (Scanner scanner = new Scanner(file)) {
		            while (scanner.hasNextLine()) {
		                String line = scanner.nextLine().trim();
		                Ticket ticket = new Ticket(line);
		                unPaidList.add(ticket);
		            }
		        }
		    }

		    // set empty lists if needed
		    while (UNPAIDTICKETS.size() <= garageID)
		        UNPAIDTICKETS.add(null);
		    while (PAIDTICKETS.size() <= garageID)
		        PAIDTICKETS.add(null);

		    // add the list of tickets to list
		    UNPAIDTICKETS.set(garageID, unPaidList);
		    PAIDTICKETS.set(garageID, paidList);
		}
		
		
		private List<Ticket> loadUnpaidTicket() {
			String fileNameUnpaid = "garage#" + Integer.toString(garageID) + "_unpaid.txt";
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

		private List<Ticket> loadpaidTicket() {
			String fileNamepaid = "garage#" + Integer.toString(garageID) + "_paid.txt";
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

		// check if Operator username and pw exist when client's operator want to log in
		private boolean isOperatorAuthenticated(Message inMsg) throws FileNotFoundException, IOException {
			String operatorUsername = inMsg.getOperator().getUsername();
			String operatorPw = inMsg.getOperator().getPassword();
			try (BufferedReader reader = new BufferedReader(new FileReader("username_pw.txt"))) {
				String line;
				while ((line = reader.readLine()) != null) {
					String[] parts = line.split(",");
					if (operatorUsername.equals(parts[0]) && operatorPw.equals(parts[1])) {
						return true;
					}

				}
			}
			return false;
		}

		// Operator search ticket, PGMS pull and send ticket back to client
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

		private List<Ticket> getReportTickets() {
			List<Ticket> paidList = loadpaidTicket();
			return paidList;

		}

		private List<Ticket> searchTicketByMonthYear(int month, int year) {
			List<Ticket> source = loadpaidTicket();
			// read ticket from file and add it source;

			// the reason we read it from file to source first, is because we don't want to
			// keep the file open for too long for this thread
			// filter the month/year
			List<Ticket> copy = new ArrayList<>();
			if (!source.isEmpty()) {
				for (Ticket ticket : source) {
					int ticketMonth = ticket.getEntryTime().getMonthValue();
					int ticketYear = ticket.getEntryTime().getYear();

					boolean matches = true;
					// filter month
					if (month != -1 && ticketMonth != month) {
						matches = false;
					}
					// filter year
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
		// check if the server is running the first time.
		// if its running the first time, create the file and save it.
		// otherwise load the total of garage from file to garageCount;
		File file = new File(numberOfGarageFileName);
		if (file.exists()) {
			String garageNumber;
			try (Scanner scanner = new Scanner(file)) {
				garageNumber = scanner.nextLine().trim();
				garageCount = Integer.parseInt(garageNumber);
			}
		} else {
			try (FileWriter writer = new FileWriter(numberOfGarageFileName, true)) {
				writer.write(String.valueOf(garageCount));
			}
		}
	}

	// ============ PGMS OwnerGUI callback functions ===================
	public static boolean getSetRateCallback(int garageID, double rate) {
		PGMS.HOURLY_RATE = rate; // applies hourly rate to all garages
		try {
			ClientHandler handler = clientsByGarageId.get(garageID);
			if (handler == null) {
				System.out.println("Garage " + garageID + " not connected.");
				return false;
			}

			Message msg = new Message(MsgTypes.SETRATE, garageID);

			// Use a dedicated object instead of misusing Ticket for rate data
			Ticket ticket = new Ticket(garageID, rate);
			msg.setTicket(ticket);

			handler.send(msg);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static Ticket getSearchTicketCallback(String licensePlate) {
		for (int i = 0; i < garageCount; i++) {

			String fileNamePaid = "garage#" + Integer.toString(i) + "_paid.txt";
			String fileNameUnpaid = "garage#" + Integer.toString(i) + "_unpaid.txt";
			// set entry time to null to show this ticket is not a regular ticket
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

	public static Report getOwnerGetReportCallback(int garageID) {
		String fileNamePaid = "garage#" + Integer.toString(garageID) + "_paid.txt";

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

	public static Report getOwnerGetReportByMonthYearCallback(int garageID, int month, int year) {
		String fileNamePaid = "garage#" + garageID + "_paid.txt";

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

						// If month filter provided, enforce it
						if (month != -1 && ticketMonth != month) {
							matches = false;
						}

						// If year filter provided, enforce it
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
