package org.cytoscape.ding.customgraphicsmgr.internal;

import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.DummyCustomGraphics;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.CyCustomGraphics;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

public class ReplaceDummyGraphicsTask implements Task {

	private final VisualMappingManager vmm;
	private final CustomGraphicsManager customGraphicsManager;

	private final CyApplicationManager applicationManager;

	public ReplaceDummyGraphicsTask(final VisualMappingManager vmm, final CustomGraphicsManager customGraphicsManager,
			final CyApplicationManager applicationManager) {
		this.customGraphicsManager = customGraphicsManager;
		this.vmm = vmm;
		this.applicationManager = applicationManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final Set<VisualStyle> visualStyles = vmm.getAllVisualStyles();

		final Set<VisualProperty<CyCustomGraphics>> cgVisualProps = DVisualLexicon.getCustomGraphicsVisualProperties();

		for (final VisualStyle style : visualStyles) {

			for (final VisualProperty<CyCustomGraphics> vp : cgVisualProps) {
				// Step 1: Replace all default values.
				final CyCustomGraphics defValue = style.getDefaultValue(vp);
				if (defValue instanceof DummyCustomGraphics) {
					DummyCustomGraphics dummy = (DummyCustomGraphics) defValue;
					Long dummyID = dummy.getIdentifier();
					CyCustomGraphics replacement = customGraphicsManager.getCustomGraphicsByID(dummyID);
					if (replacement == null) {
						replacement = NullCustomGraphics.getNullObject();
					}

					// Replace it.
					style.setDefaultValue(vp, replacement);
				}
			}
		}
		// Apply the style and Update view

		final CyNetworkView currentView = applicationManager.getCurrentNetworkView();
		if(currentView != null) {
			vmm.getVisualStyle(currentView).apply(currentView);
			currentView.updateView();
		}
	}

	@Override
	public void cancel() {}

}
