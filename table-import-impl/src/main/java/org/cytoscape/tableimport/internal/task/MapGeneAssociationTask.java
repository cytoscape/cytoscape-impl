package org.cytoscape.tableimport.internal.task;

import java.util.List;
import java.util.Set;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationReader;
import org.cytoscape.tableimport.internal.reader.ontology.OBOReader;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map global annotation table to local tables. This does not create copy, just
 * add shared columns.
 * 
 */
public class MapGeneAssociationTask extends AbstractTask {

	private static final String MAPPING_KEY = "mapping key";

	private static final Logger logger = LoggerFactory.getLogger(MapGeneAssociationTask.class);

	private final CyNetworkManager networkManager;
	private final CyTableReader tableReader;
	private final CyTableManager tableManager;

	MapGeneAssociationTask(final CyTableReader tableReader, final CyTableManager tableManager,
			final CyNetworkManager networkManager) {
		this.tableReader = tableReader;
		this.tableManager = tableManager;
		this.networkManager = networkManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Mapping Global Gene Association Table to Local Network Tabels");
		taskMonitor.setStatusMessage("Mapping global colums to local...");

		final CyTable[] tables = tableReader.getTables();

		if (tables == null || tables[0] == null)
			throw new NullPointerException("Could not find table.");

		tableManager.addTable(tables[0]);

		mapping(taskMonitor, tables[0]);
		taskMonitor.setProgress(1.0d);
	}

	private void mapping(TaskMonitor taskMonitor, final CyTable globalTable) {
		taskMonitor.setProgress(0.0d);

		final Set<CyNetwork> networks = networkManager.getNetworkSet();
		if (networks.isEmpty())
			return;

		int networkCount = 1;
		final double increment = 1.0d / networks.size();
		double progress = 0d;
		// Map to all networks.
		for (final CyNetwork network : networks) {
			taskMonitor.setStatusMessage("Mapping networks " + networkCount + "/" + networks.size());

			final CyTable networkTable = network.getDefaultNetworkTable();
			final Boolean isDag = networkTable.getRow(network.getSUID()).get(OBOReader.DAG_ATTR, Boolean.class);
			if (isDag == null || isDag == false) {
				buildMapping(network, network.getDefaultNodeTable(), globalTable);
			}
			networkCount++;
			progress += increment;
			taskMonitor.setProgress(progress);
		}
	}

	private void buildMapping(final CyNetwork network, final CyTable nodeTable, final CyTable globalTable) {
		final List<String> globalKeys = globalTable.getColumn(CyNetwork.NAME).getValues(String.class);
		
		if(nodeTable.getColumn(MAPPING_KEY) == null)
			nodeTable.createColumn(MAPPING_KEY, String.class, true);

		for (final CyNode node : network.getNodeList()) {
			final String nodeName = network.getRow(node).get(CyNetwork.NAME, String.class);
			for (final String key : globalKeys) {
				final CyRow curRow = globalTable.getRow(key);

				if (curRow.getList(GeneAssociationReader.SYNONYM_COL_NAME, String.class).contains(nodeName)) {
					final CyRow nodeTableRow = nodeTable.getRow(node.getSUID());
					nodeTableRow.set(MAPPING_KEY, key);
					break;
				}
			}
		}

		nodeTable.addVirtualColumns(globalTable, MAPPING_KEY, true);
	}
}
