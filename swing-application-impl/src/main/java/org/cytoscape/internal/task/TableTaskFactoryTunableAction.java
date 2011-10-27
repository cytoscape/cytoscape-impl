package org.cytoscape.internal.task;

import java.awt.event.ActionEvent;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

public class TableTaskFactoryTunableAction extends TaskFactoryTunableAction<TableTaskFactory>{
	
	public TableTaskFactoryTunableAction(
			DialogTaskManager manager,
			TableTaskFactory factory, @SuppressWarnings("rawtypes") Map serviceProps,
			final CyApplicationManager applicationManager) {
		super(manager, factory, serviceProps, applicationManager);
	}

	public void actionPerformed(ActionEvent a) {
		factory.setTable(applicationManager.getCurrentTable());
		super.actionPerformed(a);
	}

}
