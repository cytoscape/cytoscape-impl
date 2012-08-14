package org.cytoscape.editor.internal;

import java.util.List;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class CutNodeTaskFactory extends AbstractNodeViewTaskFactory {
	final CyNetworkManager netMgr;
	final ClipboardManagerImpl clipMgr;
	final UndoSupport undoSupport;

	public CutNodeTaskFactory(final ClipboardManagerImpl clipboardMgr, final UndoSupport undoSupport,
	                          final CyNetworkManager netMgr) {
		this.netMgr = netMgr;
		this.clipMgr = clipboardMgr;
		this.undoSupport = undoSupport;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(new CutTask(networkView, nodeView, clipMgr, undoSupport));
	}
}
