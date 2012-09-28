package org.cytoscape.tableimport.internal;

import java.io.InputStream;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
//import org.cytoscape.task.internal.table.MapTableToNetworkTablesTask;
//import org.cytoscape.task.internal.table.JoinTablesTask;
//import org.cytoscape.task.internal.table.UpdateAddedNetworkAttributes;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableValidator;
import static org.cytoscape.work.TunableValidator.ValidationState.OK;

public class CombineReaderAndMappingTask extends AbstractTask implements CyNetworkReader, TunableValidator {

	@ProvidesTitle
	public String getTitle() {
		return "Import Network From Table";
	}	

	@ContainsTunables
	public NetworkCollectionHelper networkCollectionHelperTask;
	
	@ContainsTunables
	public ImportNetworkTableReaderTask importTask;

	
	public CombineReaderAndMappingTask(final InputStream is, final String fileType,
		    final String inputName){
		this.importTask = new ImportNetworkTableReaderTask(is, fileType, inputName);
		this.networkCollectionHelperTask = new NetworkCollectionHelper();
	}

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		if ( importTask instanceof TunableValidator ) {
			ValidationState readVS = ((TunableValidator)importTask).getValidationState(errMsg);

			if ( readVS != OK )
				return readVS;
		}
		
		return OK;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.networkCollectionHelperTask.run(taskMonitor);
		this.importTask.setNodeMap(this.networkCollectionHelperTask.getNodeMap());
		this.importTask.setRootNetwork(this.networkCollectionHelperTask.getRootNetwork());
		this.importTask.run(taskMonitor);
	}

	
	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork arg0) {
		final CyNetworkView view = CytoscapeServices.cyNetworkViewFactory.createNetworkView(arg0);
		return view;
	}

	@Override
	public CyNetwork[] getNetworks() {
		return this.importTask.getNetworks();
	}

}
