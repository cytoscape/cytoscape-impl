package org.cytoscape.filter.internal.work;

import javax.swing.JProgressBar;

import org.cytoscape.filter.internal.view.AbstractPanel;
import org.cytoscape.filter.internal.view.AbstractPanelController;
import org.cytoscape.filter.model.TransformerListener;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Filters 2 Impl (filter2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public abstract class AbstractWorker<V extends AbstractPanel<?,?>, C extends AbstractPanelController<?,?>> implements LazyWorker, TransformerListener {
	
	public static final int PROGRESS_BAR_MAXIMUM = Integer.MAX_VALUE;
	
	protected LazyWorkQueue queue;
	
	private ProgressMonitor currentMonitor;
	
	protected V view;
	protected C controller;

	protected boolean isInteractive;

	protected final CyServiceRegistrar serviceRegistrar;
	
	public AbstractWorker(final LazyWorkQueue queue, final CyServiceRegistrar serviceRegistrar) {
		this.queue = queue;
		this.serviceRegistrar = serviceRegistrar;
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
