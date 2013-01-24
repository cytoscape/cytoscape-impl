package org.cytoscape.task.internal.zoom;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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


import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;


public class FitSelectedTask extends AbstractNetworkViewTask {
	private final UndoSupport undoSupport;

	public FitSelectedTask(final UndoSupport undoSupport, final CyNetworkView v) {
		super(v);
		this.undoSupport = undoSupport;
	}

	public void run(TaskMonitor tm) {
		tm.setProgress(0.0);
		undoSupport.postEdit(new FitContentEdit("Fit Selected",
		                                                                 view));
		tm.setProgress(0.3);
		view.fitSelected();
		tm.setProgress(1.0);
	}
}
