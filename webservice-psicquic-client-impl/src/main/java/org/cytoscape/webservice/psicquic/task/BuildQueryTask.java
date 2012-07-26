package org.cytoscape.webservice.psicquic.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.RegistryManager;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.webservice.psicquic.mapper.MergedNetworkBuilder;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class BuildQueryTask extends AbstractTask {

	@Tunable(description = "Select column send as query:")
	public ListSingleSelection<String> columnList;

	private PSICQUICRestClient client;
	private final RegistryManager manager;

	private final CyTable table;
	private final View<CyNode> nodeView;

	private final CyEventHelper eh;
	private final VisualMappingManager vmm;

	private final CyLayoutAlgorithmManager layouts;
	private final MergedNetworkBuilder builder;

	private final CyNetworkView netView;

	private final Map<String, CyColumn> colName2column;

	BuildQueryTask(final CyNetworkView netView, final View<CyNode> nodeView, CyEventHelper eh,
			VisualMappingManager vmm, final PSICQUICRestClient client, final RegistryManager manager,
			final CyLayoutAlgorithmManager layouts, final MergedNetworkBuilder builder) {
		this.table = netView.getModel().getDefaultNodeTable();
		this.nodeView = nodeView;
		this.manager = manager;
		this.eh = eh;
		this.vmm = vmm;
		this.client = client;
		this.layouts = layouts;
		this.builder = builder;
		this.netView = netView;

		colName2column = new HashMap<String, CyColumn>();
		final Collection<CyColumn> columns = table.getColumns();

		final CyRow row = table.getRow(nodeView.getModel().getSUID());

		String defaultSelection = null;
		for (CyColumn col : columns) {
			final Object val = row.get(col.getName(), col.getType());
			if (val != null && col.getType() == String.class) {
				final String labelString = col.getName() + "(" + val.toString() + ")";
				colName2column.put(labelString, col);
				if (col.getName().equals(CyNetwork.NAME))
					defaultSelection = labelString;
			}
		}
		columnList = new ListSingleSelection<String>(new ArrayList<String>(colName2column.keySet()));

		if (defaultSelection != null)
			columnList.setSelectedValue(defaultSelection);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		final String selectedStr = columnList.getSelectedValue();
		final CyColumn selected = colName2column.get(selectedStr);
		final Object value = table.getRow(nodeView.getModel().getSUID()).get(selected.getName(), selected.getType());

		if (value == null)
			throw new NullPointerException("Selected column value is null: " + selected.getName());

		final String query = value.toString();
		SearchRecoredsTask searchTask = new SearchRecoredsTask(client, SearchMode.INTERACTOR);
		final Map<String, String> activeSource = manager.getActiveServices();
		searchTask.setQuery(query);
		searchTask.setTargets(activeSource.values());

		final ProcessSearchResultTask expandTask = new ProcessSearchResultTask(query, client, searchTask, netView,
				nodeView, eh, vmm, layouts, manager, builder);

		insertTasksAfterCurrentTask(searchTask, expandTask);

	}

}
