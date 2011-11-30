/*
 File: NewEmptyNetworkTask.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.task.internal.creation;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.session.CyNetworkNaming;


/**
 * Create an empty network with view.
 *
 */
public class NewEmptyNetworkTask extends AbstractTask {

	private final CyNetworkFactory cnf;
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkNaming namingUtil; 
	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;
	private boolean cancel = false;

	private CyNetworkView view; 

	public NewEmptyNetworkTask(CyNetworkFactory cnf, CyNetworkViewFactory cnvf, CyNetworkManager netmgr,
				   final CyNetworkViewManager networkViewManager, final CyNetworkNaming namingUtil) {
		this.networkManager = netmgr;
		this.networkViewManager = networkViewManager;
		this.cnf = cnf;
		this.cnvf = cnvf;
		this.namingUtil = namingUtil;
	}

	public void run(TaskMonitor tm) {
		tm.setProgress(0.0);
		final CyNetwork newNet = cnf.createNetwork();
		tm.setProgress(0.2);
		newNet.getCyRow().set(CyTableEntry.NAME, namingUtil.getSuggestedNetworkTitle("Network"));
		tm.setProgress(0.4);
		view = cnvf.createNetworkView(newNet);		
		tm.setProgress(0.6);
		networkManager.addNetwork(newNet);
		tm.setProgress(0.8);
		networkViewManager.addNetworkView(view);
		tm.setProgress(1.0);
	}

	@Override
	public void cancel() {
		cancel = true;
	}

	public CyNetworkView getView() {
		return view;
	}
}
