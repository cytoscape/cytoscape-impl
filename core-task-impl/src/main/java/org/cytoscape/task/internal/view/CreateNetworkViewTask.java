package org.cytoscape.task.internal.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.command.StringToModel;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkCollectionTask;
import org.cytoscape.task.internal.layout.ApplyPreferredLayoutTask;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class CreateNetworkViewTask extends AbstractNetworkCollectionTask 
                                   implements ObservableTask  {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	private final UndoSupport undoSupport;
	private final CyNetworkViewManager netViewMgr;
	private CyNetworkViewFactory viewFactory;
	private final Set<NetworkViewRenderer> viewRenderers;
	private final CyLayoutAlgorithmManager layoutMgr;
	private final CyEventHelper eventHelper;
	private final VisualMappingManager vmMgr;
	private final RenderingEngineManager renderingEngineMgr;
	private final CyApplicationManager appMgr;
	private final CyServiceRegistrar serviceRegistrar;
	private final CyNetworkView sourceView;
	private	List<CyNetworkView> networkViews;

	@Tunable(
			description = "Network",
			longDescription = StringToModel.CY_NETWORK_LONG_DESCRIPTION,
			exampleStringValue = "SUID:52",
			context = "nogui"
	)
	public CyNetwork network;

	@Tunable(
			description = "Layout the resulting view?",
			longDescription = "If true (default), the preferred layout will be applied to the new view. If false, no layout will be applied.",
			exampleStringValue = "false",
			context = "nogui"
	)
	public boolean layout = true;

	public CreateNetworkViewTask(final UndoSupport undoSupport,
								 final Collection<CyNetwork> networks,
								 final CyNetworkViewManager netViewMgr,
								 final CyLayoutAlgorithmManager layoutMgr,
								 final CyEventHelper eventHelper,
								 final VisualMappingManager vmMgr,
								 final RenderingEngineManager renderingEngineMgr,
								 final CyApplicationManager appMgr,
								 final Set<NetworkViewRenderer> viewRenderers,
								 final CyServiceRegistrar serviceRegistrar) {
		this(undoSupport, networks, null, netViewMgr, layoutMgr, eventHelper, vmMgr, renderingEngineMgr, appMgr,
				serviceRegistrar);
		
		if (viewRenderers != null) {
			this.viewRenderers.addAll(viewRenderers);
			
			if (viewRenderers.size() == 1)
				viewFactory = viewRenderers.iterator().next().getNetworkViewFactory();
		}
	}

	public CreateNetworkViewTask(final UndoSupport undoSupport,
								 final Collection<CyNetwork> networks,
								 final CyNetworkViewFactory viewFactory,
								 final CyNetworkViewManager netViewMgr,
								 final CyLayoutAlgorithmManager layoutMgr,
								 final CyEventHelper eventHelper,
								 final VisualMappingManager vmMgr,
								 final RenderingEngineManager renderingEngineMgr,
								 final CyApplicationManager appMgr,
								 final CyNetworkView sourceView,
								 final CyServiceRegistrar serviceRegistrar) {
		super(networks);

		this.undoSupport = undoSupport;
		this.viewFactory = viewFactory;
		this.netViewMgr = netViewMgr;
		this.layoutMgr = layoutMgr;
		this.eventHelper = eventHelper;
		this.vmMgr = vmMgr;
		this.renderingEngineMgr = renderingEngineMgr;
		this.appMgr = appMgr;
		this.sourceView = sourceView;
		this.serviceRegistrar = serviceRegistrar;
		this.networkViews = new ArrayList<>();
		this.viewRenderers = new TreeSet<>(new Comparator<NetworkViewRenderer>() {
			@Override
			public int compare(NetworkViewRenderer r1, NetworkViewRenderer r2) {
				return r1.toString().compareToIgnoreCase(r2.toString());
			}
		});
	}

	public CreateNetworkViewTask(final UndoSupport undoSupport,
								 final Collection<CyNetwork> networks,
								 final CyNetworkViewFactory viewFactory,
								 final CyNetworkViewManager netViewMgr,
								 final CyLayoutAlgorithmManager layoutMgr,
								 final CyEventHelper eventHelper,
								 final VisualMappingManager vmMgr,
								 final RenderingEngineManager renderingEngineMgr,
								 final CyApplicationManager appMgr,
								 final CyServiceRegistrar serviceRegistrar) {
		this(undoSupport, networks, viewFactory, netViewMgr, layoutMgr, eventHelper, vmMgr, renderingEngineMgr, appMgr,
				null, serviceRegistrar);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		
		final Collection<CyNetwork> netList = network != null ? Collections.singletonList(network) : networks;
		final int total = netList.size();
		
		tm.setTitle("Creating Network View" + (total == 1 ? "" : "s"));
		tm.setStatusMessage("Creating " + total + " network view" + (total == 1 ? "" : "s") + "...");
		
		if (viewFactory == null && viewRenderers.size() > 1) {
			// Let the user choose the network view renderer first
			final ChooseViewRendererTask chooseRendererTask = new ChooseViewRendererTask(netList);
			insertTasksAfterCurrentTask(chooseRendererTask);
		} else {
			final CyNetwork curNet = appMgr.getCurrentNetwork();
			CyNetworkView curView = appMgr.getCurrentNetworkView();
			
			final VisualStyle style = vmMgr.getCurrentVisualStyle();
			int i = 0;
			int viewCount = netList.size();
			
			for (final CyNetwork n : netList) {
				// TODO Remove this check when multiple views per network is supported
				if (netViewMgr.viewExists(n))
					continue;
				
				final CyNetworkView view = createView(n, style, tm);
				networkViews.add(view);
				
				if (curView == null && n.equals(curNet))
					curView = view;
				
				tm.setStatusMessage("Network view successfully created for:  "
						+ n.getRow(n).get(CyNetwork.NAME, String.class));
				i++;
				tm.setProgress((i / (double) viewCount));
			}
			
			final List<CyNetwork> selectedNetworks = appMgr.getSelectedNetworks();
			final List<CyNetworkView> selectedViews = new ArrayList<>(appMgr.getSelectedNetworkViews());
			boolean setSelectedViews = false;
			
			for (CyNetworkView view : networkViews) {
				if (selectedNetworks.contains(view.getModel())) {
					selectedViews.add(view);
					setSelectedViews = true;
				}
			}
			
			if (curView == null && !selectedViews.isEmpty())
				curView = selectedViews.get(0);
			
			if (setSelectedViews)
				appMgr.setSelectedNetworkViews(selectedViews);
			
			if (curView != null)
				appMgr.setCurrentNetworkView(curView);
		}
		
		tm.setProgress(1.0);
	}

	private final CyNetworkView createView(final CyNetwork network, final VisualStyle style, TaskMonitor tm)
			throws Exception {
		final long start = System.currentTimeMillis();

		try {
			// By calling this task, actual view will be created even if it's a large network.
			final CyNetworkView view = viewFactory.createNetworkView(network);
			
			// Create a default title
			final Collection<CyNetworkView> netViews = netViewMgr.getNetworkViews(network);
			String title = network.getDefaultNetworkTable().getRow(network.getSUID()).get(CyNetwork.NAME, String.class);
			title += (netViews.isEmpty() ? "" : " (" + (netViews.size() + 1) + ")");
			
			view.setVisualProperty(BasicVisualLexicon.NETWORK_TITLE, title);
			
			netViewMgr.addNetworkView(view, false);

			// Apply visual style
			if (style != null) {
				vmMgr.setVisualStyle(style, view);
				style.apply(view);
			}

			// If a source view has been provided, use that to set the X/Y positions of the
			// nodes along with the visual style.
			if (sourceView != null) {
				insertTasksAfterCurrentTask(
						new CopyExistingViewTask(renderingEngineMgr, view, sourceView, style, null, null, true));
			} else if (layout == true) {
				final Set<CyNetworkView> views = new HashSet<>();
				views.add(view);
				insertTasksAfterCurrentTask(new ApplyPreferredLayoutTask(views, serviceRegistrar));
//				executeInParallel(view, style, new ApplyPreferredLayoutTask(views, layoutMgr), tMonitor);
			}
			
			return view;
		} catch (Exception e) {
			throw new Exception("Could not create network view for network: "
					+ (network != null ? network.getRow(network).get(CyNetwork.NAME, String.class) : "null"), e);
		} finally {
			if (undoSupport != null)
				undoSupport.postEdit(new CreateNetworkViewEdit(eventHelper, network, viewFactory, netViewMgr));
	
			logger.info("Network view creation finished in " + (System.currentTimeMillis() - start) + " msec.");
		}
	}

//	private final void executeInParallel(final CyNetworkView view, final VisualStyle style, final Task task, final TaskMonitor tMonitor) {
//		final ExecutorService exe = Executors.newCachedThreadPool();
//		final long startTime = System.currentTimeMillis();
//
//		final ApplyVisualStyleTask applyTask = new ApplyVisualStyleTask(view, style);
//		final LayoutTask layoutTask = new LayoutTask(task, tMonitor);
//		exe.submit(applyTask);
//		exe.submit(layoutTask);
//
//		try {
//			exe.shutdown();
//			exe.awaitTermination(1000000, TimeUnit.SECONDS);
//
//			long endTime = System.currentTimeMillis();
//			double sec = (endTime - startTime) / (1000.0);
//			logger.info("Create View Finished in " + sec + " sec.");
//		} catch (Exception ex) {
//			logger.warn("Create view operation timeout", ex);
//		} finally {
//
//		}
//	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getResults(Class type) {
		if (type == String.class) {
			String res = "";
			
			if (networkViews != null && !networkViews.isEmpty()) {
				res += "Created views:\n";
				
				for (CyNetworkView view : networkViews)
					res += DataUtils.getViewTitle(view) + " (SUID: " + view.getSUID() + ")" + "\n";
			
				res = res.substring(0, res.length() - 1); // This strips the trailing line break
			} else {
				res = "No views were created.";
			}
			
			return res;
		} else if (type == JSONResult.class) {
			String json = networkViews != null && !networkViews.isEmpty() ?
					serviceRegistrar.getService(CyJSONUtil.class).toJson(networkViews.get(0)) :
					null;
			
			JSONResult res = () -> { return json; };
			
			return res;
		}
		
		return new ArrayList<>(networkViews);
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class, JSONResult.class);
	}

//	private static final class ApplyVisualStyleTask implements Runnable {
//		private final CyNetworkView view;
//		private final VisualStyle style;
//		
//		ApplyVisualStyleTask(final CyNetworkView view, final VisualStyle style) {
//			this.style = style;
//			this.view = view;
//		}
//
//		@Override
//		public void run() {
//			style.apply(view);
//		}
//	}
//
//	private static final class LayoutTask implements Runnable {
//
//		private final Task task;
//		private final TaskMonitor tMonitor;
//		
//		public LayoutTask(final Task task, final TaskMonitor tMonitor) {
//			this.task = task;
//			this.tMonitor = tMonitor;
//		}
//		@Override
//		public void run() {
//			try {
//				task.run(tMonitor);
//			} catch (Exception e) {
//				throw new RuntimeException("Could not run task", e);
//			}
//		}
//	}
	
	public class ChooseViewRendererTask extends AbstractNetworkCollectionTask {

		@Tunable(description = "Network View Renderer:")
		public ListSingleSelection<NetworkViewRenderer> renderers;
		
		public ChooseViewRendererTask(final Collection<CyNetwork> networks) {
			super(networks);
			renderers = new ListSingleSelection<>(new ArrayList<>(viewRenderers));
			
			final NetworkViewRenderer defViewRenderer = appMgr.getDefaultNetworkViewRenderer();
			
			if (defViewRenderer != null && viewRenderers.contains(defViewRenderer))
				renderers.setSelectedValue(defViewRenderer);
		}

		@ProvidesTitle
		public String getTitle() {
			return "Choose a Network View Renderer";
		}
		
		@Override
		public void run(final TaskMonitor taskMonitor) throws Exception {
			// Try again, now with the selected view factory
			final CyNetworkViewFactory factory = renderers.getSelectedValue().getNetworkViewFactory();
			final CreateNetworkViewTask createViewTask = new CreateNetworkViewTask(undoSupport, networks, factory, 
					netViewMgr, layoutMgr, eventHelper, vmMgr, renderingEngineMgr, appMgr, null);
			
			if (!cancelled)
				insertTasksAfterCurrentTask(createViewTask);
		}
	}
}
