package org.cytoscape.task.internal.quickstart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import org.cytoscape.task.internal.quickstart.QuickStartState.Job;
import org.cytoscape.work.AbstractTask;
//import org.cytoscape.work.ProvidesGUI;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class SelectMappingKeyTypeTask extends AbstractTask {
	
	public String otherIDType = null;
	
	private final QuickStartState state;
	
	private final Map<String, IDType> idTypeMap;
	private final ImportTaskUtil util;
	private MappingKeyTypePanel mappingKeyTypePanel = null;

	private List<String> values = new ArrayList<String>();
	private String[] previewKeys;
	private String[][] previewData;
	
	public SelectMappingKeyTypeTask(final QuickStartState state, ImportTaskUtil util, 
			String[] previewKeys, String[][] previewData) {
		this.idTypeMap = new HashMap<String, IDType>();
		this.state = state;	
		this.util = util;
		this.previewKeys = previewKeys;
		this.previewData = previewData;
				
		for(IDType val: IDType.values()) {
			values.add(val.getDisplayName());
			this.idTypeMap.put(val.getDisplayName(), val);
		}
	}

	
	//@ProvidesGUI
	public JPanel getGUI() {
		if (mappingKeyTypePanel == null){
			this.mappingKeyTypePanel = new MappingKeyTypePanel(values, previewKeys, previewData);			
		}
				
		return mappingKeyTypePanel;
	}
	
	
	@Override
	public void run(TaskMonitor monitor) throws Exception {
		monitor.setProgress(0.0);
		// Check current status and move to next task.
		if(state.isJobFinished(Job.LOAD_NETWORK) && state.isJobFinished(Job.LOAD_TABLE)) {
			insertTasksAfterCurrentTask(new MergeDataTask(state, util));
		} else if(state.isJobFinished(Job.LOAD_NETWORK)) {
			// Need to load table.
			insertTasksAfterCurrentTask(new LoadTableTask(state, util));
		} else if(state.isJobFinished(Job.LOAD_TABLE)) {
			// Need to load network.
			insertTasksAfterCurrentTask(new LoadNetworkTask(state, util));
		}	
		monitor.setProgress(0.2);
		String selected = mappingKeyTypePanel.getSelectedValue();
		otherIDType = mappingKeyTypePanel.getOtherIDType();
		if (otherIDType != null && !otherIDType.equalsIgnoreCase("")){
			selected = otherIDType;
		}
		monitor.setProgress(0.5);
		state.setIDType(this.idTypeMap.get(selected));
		
		state.finished(Job.SELECT_MAPPING_ID_TYPE);
		monitor.setProgress(1.0);
	}

}
