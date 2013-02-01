package org.cytoscape.filter.internal.quickfind.util;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import org.cytoscape.work.TaskMonitor;


/**
 * Task Monitor Stub.
 *
 * @author Ethan Cerami
 */
public class TaskMonitorBase implements TaskMonitor {
	private String status;
	private String title;
	private double progress;

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void setProgress(double progress) {
		this.progress = progress;
	}

	@Override
	public void setStatusMessage(String statusMessage) {
		status = statusMessage;
	}
}
