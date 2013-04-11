package com.MobMonkey.Models.ZenDesk;

import java.util.HashMap;

public class TicketResponse {

	private HashMap ticket;
	private HashMap audit;
	
	public HashMap getTicket() {
		return ticket;
	}

	public void setTicket(HashMap ticket) {
		this.ticket = ticket;
	}

	public HashMap getAudit() {
		return audit;
	}

	public void setAudit(HashMap audit) {
		this.audit = audit;
	}
}
