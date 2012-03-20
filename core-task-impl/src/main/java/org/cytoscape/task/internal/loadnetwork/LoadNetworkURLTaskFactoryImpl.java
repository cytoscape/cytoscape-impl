/*

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
package org.cytoscape.task.internal.loadnetwork;


import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.creation.ImportNetworksTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;


/**
 * Task to load a new network.
 */
public class LoadNetworkURLTaskFactoryImpl extends AbstractTaskFactory implements ImportNetworksTaskFactory {

	private CyNetworkReaderManager mgr;
	private CyNetworkManager netmgr;
	private final CyNetworkViewManager networkViewManager;
	private Properties props;
	private StreamUtil streamUtil;

	private CyNetworkNaming cyNetworkNaming;
	
	private final SynchronousTaskManager<?> syncTaskManager;
	
	private LoadNetworkURLTask task;

	public LoadNetworkURLTaskFactoryImpl(CyNetworkReaderManager mgr,
					     CyNetworkManager netmgr,
					     final CyNetworkViewManager networkViewManager,
					     CyProperty<Properties> cyProps, CyNetworkNaming cyNetworkNaming,
					     StreamUtil streamUtil, final SynchronousTaskManager<?> syncTaskManager)
	{
		this.mgr = mgr;
		this.netmgr = netmgr;
		this.networkViewManager = networkViewManager;
		this.props = cyProps.getProperties();
		this.cyNetworkNaming = cyNetworkNaming;
		this.streamUtil = streamUtil;
		
		this.syncTaskManager = syncTaskManager;
	}

	public TaskIterator createTaskIterator() {
		task = new LoadNetworkURLTask(mgr, netmgr, networkViewManager, props, cyNetworkNaming, streamUtil);
		// Usually we need to create view, so expected number is 2.
		return new TaskIterator(2, task);
	}
	
	@Override
	public Set<CyNetwork> loadCyNetworks(final URL url) {
		
		final Map<String,Object> m = new HashMap<String,Object>();
		m.put("url", url);
		
		syncTaskManager.setExecutionContext(m);
		syncTaskManager.execute(createTaskIterator());

		final Set<CyNetwork> networks = new HashSet<CyNetwork>();
		for(CyNetwork network: task.getCyNetworks())
			networks.add(network);
		
		return networks;
	}
}
