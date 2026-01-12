package parkingGarage;

public class Gate implements Runnable {

	// Private Variables
	private static int count = 0;
	private int id;
	private Location gateLocation;
	private volatile boolean isOpen;
	private int garageID;

	// Default Constructor
	public Gate() {
		this.id = count++;
		this.gateLocation = Location.Exit;
		this.isOpen = false;
	}

	// Parameterized Constructor
	public Gate(int garageID, Location gateTypes) {
		this.id = count++;
		this.gateLocation = gateTypes;
		this.isOpen = false;
		this.garageID = garageID;
	}

	public int getGarageID() {
		return garageID;
	}

	public int getGateID() {
		return id;
	}

	@Override
	public void run() {
		openGate();
	}

	// Function to Open Gate
	public void openGate() {
		if (isOpen)
			return; // if its already opened, do nothing and return

		// Run a gate sensor object
		GateSensor sensor = new GateSensor();
		Thread sensorThread = new Thread(sensor);
		sensorThread.setDaemon(true);
		sensorThread.start();

		isOpen = true;
		while (isOpen) {
			try {
				Thread.sleep(3000); // check gate sensor every 3 seconds
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// if the sensor indicated car has left, we can exit loop and close gate;
			if (sensor.isCarExited()) {
				isOpen = false;

			}
		}
		isOpen = false;

	}

	public Location getGateType() {
		return gateLocation;
	}

	public boolean isGateOpen() {
		return isOpen;
	}
}
