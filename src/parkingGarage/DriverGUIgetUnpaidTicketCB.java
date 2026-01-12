package parkingGarage;

public interface DriverGUIgetUnpaidTicketCB {
	// This is used as an function. created it on ParkingGarage and pass it into the
	// thread(DriverGUI). so the DriverGUI can trigger ParkingGarage to send a
	// Message to Server to pull up an unpaid ticket
	void run(int GuiID);
}
