/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.webservice.biomart.ui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.webservice.biomart.BiomartClient;
import org.cytoscape.io.webservice.biomart.BiomartQuery;
import org.cytoscape.io.webservice.biomart.rest.Attribute;
import org.cytoscape.io.webservice.biomart.rest.Dataset;
import org.cytoscape.io.webservice.biomart.rest.Filter;
import org.cytoscape.io.webservice.biomart.rest.XMLQueryBuilder;
import org.cytoscape.io.webservice.biomart.task.BioMartTaskFactory;
import org.cytoscape.io.webservice.biomart.task.ImportAttributeListTask;
import org.cytoscape.io.webservice.biomart.task.ImportFilterTask;
import org.cytoscape.io.webservice.biomart.task.LoadRepositoryResult;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.GUITaskManager;

public class BiomartAttrMappingPanel extends AttributeImportPanel {

	private static final long serialVersionUID = 3574198525811249639L;

	private static final Icon LOGO = new ImageIcon(
			BiomartAttrMappingPanel.class
					.getResource("/images/logo_biomart2.png"));

	private Map<String, String> datasourceMap;

	private Map<String, Map<String, String[]>> attributeMap;
	private Map<String, List<String>> attributeListOrder;
	private Map<String, Map<String, String>> attrNameMap;
	private Map<String, Map<String, String>> filterMap;

	public enum SourceType {
		DATABASE, ATTRIBUTE, FILTER;
	}

	private boolean cancelFlag = false;
	private boolean initialized = false;

	// These databases are not compatible with this UI.
	private static final List<String> databaseFilter = new ArrayList<String>();

	private final GUITaskManager taskManager;
	private final CyApplicationManager appManager;

	private Window parent;

	private final BiomartClient client;

	private int globalTableCounter;

	/**
	 * Creates a new BiomartNameMappingPanel object.
	 * 
	 * @param logo
	 *            DOCUMENT ME!
	 * @param title
	 *            DOCUMENT ME!
	 * @throws Exception
	 */
	public BiomartAttrMappingPanel(final BiomartClient client,
			final TaskManager taskManager,
			final CyApplicationManager appManager,
			final CyTableManager tblManager, final CyNetworkManager netManager) {
		super(tblManager, netManager, LOGO, "Biomart", "Import Settings");

		this.client = client;
		this.taskManager = (GUITaskManager) taskManager;
		this.appManager = appManager;

		this.globalTableCounter = 0;
	}

	public void setParent(final Window parent) {
		this.parent = parent;
	}

	/**
	 * Access data sources and build GUI.
	 * 
	 * @throws Exception
	 */
	public void initDataSources(LoadRepositoryResult res) {
		attributeMap = new HashMap<String, Map<String, String[]>>();
		attributeListOrder = new HashMap<String, List<String>>();
		filterMap = new HashMap<String, Map<String, String>>();
		attrNameMap = new HashMap<String, Map<String, String>>();

		// Import list of repositories.
		setMartServiceList(res);

		// Load available filters for current source.
		// loadFilter();
	}

	public void setMartServiceList(LoadRepositoryResult res) {
		this.datasourceMap = res.getDatasourceMap();
		final List<String> dsList = res.getSortedDataSourceList();
		for (String ds : dsList)
			this.databaseComboBox.addItem(ds);
	}

	public void loadFilter() {
		attributeTypeComboBox.removeAllItems();

		final String selectedDB = databaseComboBox.getSelectedItem().toString();
		final String selectedDBName = datasourceMap.get(selectedDB);

		fetchData(selectedDBName, SourceType.FILTER);
	}

	protected void resetButtonActionPerformed(ActionEvent e) {
		updateAttributeList();
	}

	protected void databaseComboBoxActionPerformed(ActionEvent evt) {
		updateAttributeList();
		loadFilter();
	}

	private void updateAttributeList() {
		final String selectedDB = databaseComboBox.getSelectedItem().toString();
		final String selectedDBName = datasourceMap.get(selectedDB);

		List<String> order = attributeListOrder.get(selectedDBName);
		attrCheckboxListModel = new DefaultListModel();
		attrCheckboxList = new CheckBoxJList();

		if (order != null) {
			// List<String> sortedList = new
			// ArrayList<String>(singleAttrMap.keySet());
			// Collections.sort(sortedList);
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

		taskManager.setParent(parent);

		if (type.equals(SourceType.ATTRIBUTE)) {
			final ImportAttributeListTask firstTask = new ImportAttributeListTask(
					datasourceName, client.getRestClient());
			final SetAttributeTask setAttrTask = new SetAttributeTask(firstTask);
			final BioMartTaskFactory tf = new BioMartTaskFactory(firstTask);
			tf.getTaskIterator().insertTasksAfter(firstTask, setAttrTask);
			taskManager.execute(tf);
		} else if (type.equals(SourceType.FILTER)) {
			final ImportFilterTask firstTask = new ImportFilterTask(
					datasourceName, client.getRestClient());
			final SetFilterTask setFilterTask = new SetFilterTask(firstTask);
			final BioMartTaskFactory tf = new BioMartTaskFactory(firstTask);
			tf.getTaskIterator().insertTasksAfter(firstTask, setFilterTask);
			taskManager.execute(tf);
		}
	}

	protected void importButtonActionPerformed(ActionEvent evt) {
		importAttributes();
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

		// // If attribute name is ID, then use node id as the key.
		// if (keyAttrName.equals("ID")) {
		// for (Node n : nodes) {
		// builder.append(n.getIdentifier());
		// builder.append(",");
		// }
		// } else {
		// Use Attributes for mapping
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

		// if ((mapAttrs == null) || (mapAttrs.size() == 0))
		// return null;
		//
		// // List acceptedClasses =
		// Arrays.asList(mapping.getAcceptedDataClasses());
		// // Class mapAttrClass = CyAttributesUtils.getClass(attrName, attrs);
		// //
		// // if ((mapAttrClass == null) ||
		// !(acceptedClasses.contains(mapAttrClass)))
		// // return null;
		// for (String key : loadKeySet(mapAttrs)) {
		// builder.append(key);
		// builder.append(",");
		// }
		// }

		String filterStr = builder.toString();
		filterStr = filterStr.substring(0, filterStr.length() - 1);

		return filterStr;
	}

	// private static Set<String> loadKeySet(final Map mapAttrs) {
	// final Set<String> mappedKeys = new TreeSet<String>();
	//
	// final Iterator keyIter = mapAttrs.values().iterator();
	//
	// Object o = null;
	//
	// while (keyIter.hasNext()) {
	// o = keyIter.next();
	//
	// if (o instanceof List) {
	// List list = (List) o;
	//
	// for (int i = 0; i < list.size(); i++) {
	// Object vo = list.get(i);
	//
	// if (!mappedKeys.contains(vo))
	// mappedKeys.add(vo.toString());
	// }
	// } else {
	// if (!mappedKeys.contains(o))
	// mappedKeys.add(o.toString());
	// }
	// }
	//
	// return mappedKeys;
	// }

	@Override
	protected void importAttributes() {
		taskManager.setParent(parent);
		taskManager.execute(client);
	}

	public BiomartQuery getTableImportQuery() {
		final String datasource = datasourceMap.get(databaseComboBox
				.getSelectedItem());
		final Map<String, String> attrMap = this.attrNameMap.get(datasource);
		final Map<String, String> fMap = filterMap.get(datasource);

		final String keyAttrName = attributeComboBox.getSelectedItem()
				.toString();

		System.out.println("Selected attr name: " + keyAttrName);

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
		return importAttributesFromService(dataset, attrs, filters,
				keyInHeader, keyAttrName);
	}

	private BiomartQuery importAttributesFromService(Dataset dataset,
			Attribute[] attrs, Filter[] filters, String keyInHeader,
			String keyAttrName) {

		final String query = XMLQueryBuilder.getQueryString(dataset, attrs,
				filters);

		final String tableName = TABLE_PREFIX + (++globalTableCounter);
		return new BiomartQuery(query, keyAttrName, tableName);
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
			final String selectedDB = databaseComboBox.getSelectedItem()
					.toString();
			final String selectedDBName = datasourceMap.get(selectedDB);

			final Map<String, String[]> attributeVals = firstTask
					.getAttributeValues();

			final Map<String, String> names = new HashMap<String, String>();
			attributeMap.put(selectedDBName, attributeVals);

			String[] entry;
			String dispNameWithCategory;

			for (String attr : attributeVals.keySet()) {
				entry = attributeVals.get(attr);

				if ((entry != null) && (entry[0] != null)) {
					if ((entry.length > 2) && (entry[2] != null))
						dispNameWithCategory = entry[2] + ": \t" + entry[0]
								+ "\t  (" + attr + ")";
					else
						dispNameWithCategory = " \t" + entry[0] + "\t  ("
								+ attr + ")";

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
			final String selectedDB = databaseComboBox.getSelectedItem()
					.toString();
			final String selectedDBName = datasourceMap.get(selectedDB);

			returnValMap = firstTask.getFilters();
			filterMap.put(selectedDBName, returnValMap);

			List<String> filterNames = new ArrayList<String>(
					returnValMap.keySet());
			Collections.sort(filterNames);

			for (String filter : filterNames)
				attributeTypeComboBox.addItem(filter);
		}

	}

}
