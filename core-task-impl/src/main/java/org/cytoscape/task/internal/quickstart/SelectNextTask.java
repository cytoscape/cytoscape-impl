package org.cytoscape.task.internal.quickstart;

import org.cytoscape.task.internal.quickstart.subnetworkbuilder.CreateSubnetworkFromSearchTask;
import org.cytoscape.task.internal.quickstart.subnetworkbuilder.CreateSubnetworkWithoutGeneListTask;
import org.cytoscape.task.internal.quickstart.subnetworkbuilder.SearchRelatedGenesTask;
import org.cytoscape.task.internal.quickstart.subnetworkbuilder.SubnetworkBuilderState;
import org.cytoscape.task.internal.quickstart.subnetworkbuilder.SubnetworkBuilderUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class SelectNextTask extends AbstractTask {
	
	private static final String SEARCH1_OPTION = "Create subnetwork from gene list";
	private static final String SEARCH2_OPTION = "Create subnetwork related to phynotypes/diseases";
	private static final String FINISH_OPTION = "Finished";
	
	@Tunable(description = "What would you like to do next?")
	public ListSingleSelection<String> selection = new ListSingleSelection<String>(SEARCH1_OPTION, SEARCH2_OPTION, FINISH_OPTION);
	
	private final SubnetworkBuilderUtil subnetworkUtil;

	
	SelectNextTask(final SubnetworkBuilderUtil subnetworkUtil) {
		this.subnetworkUtil = subnetworkUtil;
	}
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final String selected = selection.getSelectedValue();
		
		if(selected == SEARCH2_OPTION) {
			//insertTasksAfterCurrentTask(new SearchRelatedGenesTask(subnetworkUtil, new SubnetworkBuilderState()));
		} else if(selected == SEARCH1_OPTION) {
			//insertTasksAfterCurrentTask(new CreateSubnetworkWithoutGeneListTask(subnetworkUtil, new SubnetworkBuilderState()));
		}
	}

}
