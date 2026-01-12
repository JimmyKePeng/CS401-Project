package parkingGarage;

public interface OperatorGUILoginCB {
	// ParkingGarage will create this callback function and pass it into
	// OperatorGUI, so OperatorGUI can call Parking Garage to send Message to Server
	// to authenticate operator username/pw
	void run(String username, String pw);
}
