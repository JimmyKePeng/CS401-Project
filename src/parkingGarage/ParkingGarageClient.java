package parkingGarage;

import java.io.File;
import java.io.FileWriter;
//java.io.*
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
//java.net.*
import java.net.Socket;
//java.util.*
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ParkingGarageClient {

	// Private Variables
	private static DriverGUI driverGUI1;
	private static DriverGUI driverGUI2;
	private static OperatorGUI operatorGUI;
	private static volatile double ratePerSecond = 0.001;
	private static String garageIDFileName = "garageId.txt";
	private static String garageRateFileName = "garageRate.txt";

	// Program Main Section
	public static void main(String[] args) {

		// Public block variables
		int assignedID = -1; // Garage ID assigned by the server
		boolean loggedIn = false; // Indication of successful connection with Server
		// double initialRate = 0.25; // Rate per second to calculate fee

		// A thread safe queue (Linked Blocking Queue) that will store license plates
		BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

		// A constant (final) Map to map each DriverGUI to a key, linking it to a
		// Concurrent Hash Map (Multiple Thread Accessible)
		final Map<Integer, DriverGUI> guiById = new ConcurrentHashMap<>();

		try {
			// Create a socket to connect to server
			Socket socket = new Socket("localhost", 7777);// IP address should replace localhost

			// Create ObjectOutputStream from the OutPutStream
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			out.flush();

			// Create ObjectInputStream from the InputStream
			InputStream inputStream = socket.getInputStream();
			ObjectInputStream in = new ObjectInputStream(inputStream);

			File file = new File(garageIDFileName);

			if (file.exists()) {
				String garageNumber;
				String parkingRate;

				// read garageID from file
				try (Scanner scanner = new Scanner(file)) {
					garageNumber = scanner.nextLine().trim();
				}
				assignedID = Integer.parseInt(garageNumber);

				// read parking rate from file
				File rateFile = new File(garageRateFileName);
				try (Scanner scanner = new Scanner(rateFile)) {
					parkingRate = scanner.nextLine().trim();
				}
				ratePerSecond = Double.parseDouble(parkingRate);

				Message outMsg = new Message(MsgTypes.GARAGELOGIN, assignedID);
				out.writeObject(outMsg);
				out.flush();
				Message inMsg = (Message) in.readObject();
				if (!loggedIn && inMsg.getMsgType() == MsgTypes.SUCCESS) {
					loggedIn = true;
				}
			} else {
				// Create a new Message object, indicate MsgType as NEWGARAGE for new garage
				// connecting to the server
				Message outMsg = new Message(MsgTypes.NEWGARAGE, -1);
				out.writeObject(outMsg); // Sends Message to Server
				out.flush();

				// Read Server Response through 'inMsg'
				Message inMsg = (Message) in.readObject();

				// If not logged in and the message type is equal to SUCCESS, log in and use
				// assigned ID from server
				if (!loggedIn && inMsg.getMsgType() == MsgTypes.SUCCESS) {
					loggedIn = true;
					assignedID = inMsg.getGarageID();
					try (FileWriter writer = new FileWriter(garageIDFileName)) { // Opens file
						writer.write(String.valueOf(assignedID));// Writes to assignedID to file
					}
					try (FileWriter writer = new FileWriter(garageRateFileName)) { // Opens file
						writer.write(String.valueOf(ratePerSecond));// Writes to assignedID to file
					}
				}

			}

			// Constant (final) ID is assigned, constant 'running' is assigned a bool if
			// SUCCESS or otherwise
			final int garageID = assignedID;
			final boolean running = loggedIn;
			
			// create gate object here
			Gate gate = new Gate(garageID, Location.Entry);
			// double ratePerSecond = initialRate;
			// ========LISTENING FOR LICENSE PLATE READER FOR LICENSE PLATE===========
			Thread sender = new Thread(() -> { // Thread 'sender' is made, runs while logged in
				try {
					while (running) {
						String plate = queue.take(); // Queue will wait (block) until there is a license plate in the
														// queue
						double rate = gate.getRate();
						Ticket t = new Ticket(plate, garageID, rate); // A new ticket object is made with the plate taken
						Message Msg = new Message(MsgTypes.NEWTICKET, garageID); // A message object is made with the
																					// type NEWTICKET and garage ID
						Msg.setTicket(t); // Ticket object is set to the message
						out.writeObject(Msg); // Message object is sent to the server
						out.flush(); // flush stream
					}
				} catch (InterruptedException | IOException ie) { // Exceptions to catch
					System.out.println("Sender is disconnected.");
				}
			});
			sender.start(); // Start Thread
			// ========LISTENING FOR LICENSE PLATE READER FOR LICENSE PLATE===========

			// =====================LISTENING FOR INCOMING MESSAGE================
			Thread receiver = new Thread(() -> { // Thread 'receiver' is made while logged in
				try {
					while (running) {
						Message msg = (Message) in.readObject(); // Read message from the input stream, wait until
																	// Message received

						// Switch statement to handle message types
						switch (msg.getMsgType()) {
						case RECEIVED:
							// System.out.println("Server RECEIVED new Ticket!");
							break;
						case LOOKUPUNPAIDTICKET: { // Message Type LOOKUPTICKET
							Ticket t = msg.getTicket(); // Pull ticket variable from received ticket into variable
							int id = t.getGuiID(); // Pull GUI ID from Ticket

							// System.out.println("Looking up tickets in GUI# " + id);

							t.calculateFee(); // Calculate ticket fee amount, stored in Ticket
							DriverGUI targetGUI = guiById.get(id); // Creates a DriverGUI object, initialized by
																	// receiving the GUI object from associated ID Map
							targetGUI.showUnpaidTicket(t); // GUI will show the unpaid Ticket
							break;
						}
						case OPERATORSUCCESS: {
							// if operator logged in successfully
							operatorGUI.loggedInSuccess();
							break;
						}
						case SETRATE: {
							setRate(msg);
							break;
						}
						case OPERATORFAILURE: {
							// if operator logged in successfully
							operatorGUI.loggedInFail();
							break;
						}
						case GETREPORT, GETREPORTBYMONTHYEAR: {
							operatorGUI.displayReport(msg.getOperator().getReport());
							break;
						}
						case SEARCHTICKET: {
							operatorGUI.displayTicket(msg.getTicket());
						}
						default:
							break;
						}
					}
				} catch (IOException | ClassNotFoundException e) {
					System.out.println("Receiver is disconnected.");
				}
			});
			receiver.start();
			// ======================LISTENING FOR INCOMING MESSAGE================

			// ==CREATE LICENSE PLATE READERS AND RUN THEM TO 'READ' LICENSE PLATES
			LicensePlateReader entryLPR1 = new LicensePlateReader(garageID, Location.Entry, queue);
			LicensePlateReader entryLPR2 = new LicensePlateReader(garageID, Location.Entry, queue);
			new Thread(entryLPR1).start();
			new Thread(entryLPR2).start();
			// == CREATE LICENSE PLATE READERS AND RUN THEM TO 'READ' LICENSE PLATES

			// ========= CREATE GARAGE EXIT GUI ==================

			// === DEFINE THE CALLBACK FUNCTION AND PASS TO THE GUI ===
			DriverGUIgetUnpaidTicketCB getUnpaidCallback = (int GuiID) -> { // Define call back function with parameter
																			// int
				// (GuiID)
				try {
					Message msg = new Message(MsgTypes.LOOKUPUNPAIDTICKET, garageID); // Create new message with message
																						// type
					// LOOKUPTICKET and this instance of
					// GarageID
					Ticket ticket = new Ticket(); // New Ticket object
					ticket.setGarageID(garageID);

					ticket.setGuiID(GuiID); // Set the Ticket's GuiID with the callback function's GuiID
					// System.out.println(GuiID);
					msg.setTicket(ticket); // Assign the Ticket object to the Message
					out.writeObject(msg); // Send Message to server
					out.flush(); // Flush stream
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			};

			// this will send a Message contained paid ticket to server, and server save the
			// Ticket to file
			DriverGUIpaidTicketCB paidTicketCallback = (int GuiID, Ticket ticket) -> {
				try {
					Message msg = new Message(MsgTypes.TICKETPAID, garageID);
					// System.out.println(ticket.toString());
					ticket.setTicketPaid();
					msg.setTicket(ticket);
					out.writeObject(msg);
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			// === DEFINE THE CALLBACK FUNCTION AND PASS TO THE GUI ===
			// Create a GUI with this Garage ID and the
			driverGUI1 = new DriverGUI(garageID, getUnpaidCallback, paidTicketCallback);
			new Thread(driverGUI1).start(); // Runs the first driver GUI in a thread
			guiById.put(driverGUI1.getGuiID(), driverGUI1); // Maps the GUI ID with the driver GUI

			driverGUI2 = new DriverGUI(garageID, getUnpaidCallback, paidTicketCallback);
			new Thread(driverGUI2).start(); // Runs the second driver GUI in a thread
			guiById.put(driverGUI2.getGuiID(), driverGUI2); // Maps the GUI ID with the driver GUI
			/*
			 * Note that both GUIs have the same Garage ID, though different GUIs. As the
			 * license plate reader allows any and all vehicles to enter, there can be
			 * multiple exit gates to allow multiple drivers to pay & exit.
			 */
			// ========= CREATE GARAGE EXIT GUI ==================

			// === DEFINE THE CALLBACK FUNCTION AND PASS TO THE Operator GUI ===
			OperatorGUILoginCB operatorLoginCallback = (String username, String pw) -> {
				try {
					Message msg = new Message(MsgTypes.OPERATORLOGIN, garageID);
					Operator operator = new Operator(username, pw, garageID);
					msg.setOperator(operator);
					out.writeObject(msg);
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};

			GUIgetReportCB operatorGetReportCallback = (int garageId) -> {
				try {
					Message msg = new Message(MsgTypes.GETREPORT, garageID);
					out.writeObject(msg);
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();

				}
				return null;
			};

			GUISearchTicketCB operatorGUISearchTicketCallback = (String licensePlate) -> {
				try {
					Message msg = new Message(MsgTypes.SEARCHTICKET, garageID);
					Ticket ticket = new Ticket(licensePlate, garageID, 0.0);
					// set entry time to null to show this ticket is not a regular ticket
					ticket.setEntryTime(null);
					msg.setTicket(ticket);
					out.writeObject(msg);
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			};

			OperatorGUISetRateCB operatorGUISetRateCallback = (double rate) -> {
			    try {
			        // Send new rate to the server so it can update PGMS.HOURLY_RATE
			        Message msg = new Message(MsgTypes.SETRATE, garageID);
			        Ticket rateTicket = new Ticket(garageID, rate);  // uses (int garageID, double rate) constructor
			        msg.setTicket(rateTicket);
			        out.writeObject(msg);
			        out.flush();

			        // Also save locally so this garage remembers rate across restarts
			        ratePerSecond = rate;
			        try (FileWriter writer = new FileWriter(garageRateFileName)) {
			            writer.write(String.valueOf(rate));
			        }

			        System.out.println("Operator set new hourly rate: " + rate);
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			};

			GUIgetReportByMonthYearCB getReportByMonthYearCallback = (int OptionalgarageID, int month, int year) -> {
				try {
					Message msg = new Message(MsgTypes.GETREPORTBYMONTHYEAR, garageID);
					Operator operator = new Operator();
					Report report = new Report();
					report.setMonth(month);
					report.setYear(year);
					operator.setReport(report);
					msg.setOperator(operator);
					out.writeObject(msg);
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();

				}
				return null;
			};
			// === DEFINE THE CALLBACK FUNCTION AND PASS TO THE Operator GUI ===

			// =========== Create/start Operator GUI ==================
			operatorGUI = new OperatorGUI(garageID, operatorLoginCallback, operatorGetReportCallback,
					operatorGUISearchTicketCallback, operatorGUISetRateCallback, getReportByMonthYearCallback);
			new Thread(operatorGUI).start();

		} catch (

		ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void setRate(Message msg) {
	    double newRate = msg.getTicket().getRate();
	    ratePerSecond = newRate;   // update client rate

	    System.out.println("New hourly rate received from server: " + newRate);

	    // Update gate for ALL driver GUIs so new tickets use new rate
	    if (driverGUI1 != null) driverGUI1.updateRate(newRate);
	    if (driverGUI2 != null) driverGUI2.updateRate(newRate);

	    try (FileWriter writer = new FileWriter(garageRateFileName)) {
	        writer.write(String.valueOf(newRate));
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
