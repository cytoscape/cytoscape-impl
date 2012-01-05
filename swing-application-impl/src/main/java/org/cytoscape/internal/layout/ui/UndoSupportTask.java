package org.cytoscape.internal.layout.ui;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

public class UndoSupportTask implements Task {

	private String name;
	private UndoSupport undo;
	private CyEventHelper eventHelper;
	private CyNetworkView view;

	public UndoSupportTask(String name, UndoSupport undo, CyEventHelper eventHelper, CyNetworkView view) {
		this.name = name;
		this.undo = undo;
		this.eventHelper = eventHelper;
		this.view = view;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		undo.postEdit(new LayoutEdit(name, eventHelper, view));
	}

	@Override
	public void cancel() {
	}
}
