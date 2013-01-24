package org.cytoscape.task.internal.select;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
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
			throw new NullPointerException("You must specify a non-null file to load.");

		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();
		
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
