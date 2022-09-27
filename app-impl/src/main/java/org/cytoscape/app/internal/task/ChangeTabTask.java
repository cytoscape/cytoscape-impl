package org.cytoscape.app.internal.task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.service.util.CyServiceRegistrar;


public class ChangeTabTask extends AbstractAppTask implements ObservableTask {
  private CyServiceRegistrar registrar;
  private CytoPanel cytoPanelWest;
	public ChangeTabTask(final AppManager appManager, CyServiceRegistrar registrar) {
    super(appManager);
    this.registrar = registrar;
	}

  @Override
	public void run(TaskMonitor taskMonitor) throws Exception {
    CySwingApplication swingApplication = registrar.getService(CySwingApplication.class);
    cytoPanelWest = swingApplication.getCytoPanel(CytoPanelName.WEST);
    cytoPanelWest.setSelectedIndex(0);
    }

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, JSONResult.class);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <R> R getResults(Class<? extends R> type) {
		return null;
	}
}
