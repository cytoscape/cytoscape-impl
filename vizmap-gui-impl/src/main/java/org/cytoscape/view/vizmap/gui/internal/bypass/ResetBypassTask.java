package org.cytoscape.view.vizmap.gui.internal.bypass;

import java.util.Collection;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ResetBypassTask extends AbstractTask {

	private final View<? extends CyIdentifiable> view;
	private final CyNetworkView networkView;

	private final Collection<VisualProperty<?>> vpSet;

	public ResetBypassTask(final VisualLexicon lexicon, final View<? extends CyIdentifiable> view,
			final CyNetworkView networkView) {
		this.view = view;
		this.networkView = networkView;

		final CyIdentifiable model = view.getModel();
		if (model instanceof CyNode) {
			vpSet = lexicon.getAllDescendants(BasicVisualLexicon.NODE);
		} else {
			vpSet = lexicon.getAllDescendants(BasicVisualLexicon.EDGE);
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		boolean needToUpdateView = false;
		
		for (VisualProperty<?> vp : vpSet) {
			final boolean lock = view.isValueLocked(vp);
			if (lock) {
				view.clearValueLock(vp);
				needToUpdateView = true;
			}
		}
		
		if(needToUpdateView)
			networkView.updateView();
	}

}
