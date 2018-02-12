package org.cytoscape.task.internal.zoom;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class FitContentTask extends AbstractTask {
	
	private final CyServiceRegistrar serviceRegistrar;

	CyNetworkView tunableView = null;
	@Tunable(description="Network View to export", 
	         longDescription=StringToModel.CY_NETWORK_VIEW_LONG_DESCRIPTION,
	         exampleStringValue=StringToModel.CY_NETWORK_VIEW_EXAMPLE_STRING,
	         context="nogui")
	public CyNetworkView getView() {
		return tunableView;
	}
	public void setView(CyNetworkView setView) {
		tunableView = setView;
	}

	public FitContentTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	public FitContentTask(CyNetworkView view, CyServiceRegistrar serviceRegistrar) {
		tunableView = view;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Fit Content");
		tm.setProgress(0.0);
		
		if (tunableView == null) {
			tunableView = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		}

		if (tunableView == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "No view to fit");
			return;
		}

		serviceRegistrar.getService(UndoSupport.class).postEdit(new FitContentEdit("Fit Content", tunableView));
		tm.setProgress(0.3);
		
		tunableView.fitContent();
		tm.setProgress(1.0);
	}
}
