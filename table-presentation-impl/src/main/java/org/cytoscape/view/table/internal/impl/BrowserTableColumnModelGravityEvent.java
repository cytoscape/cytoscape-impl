package org.cytoscape.view.table.internal.impl;

public class BrowserTableColumnModelGravityEvent {

	private final Long column1Suid;
	private final double column1Gravity;
	private final Long column2Suid;
	private final double column2Gravity;
	
	
	public BrowserTableColumnModelGravityEvent(Long column1Suid, double column1Gravity, Long column2Suid, double column2Gravity) {
		this.column1Suid = column1Suid;
		this.column1Gravity = column1Gravity;
		this.column2Suid = column2Suid;
		this.column2Gravity = column2Gravity;
	}

	
	public Long getColumn1Suid() {
		return column1Suid;
	}

	public double getColumn1Gravity() {
		return column1Gravity;
	}

	public Long getColumn2Suid() {
		return column2Suid;
	}

	public double getColumn2Gravity() {
		return column2Gravity;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BrowserTableColumnModelGravityEvent [column1Suid=");
		builder.append(column1Suid);
		builder.append(", column1Gravity=");
		builder.append(column1Gravity);
		builder.append(", column2Suid=");
		builder.append(column2Suid);
		builder.append(", column2Gravity=");
		builder.append(column2Gravity);
		builder.append("]");
		return builder.toString();
	}
	
}
