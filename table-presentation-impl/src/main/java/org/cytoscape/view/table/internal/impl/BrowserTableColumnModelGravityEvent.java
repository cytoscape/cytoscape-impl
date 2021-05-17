package org.cytoscape.view.table.internal.impl;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
