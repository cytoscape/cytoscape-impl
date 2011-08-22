package org.cytoscape.io.webservice.biomart.task;


import java.awt.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.webservice.biomart.BiomartQuery;
import org.cytoscape.io.webservice.biomart.rest.BiomartRestClient;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.MapNetworkAttrTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


/**
 * Task to import actual data tables from BioMart service.
 * 
 */
public class ImportTableTask extends AbstractTask {
	private final BiomartRestClient client;
	private final BiomartQuery query;

	private final CyTableFactory tableFactory;

	private Set<CyTable> tables;

	private final CyNetworkManager networkManager;
	private final CyApplicationManager applicationManager;
	private final Window parent;
	private final CyTableManager tableManager;

	public ImportTableTask(final BiomartRestClient client, final BiomartQuery query,
			       final CyTableFactory tableFactory,
			       final CyNetworkManager networkManager,
			       final CyApplicationManager applicationManager, final Window parent,
			       final CyTableManager tableManager)
	{
		this.client             = client;
		this.query              = query;
		this.tableFactory       = tableFactory;
		this.parent             = parent;
		this.networkManager     = networkManager;
		this.applicationManager = applicationManager;
		this.tableManager       = tableManager;

		this.tables = new HashSet<CyTable>();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (query == null)
			throw new NullPointerException("Query is null");

		taskMonitor.setProgress(0.0);
		taskMonitor.setTitle("Loading data table from BioMart...");
		taskMonitor.setStatusMessage("Loading data...");

		final BufferedReader result = client.sendQuery(query.getQueryString());

		if (result.ready() == false)
			throw new IOException("Could not get result.");

		
		taskMonitor.setStatusMessage("Creating global table...");
		final CyTable newTable = createGlobalTable(result,
				query.getKeyColumnName());

		tables.add(newTable);

		final MapNetworkAttrTask localMappingTask = new MapNetworkAttrTask(
				CyNode.class, newTable, networkManager, applicationManager);
		final ShowResultTask messageTask = new ShowResultTask();
		this.insertTasksAfterCurrentTask(localMappingTask, messageTask);
	}

	private CyTable createGlobalTable(BufferedReader reader, String key) throws IOException {
		// Read result from reader
		String line = reader.readLine();
		//System.out.println("Table Header: " + line);
		final String[] columnNames = line.split("\\t");

		if (columnNames[0].contains("Query ERROR"))
			throw new IOException("BioMart service returns error: \n" + line);

		final CyTable globalTable = tableFactory.createTable(query.getTableName(), key, String.class, true, true);

		// For status report
		int recordCount = 0;
		List<String> report = new ArrayList<String>();

		// final int rowCount = result.size();
		final int colSize = columnNames.length;
		int keyIdx = 0;

		// Search column index of the key
		for (int i = 0; i < colSize; i++) {
			globalTable.createColumn(columnNames[i], String.class, false);

			if (columnNames[i].equals(key))
				keyIdx = i;
		}

		String[] row;
		String val;

		List<List<Object>> listOfValList;
		List<String> ids = null;

		List<Object> testList;
		String keyVal = null;
		int rowLength = 0;

		int hitCount = 0;

		long start = System.currentTimeMillis();
		while ((line = reader.readLine()) != null) {

			row = line.split("\\t");

			// Ignore invalid length entry.
			if ((row.length <= keyIdx) || (row.length == 0))
				continue;

			recordCount++;
			keyVal = row[keyIdx];

			rowLength = row.length;

			final CyRow cyRow = globalTable.getRow(keyVal);
			for (int j = 0; j < rowLength; j++) {
				val = row[j];

				if ((val != null) && (val.length() != 0)) {

					if (j == keyIdx) {
						cyRow.set(key, val);
					}
					cyRow.set(columnNames[j], val);
					// if (keyAttrName.equals("ID")) {
					// testList = attr
					// .getListAttribute(keyVal, columnNames[j]);
					//
					// if (testList != null)
					// listOfValList.add(testList);
					// } else {
					// ids = getIdFromAttrValue(attrDataType, keyAttrName,
					// keyVal, nodeIdList, attr);
					//
					// if (ids.size() == 0)
					// continue;
					//
					// for (String id : ids)
					// listOfValList.add(attr.getListAttribute(id,
					// columnNames[j]));
					// }
					//
					// if (listOfValList.size() == 0) {
					// List<Object> valList = new ArrayList<Object>();
					// listOfValList.add(valList);
					// }
					//
					// int index = 0;
					// for (List<Object> valList : listOfValList) {
					// if (valList == null)
					// valList = new ArrayList<Object>();
					//
					// if (valList.contains(row[j]) == false)
					// valList.add(row[j]);
					//
					// if (keyAttrName.equals("ID")) {
					// attr.setListAttribute(keyVal, columnNames[j],
					// valList);
					// attr.setAttribute(keyVal, columnNames[j] + "-TOP",
					// valList.get(0).toString());
					// } else {
					// attr.setListAttribute(ids.get(index),
					// columnNames[j], valList);
					// attr.setAttribute(ids.get(index), columnNames[j]
					// + "-TOP", valList.get(0).toString());
					//
					// }
					// hitCount++;
					// index++;
					// }
				}
			}
		}

		reader.close();
		reader = null;

		// Dump table
		// final List<CyRow> rows = globalTable.getAllRows();
		// for(CyRow r: rows) {
		// Map<String, Object> rowVals = r.getAllValues();
		// for(String k :rowVals.keySet()) {
		// System.out.print(k + ":" + rowVals.get(k) + " ");
		// }
		// System.out.println();
		// }

		tableManager.addTable(globalTable);

		return globalTable;
	}

	public Set<CyTable> getCyTables() {
		return tables;
	}

	
	// Show result.
	private final class ShowResultTask extends AbstractTask {

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			final CyTable table = tables.iterator().next();
			if (table != null) {
				JOptionPane.showMessageDialog(parent,
						"New table loaded.\n" + table.getTitle() + " contains "
								+ table.getRowCount() + " rows.",
						"Table Loaded from BioMart",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}

	}

}
