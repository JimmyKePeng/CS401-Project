package parkingGarage;

import java.util.Random;

public class GateSensor implements Runnable {
	
	//Private Variables
	private boolean running;
	private boolean carDetected;
	private boolean carExited;

	//Default Constructor
	public GateSensor() {
		running = true;
		carDetected = false;
		carExited = false;
	}

	@Override
	public void run() {
		reset(); //Resets state of sensors
		Random random = new Random();
		int randomNumber;
		while (running) {
			try {
				Thread.sleep(3000); // check it every 3 second
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//If no car has been detected in the sensors, do the following
			if (!carDetected) {
				randomNumber = random.nextInt(6);
				if (randomNumber % 3 == 0) { // pretend the car is in the lane now
					carDetected = true;
				}
			}
			//If the car has passed, or is on the sensor, do the following
			else { 
				randomNumber = random.nextInt(6);
				if (randomNumber % 3 == 0) { // pretend the car left the lane
					carDetected = false;
					carExited = true;
					running = false; // exit the loop
					return;
				}
			}
		}
	}

	public void reset() { // reset sensor states
		running = true;
		carDetected = false;
		carExited = false;
	}

	public boolean getCarDetected() {
		return carDetected;
	}

	public boolean isCarExited() {
		return carExited;
	}
}
