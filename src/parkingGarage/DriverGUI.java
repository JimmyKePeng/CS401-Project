package parkingGarage;

//java.util
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;

//java.awt
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;

//javax.swing
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/*Upon Class Instantiation
 * Create Window
 * Create UI
 	*Create Panel
 	*Add buttons, labels, etc.
 		*Determine functionality of buttons, labels, etc.
*/
public class DriverGUI implements Runnable {

	private static int count = 0;
	private Gate gate;
	private int garageID;
	private int GuiID;
	private Ticket ticket;
	private DriverGUIgetUnpaidTicketCB getUnpaidCallback;
	private DriverGUIpaidTicketCB paidTicketCallback;

	private JButton leaveButton;
	private JLabel durationLabel, plateLabel, feeLabel, welcomeText, question;
	private JButton payButton;
	private double rate = 2.0;
	
	// Currently Unused
	// private LicensePlateReader LPR;
	// private JFrame frame;
	// this is used to store license plate from exit license plate reader
	// BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

	public DriverGUI(int garageID, DriverGUIgetUnpaidTicketCB getUnpaidCallback,
			DriverGUIpaidTicketCB paidTicketCallback) {
		this.gate = new Gate(garageID, Location.Exit);
		this.garageID = garageID;
		this.GuiID = ++count;
		// this.LPR = new LicensePlateReader(garageID, Location.Exit, queue);
		this.ticket = null;
		this.getUnpaidCallback = getUnpaidCallback;
		this.paidTicketCallback = paidTicketCallback;
	}

	public int getGarageID() {
		return garageID;
	}

	public int getGuiID() {
		return GuiID;
	}

	@Override
	public void run() {
		SwingUtilities.invokeLater(this::createWindow);
		// createWindow();
	}

	private void createWindow() {
//		String name = "Exit GUI for Garage ID #" + garageID + ", GUI #" + GuiID;
//		JFrame frame = new JFrame(name);
//		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//
//		createUI(frame);
//		frame.setSize(400, 400);
//		frame.setLocationRelativeTo(null); // Center on screen
//		frame.setVisible(true); // make visible
		String frameTitle = "Exit GUI for Garage ID #" + garageID + ", GUI #" + GuiID;
		JFrame frame = new JFrame(frameTitle);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setLayout(new FlowLayout(FlowLayout.CENTER));

		createUI(frame);

		frame.setSize(400, 400);

		frame.setLocationRelativeTo(null); // Center on screen
		frame.setVisible(true); // make visible
	}

	private void createUI(final JFrame frame) {
		JPanel panel = new JPanel();
		LayoutManager layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);

		welcomeText = new JLabel("Thanks for staying!");
		question = new JLabel("Ready to leave?");
		leaveButton = new JButton("Yes (Get Unpaid Ticket)");
		payButton = new JButton("Pay");
		payButton.setEnabled(false);

		// ===ORGANIZATION OF BUTTONS AND LABELS IN PANEL
		panel.add(Box.createRigidArea(new Dimension(0, 20)));
		panel.add(welcomeText);
		panel.add(Box.createRigidArea(new Dimension(0, 10)));
		panel.add(question);
		panel.add(Box.createRigidArea(new Dimension(0, 20)));
		panel.add(leaveButton);
		panel.add(Box.createRigidArea(new Dimension(0, 30)));

		// License Plate
		panel.add(new JLabel("License Plate:"));
		plateLabel = new JLabel("-");
		panel.add(plateLabel);
		panel.add(Box.createRigidArea(new Dimension(0, 10)));

		// Duration of Stay
		panel.add(new JLabel("Duration of Stay:"));
		durationLabel = new JLabel("-");
		panel.add(durationLabel);
		panel.add(Box.createRigidArea(new Dimension(0, 10)));

		// Amount Due
		panel.add(new JLabel("Amount Due:"));
		feeLabel = new JLabel("-");
		panel.add(feeLabel);
		panel.add(Box.createRigidArea(new Dimension(0, 30)));

		panel.add(payButton);
	
		
		frame.add(panel);
		// ===END OF ORGANIZATION OF BUTTONS AND LABELS IN PANEL

		payButton.addActionListener((ActionEvent e) -> {
			payButtonClicked();
		});

		leaveButton.addActionListener((ActionEvent e) -> {
			getUnpaidCallback.run(GuiID); // triggers client to send GET_UNPAID
		});	
		
		// result.add(durationLabel);
		// result.add(plateLabel);
		// result.add(feeLabel);
		// frame.getContentPane().setLayout(new BorderLayout(8, 8));
		// frame.getContentPane().add(panel, BorderLayout.NORTH);
		// frame.getContentPane().add(new JScrollPane(logArea), BorderLayout.CENTER);
	}
	
	public void updateRate(double newRate) {
	    this.rate = newRate;
	    System.out.println("Updated rate for GUI " + GuiID + ": " + rate);
	}
	

	
	/* ASYNCH FUNCTION
	 * Safe to call from ANY thread. 
	 * Pulls info from ticket, set that information onto labels & buttons if it exists. 
	 */
	public void showUnpaidTicket(Ticket t) {

		SwingUtilities.invokeLater(() -> {
			this.ticket = t;
	        if (t == null) {
	            durationLabel.setText("None");
	            plateLabel.setText("-");
	            feeLabel.setText("-");
	            payButton.setEnabled(false);
	            return;
	        }
				// appendLine("No unpaid ticket found.");	
	        t.setExitTime(java.time.LocalDateTime.now());
	        t.calculateFee(); 

		        if (t.getDurationOfStay() != null) {
		        	long hours = (long) Math.ceil(t.getDurationOfStay().getSeconds() / 3600.0);
		        	durationLabel.setText(hours + " hours");
		        } else {
		            durationLabel.setText("0 hours");  // fallback (should not happen)
		        }
		        plateLabel.setText(t.getLicensePlate());
		        feeLabel.setText(String.format("$%.2f", t.getFee()));
		        payButton.setEnabled(true);
		    });
		}

	
	//Allows the payment processor to use a Credit Card object to determine if payment is authorized
	public void payButtonClicked() {
		//CreditCard creditCard = new CreditCard();
		PaymentCollector paymentCollector = new PaymentCollector(new CreditCard());

		if (paymentCollector.validatePayment()) {
			welcomeText.setText("Gate is Open, Please Exit.");
			question.setText("Thank you for visiting!");
			leaveButton.setEnabled(false);
			payButton.setEnabled(false);
			Thread thread = new Thread(() -> {
				gate.openGate();
				SwingUtilities.invokeLater(() -> {
					resetGUI();
				});
			});
			thread.start();
			paidTicketCallback.run(GuiID, ticket);
		} else {
			welcomeText.setText("Invalid credit card!");
			question.setText("Please try paying again.");
			payButton.setEnabled(true);
			leaveButton.setEnabled(false);
		}

	}

	//Resets text for labels & buttons, resets buttons
	public void resetGUI() {
		durationLabel.setText("-");
		plateLabel.setText("-");
		feeLabel.setText("-");
		welcomeText.setText("Thanks for staying!");
		question.setText("Ready to leave?");
		// appendLine("Received unpaid ticket: " + t);
		payButton.setEnabled(false);
		leaveButton.setEnabled(true);
	}

}
