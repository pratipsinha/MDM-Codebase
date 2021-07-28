package com.ww.pim.audit.item;

import com.ibm.pim.collaboration.CollaborationHistoryEvent;

public class CollaborationItemHistoryObject implements Comparable<CollaborationItemHistoryObject> {

	private int id;
	private CollaborationHistoryEvent collabHistoryEvnt;

	public CollaborationItemHistoryObject(int id,
			CollaborationHistoryEvent collabHistoryEvnt) {
		this.id = id;
		this.collabHistoryEvnt = collabHistoryEvnt;
	}

	public int getId() {
		return id;
	}

	public CollaborationHistoryEvent getCollabHistoryEvnt() {
		return collabHistoryEvnt;
	}

	@Override 
	public int compareTo(CollaborationItemHistoryObject o) {
		if (o.getCollabHistoryEvnt().getDate().equals(this.getCollabHistoryEvnt().getDate()))
			return 0;
		return (o.getCollabHistoryEvnt().getDate().after(this.getCollabHistoryEvnt().getDate())) ? 1 : -1 ;
	}
	
	public String toString() {
		return this.id+"::"+this.getCollabHistoryEvnt().getDate()+"::"+this.getCollabHistoryEvnt().getDifferences();
	}
}