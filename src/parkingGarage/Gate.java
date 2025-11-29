package parkingGarage;

public class Gate implements Runnable {

	// Private Variables
	private static int count = 0;
//	private GateSensor sensor;
	private int id;
	private Location gateLocation;
	private volatile boolean isOpen;
	private int garageID;

	// Default Constructor
	public Gate() {
		this.id = count++;
//		this.sensor = new GateSensor();
		this.gateLocation = Location.Exit;
		this.isOpen = false;
	}

	// Parameterized Constructor
	public Gate(int garageID, Location gateTypes) {
		this.id = count++;
//		this.sensor = new GateSensor();
		this.gateLocation = gateTypes;
		this.isOpen = false;
		this.garageID = garageID;
	}

	private double rate = 2.0;   // default hourly rate
	public double getRate() {
	    return rate;
	}

	public void setRate(double newRate) {
	    this.rate = newRate;
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

//		System.out.println(
//				(gateLocation == Location.Entry ? "Entry" : "Exit") + " Gate" + Integer.toString(id) + " Gate opened!");
		isOpen = true;
		while (isOpen) {
			try {
				Thread.sleep(3000); // check gate every 3 seconds
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (sensor.isCarExited()) {
//				System.out.println((gateLocation == Location.Entry ? "Entry" : "Exit") + " Gate" + Integer.toString(id)
//						+ " Gate closed!");
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
