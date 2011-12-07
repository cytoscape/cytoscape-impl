package org.cytoscape.task.internal.welcome;

import java.net.URL;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class LoadMitabFileTaskFactory implements TaskFactory {

	private CyNetworkReaderManager mgr;
	private CyNetworkManager netmgr;
	private final CyNetworkViewManager networkViewManager;
	private Properties props;

	private CyNetworkNaming cyNetworkNaming;
	
	private URL url;
	private boolean apply = false;

	public LoadMitabFileTaskFactory(CyNetworkReaderManager mgr, CyNetworkManager netmgr,
			final CyNetworkViewManager networkViewManager, CyProperty<Properties> cyProp,
			CyNetworkNaming cyNetworkNaming) {
		this.mgr = mgr;
		this.netmgr = netmgr;
		this.networkViewManager = networkViewManager;
		this.props = cyProp.getProperties();
		this.cyNetworkNaming = cyNetworkNaming;
	}
	
	void setURL(final URL url) {
		this.url = url;
	}
	
	void setApplyLayout(boolean apply) {
		this.apply = apply;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new LoadMitabFileTask(apply, url, mgr, netmgr, networkViewManager, props, cyNetworkNaming));
	}

}
