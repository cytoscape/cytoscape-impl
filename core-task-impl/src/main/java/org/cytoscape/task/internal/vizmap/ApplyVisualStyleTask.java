package org.cytoscape.task.internal.vizmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class ApplyVisualStyleTask extends AbstractNetworkViewCollectionTask {

	private final VisualMappingManager vmm;

	@ProvidesTitle
	public String getTitle() {
		return "Visual Style to be applied";
	}

	@Tunable(description = "Visual Style")
	public ListSingleSelection<VisualStyle> styles;

	public ApplyVisualStyleTask(final Collection<CyNetworkView> views, final VisualMappingManager vmm) {
		super(views);

		if (vmm == null)
			throw new NullPointerException("VisualMappingManager is null");
		this.vmm = vmm;

		final List<VisualStyle> vsList = new ArrayList<VisualStyle>(vmm.getAllVisualStyles());
		styles = new ListSingleSelection<VisualStyle>(vsList);
		if (vsList.size() > 0)
			styles.setSelectedValue(vsList.get(0));
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {

		tm.setProgress(0.0);

		int i = 0;
		int viewCount = networkViews.size();
		final VisualStyle selected = styles.getSelectedValue();
		for (final CyNetworkView view : networkViews) {
			selected.apply(view);
			view.updateView();
			vmm.setVisualStyle(selected, view);

			i++;
			tm.setProgress((i / (double) viewCount));
		}

		tm.setProgress(1.0);
	}
}
