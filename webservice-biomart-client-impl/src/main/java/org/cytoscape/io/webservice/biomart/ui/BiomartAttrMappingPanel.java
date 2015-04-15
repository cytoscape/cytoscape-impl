package org.cytoscape.io.webservice.biomart.ui;

/*
 * #%L
 * Cytoscape Biomart Webservice Impl (webservice-biomart-client-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.webservice.TableImportWebServiceClient;
import org.cytoscape.io.webservice.biomart.BiomartClient;
import org.cytoscape.io.webservice.biomart.BiomartQuery;
import org.cytoscape.io.webservice.biomart.rest.Attribute;
import org.cytoscape.io.webservice.biomart.rest.Dataset;
import org.cytoscape.io.webservice.biomart.rest.Filter;
import org.cytoscape.io.webservice.biomart.rest.XMLQueryBuilder;
import org.cytoscape.io.webservice.biomart.task.ImportAttributeListTask;
import org.cytoscape.io.webservice.biomart.task.ImportFilterTask;
import org.cytoscape.io.webservice.biomart.task.LoadRepositoryResult;
import org.cytoscape.io.webservice.biomart.task.LoadRepositoryTask;
import org.cytoscape.io.webservice.biomart.task.ShowBiomartDialogTask;
import org.cytoscape.io.webservice.swing.WebServiceGUI;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

public class BiomartAttrMappingPanel extends AttributeImportPanel {

	private static final long serialVersionUID = 3574198525811249639L;

	private static final Icon LOGO = new ImageIcon(
			BiomartAttrMappingPanel.class.getResource("/images/BioMartLogo.png"));

	private Map<String, String> datasourceMap;

	private Map<String, Map<String, String[]>> attributeMap;
	private Map<String, List<String>> attributeListOrder;
	private Map<String, Map<String, String>> attrNameMap;
	private Map<String, Map<String, String>> filterMap;

	public enum SourceType {
		DATABASE, ATTRIBUTE, FILTER;
	}

	private final DialogTaskManager taskManager;
	private final CyApplicationManager appManager;

	private BiomartClient client;

	private int globalTableCounter;

	private boolean initialized = false;

	private WebServiceGUI webServiceGUI;

	public BiomartAttrMappingPanel(final DialogTaskManager taskManager, final CyApplicationManager appManager,
			final CyTableManager tblManager, final CyNetworkManager netManager, WebServiceGUI webServiceGUI) {
		super(tblManager, netManager, LOGO, null, "Import Settings");

		this.taskManager = taskManager;
		this.appManager = appManager;
		this.globalTableCounter = 0;
		
		this.webServiceGUI = webServiceGUI;
	}

	public void setClient(final BiomartClient client) {
		this.client = client;

		this.addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorRemoved(AncestorEvent arg0) {
			}

			@Override
			public void ancestorMoved(AncestorEvent arg0) {
			}

			@Override
			public void ancestorAdded(AncestorEvent arg0) {
				if(!initialized)
					initPanel();
			}
		});
	}

	private void initPanel() {
		LoadRepositoryTask firstTask = new LoadRepositoryTask(client.getRestClient());
		ShowBiomartDialogTask showDialogTask = new ShowBiomartDialogTask(this, firstTask);

		Window parentWindow = webServiceGUI.getWindow(TableImportWebServiceClient.class);
		taskManager.setExecutionContext(parentWindow);
		taskManager.execute(new TaskIterator(firstTask, showDialogTask));
		initialized = true;
	}

	public void initDataSources(LoadRepositoryResult res) {
		attributeMap = new HashMap<String, Map<String, String[]>>();
		attributeListOrder = new HashMap<String, List<String>>();
		filterMap = new HashMap<String, Map<String, String>>();
		attrNameMap = new HashMap<String, Map<String, String>>();

		// Import list of repositories.
		setMartServiceList(res);
	}

	public void setMartServiceList(LoadRepositoryResult res) {
		databaseComboBox.removeAllItems();
		
		this.datasourceMap = res.getDatasourceMap();
		final List<String> dsList = res.getSortedDataSourceList();
		for (String ds : dsList)
			this.databaseComboBox.addItem(ds);		
	}

	public void loadFilter() {
		Object selected = databaseComboBox.getSelectedItem();
		if(selected == null)
			return;
		
		attributeTypeComboBox.removeAllItems();
		final String selectedDB = selected.toString();
		final String selectedDBName = datasourceMap.get(selectedDB);

		fetchData(selectedDBName, SourceType.FILTER);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void selectAllButtonActionPerformed(ActionEvent e) {
		final Object[] elements = ((DefaultListModel)attrCheckboxList.getModel()).toArray();
		
		if (elements != null && elements.length >= 0)
			attrCheckboxList.setSelectedItems((List)Arrays.asList(elements));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void selectNoneButtonActionPerformed(ActionEvent e) {
		attrCheckboxList.setSelectedItems(Collections.EMPTY_LIST);
	}

	@Override
	protected void databaseComboBoxActionPerformed(ActionEvent evt) {
		updateAttributeList();
		loadFilter();
	}

	private void updateAttributeList() {
		Object selected = databaseComboBox.getSelectedItem();
		if(selected == null)
			return;
		
		final String selectedDB = selected.toString();
		final String selectedDBName = datasourceMap.get(selectedDB);

		List<String> order = attributeListOrder.get(selectedDBName);
		attrCheckboxListModel = new DefaultListModel();
		attrCheckboxList = new CheckBoxJList();

		if (order != null) {
			for (String dispAttrName : order)
				attrCheckboxListModel.addElement(dispAttrName);
			attrCheckboxList.setModel(attrCheckboxListModel);
			availableAttrScrollPane.setViewportView(attrCheckboxList);
			availableAttrScrollPane.repaint();
		} else {
			fetchData(selectedDBName, SourceType.ATTRIBUTE);
		}
	}

	private void fetchData(final String datasourceName, final SourceType type) {
		taskManager.setExecutionContext(null);

		if (type.equals(SourceType.ATTRIBUTE)) {
			final ImportAttributeListTask firstTask = new ImportAttributeListTask(datasourceName,
					client.getRestClient());
			final SetAttributeTask setAttrTask = new SetAttributeTask(firstTask);
			taskManager.execute(new TaskIterator(firstTask, setAttrTask));
		} else if (type.equals(SourceType.FILTER)) {
			final ImportFilterTask firstTask = new ImportFilterTask(datasourceName, client.getRestClient());
			final SetFilterTask setFilterTask = new SetFilterTask(firstTask);
			taskManager.execute(new TaskIterator(firstTask, setFilterTask));
		}
	}

	private String getIDFilterString(final String keyAttrName) {
		// TODO fix tunables
		// final Tunable tunable =
		// WebServiceClientManager.getClient("biomart").getProps().get("selected_only");
		// tunable.updateValue();
		// final Object value = tunable.getValue();

		// if (value != null && Boolean.parseBoolean(value.toString())) {
		// // Selected nodes only
		// nodes = new
		// ArrayList<Node>(Cytoscape.getCurrentNetwork().getSelectedNodes());
		// } else {
		// // Send all nodes in current network
		// nodes = Cytoscape.getCurrentNetwork().nodesList();
		// }

		final CyNetwork curNetwork = appManager.getCurrentNetwork();
		final List<CyNode> nodes = curNetwork.getNodeList();

		final StringBuilder builder = new StringBuilder();

		final CyTable defTable = curNetwork.getDefaultNodeTable();

		final CyColumn column = defTable.getColumn(keyAttrName);
		final Class<?> attrDataType = column.getType();
		for (CyNode node : nodes) {
			final CyRow row = defTable.getRow(node.getSUID());

			Object value = row.get(keyAttrName, attrDataType);
			if (value instanceof List) {
				List<?> values = (List<?>) value;
				for (Object val : values) {
					builder.append(val.toString());
					builder.append(",");
				}
			} else {
				builder.append(value.toString());
				builder.append(",");
			}
		}

		String filterStr = builder.toString();
		filterStr = filterStr.substring(0, filterStr.length() - 1);

		return filterStr;
	}

	@Override
	protected void importAttributes() {
		final BiomartQuery query = getTableImportQuery();
		
		if (query != null) {
			TaskIterator ti = client.createTaskIterator(query);
			ti.append(new ResetAttributesTask());
			taskManager.execute(ti);
		}
	}

	public BiomartQuery getTableImportQuery() {
		if (datasourceMap == null)
			return null;
		
		final String datasource = datasourceMap.get(databaseComboBox.getSelectedItem());
		final Map<String, String> attrMap = this.attrNameMap.get(datasource);
		final Map<String, String> fMap = filterMap.get(datasource);

		final String keyAttrName = columnNameComboBox.getSelectedItem().toString();
		Dataset dataset;
		Attribute[] attrs;
		Filter[] filters;

		// Name of the data source
		dataset = new Dataset(datasource);
		// System.out.println("Target Dataset = " + dataset.getName());

		final Object[] selectedAttr = attrCheckboxList.getSelectedValues();
		attrs = new Attribute[selectedAttr.length + 1];

		// This is the mapping key
		String filterName = fMap.get(attributeTypeComboBox.getSelectedItem());
		String dbName = this.databaseComboBox.getSelectedItem().toString();
		// System.out.println("Filter Name = " + filterName);

		// Database-specific modification.
		// This is not the best way, but cannot provide universal solution.

		// FIXME
		// if (dbName.contains("REACTOME")) {
		// attrs[0] = new Attribute(stub.toAttributeName("REACTOME",
		// filterName));
		// } else
		if (dbName.contains("VARIATION")) {
			// String newName = filterName.replace("_id", "_stable_id");
			// newName = newName.replace("_ensembl", "");
			attrs[0] = new Attribute(filterName + "_stable_id");
		} else {
			attrs[0] = new Attribute(filterName);
		}

		for (int i = 1; i <= selectedAttr.length; i++) {
			attrs[i] = new Attribute(attrMap.get(selectedAttr[i - 1]));
		}

		// For name mapping, just use ID list filter for query.
		filters = new Filter[1];

		filters[0] = new Filter(filterName, getIDFilterString(keyAttrName));

		String keyInHeader = null;

		for (String key : attrMap.keySet()) {
			if (attrMap.get(key).equals(filterName)) {
				keyInHeader = key.split("\\t")[1];
				// System.out.println("Key Attr = " + keyInHeader);
			}
		}

		// Create query
		return importAttributesFromService(dataset, attrs, filters, keyInHeader);
	}

	private BiomartQuery importAttributesFromService(Dataset dataset, Attribute[] attrs, Filter[] filters,
			String keyInHeader) {

		final String query = XMLQueryBuilder.getQueryString(dataset, attrs, filters);

		final String tableName = "BioMart Annotation: " + (++globalTableCounter);
		return new BiomartQuery(query, keyInHeader, tableName);
	}

	// //////// Local tasks
	private final class SetAttributeTask extends AbstractTask {

		private final ImportAttributeListTask firstTask;

		public SetAttributeTask(ImportAttributeListTask firstTask) {
			this.firstTask = firstTask;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			final List<String> order = new ArrayList<String>();
			final String selectedDB = databaseComboBox.getSelectedItem().toString();
			final String selectedDBName = datasourceMap.get(selectedDB);

			final Map<String, String[]> attributeVals = firstTask.getAttributeValues();

			final Map<String, String> names = new HashMap<String, String>();
			attributeMap.put(selectedDBName, attributeVals);

			String[] entry;
			String dispNameWithCategory;

			for (String attr : attributeVals.keySet()) {
				entry = attributeVals.get(attr);

				if ((entry != null) && (entry[0] != null)) {
					if ((entry.length > 2) && (entry[2] != null))
						dispNameWithCategory = entry[2] + ": \t" + entry[0] + "\t  (" + attr + ")";
					else
						dispNameWithCategory = " \t" + entry[0] + "\t  (" + attr + ")";

					names.put(dispNameWithCategory, attr);
					order.add(dispNameWithCategory);
				}
			}

			attrNameMap.put(selectedDBName, names);
			Collections.sort(order);

			attrCheckboxListModel.removeAllElements();
			for (String attrName : order)
				attrCheckboxListModel.addElement(attrName);

			attributeListOrder.put(selectedDBName, order);

			attrCheckboxList.setModel(attrCheckboxListModel);
			availableAttrScrollPane.setViewportView(attrCheckboxList);
			availableAttrScrollPane.repaint();
		}

	}

	private final class SetFilterTask extends AbstractTask {

		private final ImportFilterTask firstTask;

		public SetFilterTask(final ImportFilterTask firstTask) {
			this.firstTask = firstTask;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {

			Map<String, String> returnValMap;
			final String selectedDB = databaseComboBox.getSelectedItem().toString();
			final String selectedDBName = datasourceMap.get(selectedDB);

			returnValMap = firstTask.getFilters();
			filterMap.put(selectedDBName, returnValMap);

			List<String> filterNames = new ArrayList<String>(returnValMap.keySet());
			Collections.sort(filterNames);

			for (String filter : filterNames)
				attributeTypeComboBox.addItem(filter);
		}

	}
	
	private final class ResetAttributesTask extends AbstractTask {
		
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			    	updateAttributeList();
			    }
			});
		}
	}

	@Override
	protected void refreshButtonActionPerformed(ActionEvent evt) {
		initPanel();
	}
}
