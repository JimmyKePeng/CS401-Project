package parkingGarage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
	private static volatile double ratePerSecond = GlobalVariables.initialRate;
	private static ObjectOutputStream out;
	private static ObjectInputStream in;

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
			Socket socket = new Socket(GlobalVariables.host, GlobalVariables.port);

			// Create ObjectOutputStream from the OutPutStream

			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();

			// Create ObjectInputStream from the InputStream
			in = new ObjectInputStream(socket.getInputStream());

			File file = new File(GlobalVariables.garageIDFilename);

			// check if this garage is already registered
			// if a garage is registered, there's a file saved garageID, a file saved rate
			if (file.exists()) {
				String garageNumber;
				String parkingRate;

				// read garageID from file
				try (Scanner scanner = new Scanner(file)) {
					garageNumber = scanner.nextLine().trim();
				}
				assignedID = Integer.parseInt(garageNumber);

				// read parking rate from file
				File rateFile = new File(GlobalVariables.garageRateFilename);
				try (Scanner scanner = new Scanner(rateFile)) {
					parkingRate = scanner.nextLine().trim();
				}
				ratePerSecond = Double.parseDouble(parkingRate);

				Message outMsg = new Message(MsgTypes.GARAGELOGIN, assignedID);
				// let server know that this garage is trying to login.
				send(outMsg);
				Message inMsg = (Message) in.readObject();
				if (!loggedIn && inMsg.getMsgType() == MsgTypes.SUCCESS) {
					loggedIn = true;
				}
			} else {
				// Create a new Message object, indicate MsgType as NEWGARAGE for new garage
				// connecting to the server
				Message outMsg = new Message(MsgTypes.NEWGARAGE, -1);
				send(outMsg);
				// Read Server Response through 'inMsg'
				Message inMsg = (Message) in.readObject();

				// when return message type is equal to SUCCESS, that means server successfully
				// created txt file that will store paid/unpaid tickets for this parking garage
				if (!loggedIn && inMsg.getMsgType() == MsgTypes.SUCCESS) {
					loggedIn = true;
					assignedID = inMsg.getGarageID();
					try (FileWriter writer = new FileWriter(GlobalVariables.garageIDFilename)) { // Opens file
						writer.write(String.valueOf(assignedID));// Writes assignedID to file
					}
					try (FileWriter writer = new FileWriter(GlobalVariables.garageRateFilename)) { // Opens file
						writer.write(String.valueOf(ratePerSecond));// Writes default rate to file
					}
				}

			}

			// Constant (final) ID is assigned
			// constant 'running' is assigned a bool if SUCCESS or otherwise
			final int garageID = assignedID;
			final boolean running = loggedIn;

			// ========LISTENING FOR LICENSE PLATE READER FOR LICENSE PLATE===========
			// everytime a license plate reader read a plate, it will add the license plate
			// into the queue, when plate is added to queue, it will create a new Ticket and
			// send to server
			Thread sender = new Thread(() -> { // Thread 'sender' is made, runs while logged in
				try {
					while (running) {
						String plate = queue.take(); // Queue will wait (block) until there is a plate in the queue

						// Display the car's license plate when they entered on OperatorGUI
						operatorGUI.displayLicensePlate(plate);

						// A new ticket object is made with the plate taken from the queue
						Ticket ticket = new Ticket(plate, garageID);
						Message Msg = new Message(MsgTypes.NEWTICKET, garageID);
						Msg.setTicket(ticket); // Ticket object is set to the message
						send(Msg);
					}
				} catch (InterruptedException ie) { // Exceptions to catch
					System.out.println("Sender is disconnected.");
				}
			});
			sender.start(); // Start Thread
			// ==== end of LISTENING FOR LICENSE PLATE READER FOR LICENSE PLATE=======

			// ====== begin LISTENING FOR INCOMING MESSAGE from server ================
			Thread receiver = new Thread(() -> { // Thread 'receiver' is made while logged in
				try {
					while (running) {
						// Read message from the input stream, wait until Message received
						Message msg = (Message) in.readObject();

						// Switch statement to handle message types
						switch (msg.getMsgType()) {
						case RECEIVED:
							// Server will respond with a Message when new ticket is Received
							break;

						case LOOKUPUNPAIDTICKET: {
							Ticket t = msg.getTicket(); // get the unpaid ticket from Server Message
							int id = t.getGuiID(); // Pull GUI ID from Ticket
							t.calculateFee(ratePerSecond); // Calculate ticket fee amount, stored in Ticket
							DriverGUI targetGUI = guiById.get(id); // get the DriverGUI object From the GuiById Map
							targetGUI.showUnpaidTicket(t); // have the GUI(DriverGUI) at exit show the unpaid Ticket
							break;
						}
						case SETRATE: {
							// if Owner of PGMS want to set rate for this garage, a SETRATE Message type
							// will be send to this client.
							setRate(msg.getTicket().getRate());
							break;
						}
						case OPERATORSUCCESS: {
							// if operator logged in successfully
							operatorGUI.loggedInSuccess();
							break;
						}
						case OPERATORFAILURE: {
							// if operator failed to log in
							operatorGUI.loggedInFail();
							break;
						}
						case GETREPORT, GETREPORTBYMONTHYEAR: {
							// After Operator press "GetReport" button, This client will send Message type
							// GETREPORT to Server, Server get the report and send it here.
							// OperatorGUI display the report
							operatorGUI.displayReport(msg.getOperator().getReport());
							break;
						}
						case SEARCHTICKET: {
							// After Operator press "Search" button, This client will send Message type
							// SEARCHTICKET to Server, Server search the ticket and and send it here.
							// OperatorGUI display the ticket
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
			// ============ end of LISTENING FOR INCOMING MESSAGE================

			// ==CREATE LICENSE PLATE READERS AND RUN THEM TO 'READ' LICENSE PLATES=
			LicensePlateReader entryLPR1 = new LicensePlateReader(garageID, Location.Entry, queue);
			LicensePlateReader entryLPR2 = new LicensePlateReader(garageID, Location.Entry, queue);
			new Thread(entryLPR1).start();
			new Thread(entryLPR2).start();
			// == CREATE LICENSE PLATE READERS AND RUN THEM TO 'READ' LICENSE PLATES=

			// =========begin CREATE GARAGE EXIT GUI ==================
			// === DEFINE THE CALLBACK FUNCTION AND PASS TO THE GUI ===
			DriverGUIgetUnpaidTicketCB getUnpaidCallback = (int GuiID) -> { // Define call back function with parameter
				Message msg = new Message(MsgTypes.LOOKUPUNPAIDTICKET, garageID);
				// LOOKUPTICKET with this instance of GarageID
				Ticket ticket = new Ticket();
				ticket.setGarageID(garageID);

				ticket.setGuiID(GuiID); // Set the Ticket's GuiID with the callback function's GuiID
				msg.setTicket(ticket); // Assign the Ticket object to the Message
				send(msg);
			};

			// this will send a Message contained paid ticket to server, and server save the
			// Ticket to file
			DriverGUIpaidTicketCB paidTicketCallback = (int GuiID, Ticket ticket) -> {
				try {
					Message msg = new Message(MsgTypes.TICKETPAID, garageID);
					// System.out.println(ticket.toString());
					ticket.setTicketPaid();
					msg.setTicket(ticket);
					send(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			// ==== DEFINE THE CALLBACK FUNCTION AND PASS TO THE GUI ======
			// ====so the GUI can tell ParkingGarage to do certain task ===
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
					// this function sends the Operator username/pw to server for authentication
					Message msg = new Message(MsgTypes.OPERATORLOGIN, garageID);
					Operator operator = new Operator(username, pw, garageID);
					msg.setOperator(operator);
					send(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};

			GUIgetReportCB operatorGetReportCallback = (int garageId) -> {
				try {
					// this function sends a Message to Server indicating that client wants a Report
					Message msg = new Message(MsgTypes.GETREPORT, garageID);
					send(msg);
				} catch (Exception e) {
					e.printStackTrace();

				}
				return null;
			};

			GUIgetReportByMonthYearCB getReportByMonthYearCallback = (int OptionalgarageID, int month, int year) -> {
				try {
					// this function sends a Message to Server indicating that client wants a Report
					// with filters of month/year
					Message msg = new Message(MsgTypes.GETREPORTBYMONTHYEAR, garageID);
					Operator operator = new Operator();
					Report report = new Report();
					report.setMonth(month);
					report.setYear(year);
					operator.setReport(report);
					msg.setOperator(operator);
					send(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			};
			GUISearchTicketCB operatorGUISearchTicketCallback = (String licensePlate) -> {
				try {
					// this function sends a Message to Server indicating that client wants to
					// serach for a ticket
					Message msg = new Message(MsgTypes.SEARCHTICKET, garageID);
					Ticket ticket = new Ticket(licensePlate, garageID);
					// set entry time to null to show this ticket is not a regular ticket
					// its a ticket to be search instead;
					ticket.setEntryTime(null);
					msg.setTicket(ticket);
					send(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			};

			OperatorGUISetRateCB operatorGUISetRateCallback = (double rate) -> {
				// this function let operator to set rate on this Parking Garage w/o contacting
				// Server
				setRate(rate);
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

	private static void send(Message msg) {
		synchronized (out) { // ensure sending message one at a time
			try {
				out.writeObject(msg);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void setRate(double rate) {
		ratePerSecond = rate;
		try (FileWriter writer = new FileWriter(GlobalVariables.garageRateFilename)) { // Opens file
			writer.write(String.valueOf(ratePerSecond));// Writes to assignedID to file
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
