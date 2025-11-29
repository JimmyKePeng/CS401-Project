package parkingGarage;

public interface OperatorGUISetRateCB {
	// ParkingGarage will create this callback function and pass it into
	// OperatorGUI, so OperatorGUI can call Parking Garage to update the parking
	// rate, and save the new parking rate into the txt file
	void run(double rate);
}
