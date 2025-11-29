package parkingGarage;

public interface GUISearchTicketCB {
	// OwnerGUI and OperatorGUI will use this function to search a ticket by
	// licensePlate
	Ticket run(String licensePlate);
}
