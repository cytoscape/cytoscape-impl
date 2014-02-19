package org.cytoscape.filter.internal.view;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.filter.model.TransformerListener;

public abstract class AbstractWorker<V, C> implements LazyWorker, TransformerListener {
	protected CyApplicationManager applicationManager;
	protected LazyWorkQueue queue;
	protected volatile boolean isCancelled;
	
	protected V view;
	protected C controller;

	protected boolean isInteractive;
	
	public AbstractWorker(LazyWorkQueue queue, CyApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
		this.queue = queue;
	}

	@Override
	public void handleSettingsChanged() {
		if (!isInteractive) {
			return;
		}
		queue.assignWorker(this);
	}

	public void handleFilterStructureChanged() {
		if (!isInteractive) {
			return;
		}
		queue.assignWorker(this);
	}
	
	public void cancel() {
		isCancelled = true;
	}
	
	public void setInteractive(boolean isInteractive) {
		this.isInteractive = isInteractive;
	}
	
	public void requestWork() {
		queue.assignWorker(this);
	}
	
	public void setView(V view) {
		this.view = view;
	}
	
	public void setController(C controller) {
		this.controller = controller;
	}
}
