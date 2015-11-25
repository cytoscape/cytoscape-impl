package org.cytoscape.filter.internal.work;

import javax.swing.JProgressBar;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.filter.internal.view.AbstractPanel;
import org.cytoscape.filter.internal.view.AbstractPanelController;
import org.cytoscape.filter.model.TransformerListener;

public abstract class AbstractWorker<V extends AbstractPanel<?,?>, C extends AbstractPanelController<?,?>> implements LazyWorker, TransformerListener {
	
	public static final int PROGRESS_BAR_MAXIMUM = Integer.MAX_VALUE;
	
	protected CyApplicationManager applicationManager;
	protected LazyWorkQueue queue;
	
	private ProgressMonitor currentMonitor;
	
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
		if(currentMonitor != null) {
			currentMonitor.cancel();
		}
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
	
	@Override
	public synchronized final void doWork() {
		currentMonitor = new ProgressMonitor() {
			private boolean cancelled = false;
			
			@Override
			public void cancel() {
				cancelled = true;
			}

			@Override
			public boolean isCancelled() {
				return cancelled;
			}

			@Override
			public void setProgress(double progress) {
				JProgressBar progressBar = view.getProgressBar();
				
				if(progress < 0.0) {
					progressBar.setIndeterminate(true);
					view.getCancelApplyButton().setEnabled(true);
				}
				else {
					if(progressBar.isIndeterminate()) {
						progressBar.setIndeterminate(false);
					}
					boolean done = progress == 1.0;
					view.getApplyButton().setEnabled(done);
					view.getCancelApplyButton().setEnabled(!done);
					progressBar.setValue(done ? 0 : (int)(progress * PROGRESS_BAR_MAXIMUM));
				}
			}

			@Override
			public void setStatusMessage(String message) {
				view.setStatus(message);
			}
		};
		
		doWork(currentMonitor);
	}
	
	abstract protected void doWork(ProgressMonitor monitor);
}
