package org.cytoscape.task.internal.table;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListSingleSelection;
import java.util.Map;
import java.util.HashMap;

public class MapTableToNetworkTablesTaskFactoryImpl extends AbstractTaskFactory implements MapTableToNetworkTablesTaskFactory {
	
	private final CyNetworkManager networkManager;
	private final CyApplicationManager applicationManager;
	private final CyRootNetworkManager rootNetworkManager;
	private final TunableSetter tunableSetter;

	public MapTableToNetworkTablesTaskFactoryImpl(final CyNetworkManager networkManager,
            final CyApplicationManager applicationManager,
			  final CyRootNetworkManager rootNetworkManager,
			  final TunableSetter tunableSetter)
	{
		this.networkManager = networkManager;
		this.applicationManager = applicationManager;
		this.rootNetworkManager = rootNetworkManager;
		this.tunableSetter = tunableSetter;
	}
	@Override
	public TaskIterator createTaskIterator() {
		throw new UnsupportedOperationException("This TaskFactory doesn't support createTaskIterator()");
	}


	@Override
	public TaskIterator createTaskIterator(
			Class<? extends CyIdentifiable> type, CyTable newGlobalTable,
			String mappingKey, MappingType mappingType) {
		ListSingleSelection<String> lss = new ListSingleSelection<String>(mappingType.getDescription());
		lss.setSelectedValue(mappingType.getDescription());
		
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("whichTable",lss);
		
		return tunableSetter.createTaskIterator(this.createTaskIterator(type,newGlobalTable,mappingKey), m);
	}

	@Override
	public TaskIterator createTaskIterator(
			Class<? extends CyIdentifiable> type, CyTable newGlobalTable,
			String mappingKey) {
		return new TaskIterator(1,new MapNetworkAttrTask(type,newGlobalTable,mappingKey,networkManager,applicationManager,rootNetworkManager));
	}

}
