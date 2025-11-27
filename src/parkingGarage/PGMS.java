package parkingGarage;

import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PGMS { // Parking Garage Management System
	private static final List<List<Ticket>> PAIDTICKETS = new ArrayList<>();
	private static final List<List<Ticket>> UNPAIDTICKETS = new ArrayList<>();
	private static int garageCount = 0;

	public static void main(String[] args) {
		ServerSocket server = null;

		try {
			server = new ServerSocket(7777);
			server.setReuseAddress(true);

			while (true) {
				Socket client = server.accept(); // socket object to receive incoming client

				client.getInetAddress().getHostAddress();

				ClientHandler clientSock = new ClientHandler(client); // create a new thread object
				new Thread(clientSock).start();// This thread will handle the client separately
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

	private static class ClientHandler implements Runnable {

		private final Socket clientSocket;
		private int garageID;
		boolean loggedIn = false;
		private MsgTypes msgType;

		// Constructor
		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {

			try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

				Message inMsg;

				while ((inMsg = (Message) in.readObject()) != null) {
					msgType = inMsg.getMsgType();

					// created new garage.
					if (!loggedIn) {
						if (msgType == MsgTypes.NEWGARAGE) {
							garageID = garageCount++;
							createNewGarage(garageID);
							loggedIn = true;
							inMsg = new Message(MsgTypes.SUCCESS, garageID);
							out.writeObject(inMsg);
						}
					}

					// if garage is registered
					else {

						switch (msgType) {
						case NEWTICKET: {
							UNPAIDTICKETS.get(garageID).add(inMsg.getTicket());
							String fileNameUnpaid = "garage#" + garageID + "_unpaid.txt";
							try (FileWriter writer = new FileWriter(fileNameUnpaid, true)) {
								writer.write(inMsg.getTicket().toString());
							}
							inMsg = new Message(MsgTypes.RECEIVED, garageID);
							out.writeObject(inMsg);
							out.flush();

							break;
						}
						case LOOKUPTICKET: {
							List<Ticket> tickets = UNPAIDTICKETS.get(garageID);

							if (tickets != null && !tickets.isEmpty()) {
								Random random = new Random();
								Ticket ticket = tickets.get(random.nextInt(tickets.size()));

								Message reply = new Message(MsgTypes.LOOKUPTICKET, garageID);
								Ticket copy = new Ticket();
								copy.setGuiID(inMsg.getTicket().getGuiID());
								copy.setLicensePlate(ticket.getLicensePlate());
								copy.setEntryTime(ticket.getEntryTime());

								reply.setTicket(copy);
								// inMsg.setTicket(ticket);
								out.writeObject(reply);
								out.flush();
							}

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

		private void createNewGarage(int garageID) {
			List<Ticket> unPaidList = new ArrayList<Ticket>();
			UNPAIDTICKETS.add(unPaidList); // first garage is on UNPAIDTICKETS[0];
			List<Ticket> paidList = new ArrayList<Ticket>();
			PAIDTICKETS.add(paidList);
			String fileNamePaid = "garage#" + Integer.toString(garageID) + "_paid.txt";
			String fileNameUnpaid = "garage#" + Integer.toString(garageID) + "_unpaid.txt";
			try (FileWriter writerPaid = new FileWriter(fileNamePaid, true);
					FileWriter writerUnpaid = new FileWriter(fileNameUnpaid, true)) {
				System.out.println("Appended to file!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
