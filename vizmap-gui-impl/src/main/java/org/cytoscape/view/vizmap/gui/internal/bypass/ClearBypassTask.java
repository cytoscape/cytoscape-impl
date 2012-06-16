package org.cytoscape.view.vizmap.gui.internal.bypass;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ClearBypassTask extends AbstractTask {

	private final View<? extends CyIdentifiable> view;
	private final VisualProperty<Object> vp;

	private final CyNetworkView networkView;

	public ClearBypassTask(final VisualProperty<?> vp, final View<? extends CyIdentifiable> view,
			final CyNetworkView networkView) {
		this.view = view;
		this.vp = (VisualProperty<Object>) vp;
		this.networkView = networkView;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		final boolean lock = view.isValueLocked(vp);
		if (lock) {
			view.clearValueLock(vp);
			networkView.updateView();
		}
	}

}