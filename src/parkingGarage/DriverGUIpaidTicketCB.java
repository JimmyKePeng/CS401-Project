package parkingGarage;

public interface DriverGUIpaidTicketCB {
	// This is used as an function. created it on ParkingGarage and pass it into the
	// thread(DriverGUI). so the DriverGUI can trigger ParkingGarage to send a
	// Message(including a ticket) to Server indicate that this ticket is paid.
	void run(int GuiID, Ticket ticket);
}
