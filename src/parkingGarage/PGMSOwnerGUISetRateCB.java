package parkingGarage;

public interface PGMSOwnerGUISetRateCB {
	// PGMS will create this object and pass into thread(PGMS GUI), then GUI can
	// call this function to have PGMS send a Message over to ParkingGarage(client)
	// to change their rate, must pass garageID and rate, so we know which garage
	// need a rate change
	boolean run(int garageID, double rate);
}
