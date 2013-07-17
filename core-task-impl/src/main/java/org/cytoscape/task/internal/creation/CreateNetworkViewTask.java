package org.cytoscape.task.internal.creation;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkCollectionTask;
import org.cytoscape.task.internal.layout.ApplyPreferredLayoutTask;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.undo.UndoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateNetworkViewTask extends AbstractNetworkCollectionTask implements ObservableTask {

	private static final Logger logger = LoggerFactory.getLogger(CreateNetworkViewTask.class);

	private final UndoSupport undoSupport;
	private final CyNetworkViewManager netViewMgr;
	private final CyNetworkViewFactory viewFactory;
	private final CyLayoutAlgorithmManager layoutMgr;
	private final CyEventHelper eventHelper;
	private final VisualMappingManager vmm;
	private final RenderingEngineManager renderingEngineMgr;
	private final CyNetworkView sourceView;
	private	Collection<CyNetworkView> result;

	public CreateNetworkViewTask(final UndoSupport undoSupport, final Collection<CyNetwork> networks,
			final CyNetworkViewFactory viewFactory, final CyNetworkViewManager netViewMgr,
			final CyLayoutAlgorithmManager layoutMgr, final CyEventHelper eventHelper, final VisualMappingManager vmm,
			final RenderingEngineManager renderingEngineMgr) {
		this(undoSupport, networks, viewFactory, netViewMgr, layoutMgr, eventHelper, vmm, renderingEngineMgr, null);
	}

	public CreateNetworkViewTask(final UndoSupport undoSupport, final Collection<CyNetwork> networks,
			final CyNetworkViewFactory viewFactory, final CyNetworkViewManager netViewMgr,
			final CyLayoutAlgorithmManager layoutMgr, final CyEventHelper eventHelper, final VisualMappingManager vmm,
			final RenderingEngineManager renderingEngineMgr, final CyNetworkView sourceView) {
		super(networks);

		this.undoSupport = undoSupport;
		this.viewFactory = viewFactory;
		this.netViewMgr = netViewMgr;
		this.layoutMgr = layoutMgr;
		this.eventHelper = eventHelper;
		this.vmm = vmm;
		this.renderingEngineMgr = renderingEngineMgr;
		this.sourceView = sourceView;
		this.result = new ArrayList<CyNetworkView>();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		taskMonitor.setTitle("Creating Network View");
		taskMonitor.setStatusMessage("Creating network view...");

		final VisualStyle style = vmm.getCurrentVisualStyle();
		
		int i = 0;
		int viewCount = networks.size();
		for (final CyNetwork n : networks) {
			if (netViewMgr.getNetworkViews(n).isEmpty()) {
				result.add(createView(n, style, taskMonitor));
				taskMonitor.setStatusMessage("Network view successfully created for:  "
						+ n.getRow(n).get(CyNetwork.NAME, String.class));
				i++;
				taskMonitor.setProgress((i / (double) viewCount));
			}
		}

		taskMonitor.setProgress(1.0);
	}

	private final CyNetworkView createView(final CyNetwork network, final VisualStyle style, TaskMonitor tMonitor) throws Exception {
		final long start = System.currentTimeMillis();

		try {
			// By calling this task, actual view will be created even if it's a
			// large network.
			final CyNetworkView view = viewFactory.createNetworkView(network);
			netViewMgr.addNetworkView(view);

			// Apply visual style
			if (style != null) {
				vmm.setVisualStyle(style, view);
				style.apply(view);
			}

			// If a source view has been provided, use that to set the X/Y
			// positions of the
			// nodes along with the visual style.
			if (sourceView != null) {
				insertTasksAfterCurrentTask(new CopyExistingViewTask(vmm, renderingEngineMgr, view, sourceView, null,
						null, true));
			} else if (layoutMgr != null) {
				final Set<CyNetworkView> views = new HashSet<CyNetworkView>();
				views.add(view);
				insertTasksAfterCurrentTask(new ApplyPreferredLayoutTask(views, layoutMgr));
//				executeInParallel(view, style, new ApplyPreferredLayoutTask(views, layoutMgr), tMonitor);
			}
			return view;
		} catch (Exception e) {
			throw new Exception("Could not create network view for network: "
					+ network.getRow(network).get(CyNetwork.NAME, String.class), e);
		} finally {
			if (undoSupport != null)
				undoSupport.postEdit(new CreateNetworkViewEdit(eventHelper, network, viewFactory, netViewMgr));
	
			logger.info("Network view creation finished in " + (System.currentTimeMillis() - start) + " msec.");
		}
	}

	private final void executeInParallel(final CyNetworkView view, final VisualStyle style, final Task task, final TaskMonitor tMonitor) {
		final ExecutorService exe = Executors.newCachedThreadPool();
		final long startTime = System.currentTimeMillis();

		final ApplyVisualStyleTask applyTask = new ApplyVisualStyleTask(view, style);
		final LayoutTask layoutTask = new LayoutTask(task, tMonitor);
		exe.submit(applyTask);
		exe.submit(layoutTask);

		try {
			exe.shutdown();
			exe.awaitTermination(1000000, TimeUnit.SECONDS);

			long endTime = System.currentTimeMillis();
			double sec = (endTime - startTime) / (1000.0);
			logger.info("Create View Finished in " + sec + " sec.");
		} catch (Exception ex) {
			logger.warn("Create view operation timeout", ex);
		} finally {

		}
	}
	
	@Override
	public Object getResults(Class requestedType) {
		// Support Collection<CyNetwork> or String
		if (requestedType.equals(String.class)) {
			String strRes = "";
			for (CyNetworkView nv: result) {
				strRes += nv.toString()+"\n";
			}
			return strRes.substring(0, strRes.length()-1); // This strips the trailing tab
		} else
			return result;
	}

	private static final class ApplyVisualStyleTask implements Runnable {
		private final CyNetworkView view;
		private final VisualStyle style;
		
		ApplyVisualStyleTask(final CyNetworkView view, final VisualStyle style) {
			this.style = style;
			this.view = view;
		}

		@Override
		public void run() {
			style.apply(view);
		}
	}

	private static final class LayoutTask implements Runnable {

		private final Task task;
		private final TaskMonitor tMonitor;
		
		public LayoutTask(final Task task, final TaskMonitor tMonitor) {
			this.task = task;
			this.tMonitor = tMonitor;
		}
		@Override
		public void run() {
			try {
				task.run(tMonitor);
			} catch (Exception e) {
				throw new RuntimeException("Could not run task", e);
			}
		}
	}

}
