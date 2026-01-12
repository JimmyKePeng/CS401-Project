package parkingGarage;

import java.io.Serializable;

public class Message implements Serializable {
	private MsgTypes msgType;

	// Message contain the msgType/Operator/Ticket
	// theres no Report in Message because Operator will contain a Report
	private int garageID;
	private Operator operator;
	private Ticket ticket;

	public Message() {
		this.msgType = MsgTypes.UNDEFINED;
		this.garageID = 0;
	}

	public Message(MsgTypes msgType, int garageID) {
		this.msgType = msgType;
		this.garageID = garageID;
	}

	public int getGarageID() {
		return garageID;
	}

	public MsgTypes getMsgType() {
		return msgType;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

}