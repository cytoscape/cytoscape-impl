package org.cytoscape.browser.internal.task;

import org.cytoscape.browser.internal.view.TableBrowserMediator;
import org.cytoscape.model.CyColumn;
import org.cytoscape.task.AbstractTableColumnTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class ToggleTextWrapTask extends AbstractTableColumnTask {

	private final TableBrowserMediator mediator;
	
	@ProvidesTitle
	public String getTitle() {
		return "Hide Column";
	}
	
	public ToggleTextWrapTask(CyColumn column, TableBrowserMediator mediator) {
		super(column);
		this.mediator = mediator;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Toggle Text Wrap");
		tm.setStatusMessage("Toggling text wrap of column '" + column.getName() + "'...");
		tm.setProgress(-1);
		
		mediator.toggleTextWrap(column);
	}
}