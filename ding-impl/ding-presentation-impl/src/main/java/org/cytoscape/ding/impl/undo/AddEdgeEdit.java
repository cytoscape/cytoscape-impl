package org.cytoscape.ding.impl.undo;

import java.util.Collections;

import org.cytoscape.ding.impl.AddEdgeTask;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

public class AddEdgeEdit extends AbstractCyEdit {

	private final CyServiceRegistrar registrar;
	private final CyNetworkViewSnapshot snapshot;
	private final View<CyNode> sourceNodeView;
	private final View<CyNode> targetNodeView;
	
	private CyEdge edge;
	

	public AddEdgeEdit(CyServiceRegistrar registrar, CyNetworkViewSnapshot snapshot, View<CyNode> sourceNodeView, View<CyNode> targetNodeView, View<CyEdge> edgeView) {
		super("Add Edge");
		this.registrar = registrar;
		this.snapshot = snapshot;
		this.sourceNodeView = sourceNodeView;
		this.targetNodeView = targetNodeView;
		this.edge = edgeView.getModel();
	}

	@Override
	public void undo() {
		CyNetwork network = snapshot.getMutableNetworkView().getModel();
		network.removeEdges(Collections.singleton(edge));
	}

	@Override
	public void redo() {
		AddEdgeTask addEdgeTask = new AddEdgeTask(registrar, snapshot, sourceNodeView, targetNodeView);
		addEdgeTask.setPostUndo(false);
		
		DialogTaskManager taskManager = registrar.getService(DialogTaskManager.class);
		taskManager.execute(new TaskIterator(addEdgeTask), new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
				if(task instanceof AddEdgeTask) {
					edge = ((AddEdgeTask)task).getResults(CyEdge.class);
				}
			}
			public void allFinished(FinishStatus finishStatus) { }
		});
	}

	public void post() {
		registrar.getService(UndoSupport.class).postEdit(this);
	}
}
