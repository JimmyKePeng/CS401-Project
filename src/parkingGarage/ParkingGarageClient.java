package parkingGarage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ParkingGarageClient {
	private static DriverGUI driverGUI1;
	private static DriverGUI driverGUI2;

	public static void main(String[] args) {
		int assignedID = -1;
		boolean loggedIn = false;
		double ratePerSecond = 1.0;
		// entrance license plate reader, plates will store in queue
		BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

		// store Gui to ID
		final Map<Integer, DriverGUI> guiById = new ConcurrentHashMap<>();

		try {
			Socket socket = new Socket("localhost", 7777);// IP address should replace localhost

			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			out.flush();
			InputStream inputStream = socket.getInputStream();
			ObjectInputStream in = new ObjectInputStream(inputStream);

			// let server know this is a new garage
			Message outMsg = new Message(MsgTypes.NEWGARAGE, -1);
			out.writeObject(outMsg);
			out.flush();

			// server's response
			Message inMsg = (Message) in.readObject();
			if (!loggedIn && inMsg.getMsgType() == MsgTypes.SUCCESS) {
				loggedIn = true;
				assignedID = inMsg.getGarageID();
			}

			// final garageID is assigned from the server;
			final int garageID = assignedID;
			final boolean running = loggedIn;

			// ========Listening for License Plate Reader to get license plate===========
			Thread sender = new Thread(() -> {
				try {
					while (running) {
						String plate = queue.take(); // blocks
						Ticket ticket = new Ticket(plate, garageID);
						Message Msg = new Message(MsgTypes.NEWTICKET, garageID);
						Msg.setTicket(ticket);
						out.writeObject(Msg);
						out.flush();
					}
				} catch (InterruptedException | IOException ie) {
					System.out.println("Sender is disconnected.");
				}
			});
			sender.start();
			// =========Listening for License Plate Reader to get license plate===========

			// =====================Listening Incoming Message================
			Thread receiver = new Thread(() -> {
				try {
					while (running) {
						Message msg = (Message) in.readObject();
						// handle message types
						switch (msg.getMsgType()) {
						case RECEIVED:
							// System.out.println("Server RECEIVED new Ticket!");
							break;
						case LOOKUPTICKET: {
							Ticket t = msg.getTicket();
							int id = t.getGuiID();

							System.out.println("Looking up tickets in GUI# " + id);

							t.calculateFee(ratePerSecond);
							DriverGUI targetGUI = guiById.get(id);
							targetGUI.showUnpaidTicket(t);
							break;
						}
						default:
							System.out.println("Unknown message: " + msg);
							break;
						}
					}
				} catch (IOException | ClassNotFoundException e) {
					System.out.println("Receiver is disconnected.");
				}
			});
			receiver.start();
			// ======================Listening Incoming Message================

			// ======== Create License Plate Reader and run them to read license plate===
			LicensePlateReader entryLPR1 = new LicensePlateReader(garageID, Location.Entry, queue);
			LicensePlateReader entryLPR2 = new LicensePlateReader(garageID, Location.Entry, queue);
			new Thread(entryLPR1).start();
			new Thread(entryLPR2).start();
			// ======== Create License Plate Reader and run them to read license plate===

			// ========= Create garage exit gui ==================
			// === Define the callback function and pass to GUI ===
			GUIgetUnpaidTicket getUnpaidCallback = (int GuiID) -> {
				try {
					Message msg = new Message(MsgTypes.LOOKUPTICKET, garageID);
					Ticket ticket = new Ticket();
					ticket.setGuiID(GuiID); // here i need to pass in the GuiID;
					// System.out.println(GuiID);
					msg.setTicket(ticket);
					out.writeObject(msg);
					out.flush();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			};
			driverGUI1 = new DriverGUI(garageID, getUnpaidCallback);
			new Thread(driverGUI1).start();
			guiById.put(driverGUI1.getGuiID(), driverGUI1);
			driverGUI2 = new DriverGUI(garageID, getUnpaidCallback);
			new Thread(driverGUI2).start();
			guiById.put(driverGUI2.getGuiID(), driverGUI2);
			// ========= Create garage exit gui ==================

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
