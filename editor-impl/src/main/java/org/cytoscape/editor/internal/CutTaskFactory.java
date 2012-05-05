package org.cytoscape.editor.internal;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class CutTaskFactory extends AbstractNetworkViewTaskFactory {
	final CyNetworkManager netMgr;
	final ClipboardManagerImpl clipMgr;

	public CutTaskFactory(final ClipboardManagerImpl clipboardMgr, final CyNetworkManager netMgr) {
		this.netMgr = netMgr;
		this.clipMgr = clipboardMgr;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new CutTask(networkView, clipMgr));
	}
}
