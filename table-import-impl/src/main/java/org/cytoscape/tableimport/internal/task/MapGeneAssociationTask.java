package org.cytoscape.tableimport.internal.task;

import java.util.List;
import java.util.Set;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationReader;
import org.cytoscape.tableimport.internal.reader.ontology.OBOReader;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapGeneAssociationTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(MapGeneAssociationTask.class);

	final CyNetworkManager networkManager;
	private final CyTableReader tableReader;

	MapGeneAssociationTask(final CyTableReader tableReader, final CyNetworkManager networkManager) {
		this.tableReader = tableReader;
		this.networkManager = networkManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Mapping Global Gene Association Table to Local Network Tabels");
		taskMonitor.setStatusMessage("Mapping global colums to local...");
		
		final CyTable[] tables = tableReader.getCyTables();

		if (tables == null || tables[0] == null)
			throw new NullPointerException("Could not find table.");

		mapping(taskMonitor, tables[0]);
	}

	private void mapping(TaskMonitor taskMonitor, final CyTable globalTable) {

		taskMonitor.setProgress(0.0);
		logger.debug("Target Table = " + globalTable.getTitle());
		Set<CyNetwork> networks = networkManager.getNetworkSet();

		int networkCount = 1;
		
		for (CyNetwork network : networks) {
			taskMonitor.setStatusMessage("Mapping networks " + networkCount + "/" + networks.size());
			
			final CyTable networkTable = network.getDefaultNetworkTable();
			Boolean isDag = networkTable.getRow(network.getSUID()).get(OBOReader.DAG_ATTR, Boolean.class);
			if (isDag == null || isDag == false) {
				buildMapping(taskMonitor, "SUID " + network.getSUID(), network.getDefaultNodeTable(), globalTable);
			}
			networkCount ++;
		}
	}

	// TODO: parallelize this
	private void buildMapping(TaskMonitor taskMonitor, final String networkName, CyTable nodeTable, CyTable globalTable) {
		final List<String> nodeNames = nodeTable.getColumn(CyTableEntry.NAME).getValues(String.class);
		final List<String> globalKeys = globalTable.getColumn(CyTableEntry.NAME).getValues(String.class);

		// Create immutable key column for this mapping
		final String colName = "Mapping Key for " + networkName;
		globalTable.createColumn(colName, String.class, true);
		
		double nodeCount = nodeNames.size();
		double count = 0.0;
		double progress = 0.0;
		for (final String name : nodeNames) {
			for (final String key : globalKeys) {
				final CyRow curRow = globalTable.getRow(key);
				if (curRow.getList(GeneAssociationReader.SYNONYM_COL_NAME, String.class)
						.contains(name)) {
					curRow.set(colName, name);
					break;
				}
			}
			count++;
			progress = count/nodeCount;
			taskMonitor.setProgress(progress);
		}
		
		nodeTable.addVirtualColumns(globalTable, colName, CyTableEntry.NAME, false);
	}
}
