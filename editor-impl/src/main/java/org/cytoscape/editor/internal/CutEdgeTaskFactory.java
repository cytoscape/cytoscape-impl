package org.cytoscape.editor.internal;

import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.AbstractEdgeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class CutEdgeTaskFactory extends AbstractEdgeViewTaskFactory {
	final CyNetworkManager netMgr;
	final ClipboardManagerImpl clipMgr;
	final UndoSupport undoSupport;

	public CutEdgeTaskFactory(final ClipboardManagerImpl clipboardMgr, final UndoSupport undoSupport,
	                          final CyNetworkManager netMgr) {
		this.netMgr = netMgr;
		this.clipMgr = clipboardMgr;
		this.undoSupport = undoSupport;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyEdge> edgeView, CyNetworkView networkView) {
		return new TaskIterator(new CutTask(networkView, edgeView, clipMgr, undoSupport));
	}
}
