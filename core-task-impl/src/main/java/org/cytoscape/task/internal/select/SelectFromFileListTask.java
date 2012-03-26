/*
  File: SelectFromFileListTask.java

  Copyright (c) 2006, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.task.internal.select;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.UndoSupport;


public class SelectFromFileListTask extends AbstractSelectTask {
	private final UndoSupport undoSupport;

	@ProvidesTitle
	public String getTitle() {
		return "Select Nodes using ID File";
	}
	
	@Tunable(description = "Node selection file",params="input=true")
	public File file;

	public SelectFromFileListTask(final UndoSupport undoSupport, final CyNetwork net,
	                              final CyNetworkViewManager networkViewManager,
	                              final CyEventHelper eventHelper)
	{
		super(net, networkViewManager, eventHelper);
		this.undoSupport = undoSupport;
	}

	public void run(final TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		if (file == null)
			throw new NullPointerException("You must specify a non-null file to load!");

		final CyNetworkView view = networkViewManager.getNetworkView(network);
		final SelectionEdit edit =
			new SelectionEdit(eventHelper, "Select Nodes From File", network, view,
			                  SelectionEdit.SelectionFilter.NODES_ONLY);
		tm.setProgress(0.1);
		try {
			FileReader fin = new FileReader(file);
			BufferedReader bin = new BufferedReader(fin);
			Set<String> fileNodes = new HashSet<String>();
			String s;
			tm.setProgress(0.2);
			while ((s = bin.readLine()) != null) {
				final String trimName = s.trim();
				if (trimName.length() > 0)
					fileNodes.add(trimName);
			}
			fin.close();
			tm.setProgress(0.6);
			// Loop through all the node of the graph selecting those in the file:
			List<CyNode> nodeList = network.getNodeList();
			for (final CyNode node : nodeList) {
				if (fileNodes.contains(network.getRow(node).get(CyNetwork.NAME, String.class)))
					network.getRow(node).set(CyNetwork.SELECTED, true);
			}
			tm.setProgress(0.8);
			updateView();
		} catch (Exception e) {
			throw new Exception("Error reading file: " + file.getAbsolutePath(), e);
		}

		undoSupport.postEdit(edit);
		tm.setProgress(1.0);
	}
}
