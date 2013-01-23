package org.cytoscape.app.internal.net;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

public class DownloadStatus {
	private final TaskMonitor taskMonitor;
	private volatile boolean isCanceled;

	public DownloadStatus(TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
	}
	
	public TaskMonitor getTaskMonitor() {
		return taskMonitor;
	}
	
	public boolean isCanceled() {
		return isCanceled;
	}
	
	public void cancel() {
		isCanceled = true;
	}
}
