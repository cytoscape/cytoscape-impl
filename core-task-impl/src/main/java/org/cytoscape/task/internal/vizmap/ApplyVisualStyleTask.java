package org.cytoscape.task.internal.vizmap;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class ApplyVisualStyleTask extends AbstractNetworkViewTask {
	
	@ProvidesTitle
	public String getTitle() {
		return "Visual Style to be applied";
	}
	
	@Tunable(description = "Visual Style")
	public ListSingleSelection<VisualStyle> styles;
	
	
	public ApplyVisualStyleTask(final CyNetworkView view, final VisualMappingManager vmm) {
		super(view);
		final List<VisualStyle> vsList = new ArrayList<VisualStyle>(vmm.getAllVisualStyles());
		styles = new ListSingleSelection<VisualStyle>(vsList);
		if(vsList.size()>0)
			styles.setSelectedValue(vsList.get(0));
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final VisualStyle selected = styles.getSelectedValue();
		
		// Simply apply the style to the view.
		selected.apply(view);
	}

}
