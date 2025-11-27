package parkingGarage;

import java.io.Serializable;

public class Message implements Serializable {
	private MsgTypes msgType;

	private int garageID;
	private Operator operator; // to be implemented
	private Ticket ticket; // to be implemented

	public Message() {
		this.msgType = MsgTypes.UNDEFINED;
		this.garageID = 0;
	}

	public Message(MsgTypes msgType, int garageID) {
		this.msgType = msgType;
		this.garageID = garageID;
	}
