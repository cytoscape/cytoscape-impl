/*
 Copyright (c) 2006, 2007, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.tableimport.internal.ui;


import java.io.IOException;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.tableimport.internal.reader.AttributeMappingParameters;
import org.cytoscape.tableimport.internal.reader.TextTableReader;
import org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.task.MapNetworkAttrTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


public class ImportAttributeTableTask extends AbstractTask implements CyTableReader {
	protected CyNetworkView[] cyNetworkViews;
	protected VisualStyle[] visualstyles;

	private final TextTableReader reader;
	private CyTable[] cyTables;
	private static int numImports = 0;
	private final CyTableManager tableManager;

	/**
	 * Creates a new ImportNetworkTask object.
	 */
	public ImportAttributeTableTask(final TextTableReader reader,
					final CyTableManager tableManager)
	{
		this.reader       = reader;
		this.tableManager = tableManager;
	}

	@Override
	public void run(TaskMonitor tm) throws IOException {
		final Class<? extends CyTableEntry> type = getMappingClass();

		String primaryKey = reader.getMappingParameter().getAttributeNames()[reader.getMappingParameter().getKeyIndex()];
		String mappingKey = reader.getMappingParameter().getMappingAttribute();

		final CyTable table =
			CytoscapeServices.tableFactory.createTable("AttrTable "
			                                           + Integer.toString(numImports++),
			                                           primaryKey, String.class, true,
			                                           true);
		cyTables = new CyTable[] { table };

		if (CytoscapeServices.netMgr.getNetworkSet().size() > 0 && type != null) {
			/* Case 1: use node ID as the key. */
			if (reader.getMappingParameter().getMappingAttribute().equals(AttributeMappingParameters.ID)) {
					final MapNetworkAttrTask task = new MapNetworkAttrTask(type, table,
							CytoscapeServices.netMgr, CytoscapeServices.appMgr);
					insertTasksAfterCurrentTask(task);
			} else { /* Case 2: use an attribute as the key. */
				final MapNetworkAttrTask task = new MapNetworkAttrTask(type, table, mappingKey,
						CytoscapeServices.netMgr, CytoscapeServices.appMgr);
				insertTasksAfterCurrentTask(task);
			}
		}

		this.reader.readTable(table);
		tableManager.addTable(table);
	}

	private Class<? extends CyTableEntry> getMappingClass() {
		ObjectType type = reader.getMappingParameter().getObjectType();

		if (type == ObjectType.NODE) {
			return CyNode.class;
		} else if (type == ObjectType.EDGE) {
			return CyEdge.class;
		} else if (type == ObjectType.NETWORK) {
			return CyNetwork.class;
		}

		return null;
	}

	@Override
	public CyTable[] getCyTables() {
		return cyTables;
	}
}
