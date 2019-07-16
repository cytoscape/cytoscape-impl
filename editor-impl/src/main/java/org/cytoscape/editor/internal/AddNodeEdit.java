package org.cytoscape.editor.internal;

import java.awt.geom.Point2D;
import java.util.Collections;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

public class AddNodeEdit extends AbstractCyEdit {

	private final CyNetworkView view;
	private final Point2D xformPt;
	private final String nodeName;
	private final CyServiceRegistrar registrar;
	
	private CyNode node;
	
	public AddNodeEdit(CyNetworkView view, CyNode node, Point2D xformPt, String nodeName, CyServiceRegistrar registrar) {
		super("Add Node");
		this.view = view;
		this.node = node;
		this.xformPt = xformPt;
		this.nodeName = nodeName;
		this.registrar = registrar;
	}

	@Override
	public void undo() {
		CyNetwork network = view.getModel();
		network.removeNodes(Collections.singleton(node));
	}

	@Override
	public void redo() {
		AddNodeTask addNodeTask = new AddNodeTask(view, xformPt, nodeName, registrar);
		addNodeTask.setPostUndo(false);
		
		DialogTaskManager taskManager = registrar.getService(DialogTaskManager.class);
		taskManager.execute(new TaskIterator(addNodeTask), new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
				if(task instanceof AddNodeTask) {
					node = ((AddNodeTask)task).getResults(CyNode.class);
				}
			}
			public void allFinished(FinishStatus finishStatus) { }
		});
	}

	public void post() {
		registrar.getService(UndoSupport.class).postEdit(this);
	}
}
