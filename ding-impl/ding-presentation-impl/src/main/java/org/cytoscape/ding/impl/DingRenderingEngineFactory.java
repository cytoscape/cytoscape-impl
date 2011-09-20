package org.cytoscape.ding.impl;


import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

import org.cytoscape.di.util.DIUtil;
import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.dnd.DropNodeViewTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEventListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DingRenderingEngineFactory implements
		RenderingEngineFactory<CyNetwork>,
		UpdateNetworkPresentationEventListener
{
	private static final Logger logger = LoggerFactory.getLogger(DingRenderingEngineFactory.class);

	private final RenderingEngineManager renderingEngineManager;
	private final CyTableFactory dataTableFactory;
	private final CyRootNetworkFactory rootNetworkFactory;
	private final SpacialIndex2DFactory spacialFactory;
	private final UndoSupport undo;
	private final VisualLexicon dingLexicon;
	private final CyServiceRegistrar registrar;
	
	private final Map<CyNetworkView, DGraphView> viewMap;

	private Map<NodeViewTaskFactory, Map> nodeViewTFs;
	private Map<EdgeViewTaskFactory, Map> edgeViewTFs;
	private Map<NetworkViewTaskFactory, Map> emptySpaceTFs;
	private Map<DropNodeViewTaskFactory, Map> dropNodeViewTFs;
	private Map<DropNetworkViewTaskFactory, Map> dropEmptySpaceTFs;

	private TaskManager tm;
	private final CyNetworkTableManager tableMgr;
	private final CyEventHelper eventHelper;
	
	
	public DingRenderingEngineFactory(CyTableFactory dataTableFactory,
			CyRootNetworkFactory rootNetworkFactory, UndoSupport undo,
			SpacialIndex2DFactory spacialFactory, VisualLexicon dingLexicon,
			TaskManager tm, CyServiceRegistrar registrar,
			CyNetworkTableManager tableMgr,
			CyEventHelper eventHelper,
			RenderingEngineManager renderingEngineManager) {
		
		this.dataTableFactory = DIUtil.stripProxy(dataTableFactory);
		this.rootNetworkFactory = DIUtil.stripProxy(rootNetworkFactory);
		this.spacialFactory = DIUtil.stripProxy(spacialFactory);
		this.undo = DIUtil.stripProxy(undo);
		this.dingLexicon = DIUtil.stripProxy(dingLexicon);
		this.tm = DIUtil.stripProxy(tm);
		this.registrar = DIUtil.stripProxy(registrar);
		this.tableMgr = DIUtil.stripProxy(tableMgr);
		this.eventHelper = DIUtil.stripProxy(eventHelper);
		this.renderingEngineManager = DIUtil.stripProxy(renderingEngineManager);

		viewMap = new HashMap<CyNetworkView, DGraphView>();
		nodeViewTFs = new HashMap<NodeViewTaskFactory, Map>();
		edgeViewTFs = new HashMap<EdgeViewTaskFactory, Map>();
		emptySpaceTFs = new HashMap<NetworkViewTaskFactory, Map>();
		dropNodeViewTFs = new HashMap<DropNodeViewTaskFactory, Map>();
		dropEmptySpaceTFs = new HashMap<DropNetworkViewTaskFactory, Map>();
	}

	/**
	 * Render given view model by Ding rendering engine.
	 * 
	 */
	@Override
	public RenderingEngine<CyNetwork> getInstance(
			final Object presentationContainer, final View<CyNetwork> view) {

		// Validate arguments
		if (presentationContainer == null)
			throw new IllegalArgumentException("Container is null.");

		if (view == null)
			throw new IllegalArgumentException(
					"Cannot create presentation for null view model.");

		if (view instanceof CyNetworkView == false)
			throw new IllegalArgumentException(
					"Ding accepts CyNetworkView only.");

		final CyNetworkView targetView = (CyNetworkView) view;

		DGraphView dgv = null;
		if (presentationContainer instanceof JComponent) {

			logger.debug("Start rendering presentation by Ding: "
					+ targetView.getSUID());

			
			if(view instanceof DGraphView) {
				dgv = (DGraphView) view;
				logger.info("%%%%%%% This view is DGV.");
			}
			else
				dgv = new DGraphView(targetView, dataTableFactory,
					rootNetworkFactory, undo, spacialFactory, dingLexicon,
					nodeViewTFs, edgeViewTFs, emptySpaceTFs, dropNodeViewTFs,
					dropEmptySpaceTFs, tm, eventHelper, tableMgr);

			logger.info("DGraphView created as a presentation for view model: "
					+ targetView.getSUID());
			viewMap.put(targetView, dgv);

			if (presentationContainer instanceof JInternalFrame) {
				final JInternalFrame inFrame = (JInternalFrame) presentationContainer;
				
				final InternalFrameComponent internalFrameComp = 
					new InternalFrameComponent(inFrame.getLayeredPane(), dgv);
				inFrame.getContentPane().add(internalFrameComp);

//				// TODO - not sure this layered pane bit is optimal
//				inFrame.setContentPane(dgv.getContainer(inFrame
//						.getLayeredPane()));
//				// dgv.addTransferComponent(desktopPane);
			} else {
				final JComponent component = (JComponent) presentationContainer;
				component.setLayout(new BorderLayout());
				component.add(dgv.getComponent(), BorderLayout.CENTER);
			}

		} else {
			throw new IllegalArgumentException(
					"frame object is not of type JInternalFrame, which is invalid for this implementation of PresentationFactory");
		}

		registrar.registerAllServices(dgv, new Properties());
		final AddDeleteHandler addDeleteHandler = new AddDeleteHandler(dgv);
		registrar.registerAllServices(addDeleteHandler, new Properties());

		// Register engine to manager
		this.renderingEngineManager.addRenderingEngine(dgv);

		return dgv;
	}

	/**
	 * This method simply redraw the canvas, NOT updating the view model. To
	 * apply and draw the new view model, you need to call this after apply.
	 * 
	 */
	@Override
	public void handleEvent(UpdateNetworkPresentationEvent nvce) {
		DGraphView gv = viewMap.get(nvce.getSource());
		logger.debug("NetworkViewChangedEvent listener got view update request: "
				+ nvce.getSource().getSUID());
		if (gv != null)
			gv.updateView();
	}

	public DGraphView getGraphView(CyNetworkView cnv) {
		return viewMap.get(cnv);
	}

	public void addNodeViewTaskFactory(NodeViewTaskFactory nvtf, Map props) {
		if (nvtf == null)
			return;

		nodeViewTFs.put(nvtf, props);
	}

	public void removeNodeViewTaskFactory(NodeViewTaskFactory nvtf, Map props) {
		if (nvtf == null)
			return;

		nodeViewTFs.remove(nvtf);
	}

	public void addEdgeViewTaskFactory(EdgeViewTaskFactory evtf, Map props) {
		if (evtf == null)
			return;

		edgeViewTFs.put(evtf, props);
	}

	public void removeEdgeViewTaskFactory(EdgeViewTaskFactory evtf, Map props) {
		if (evtf == null)
			return;

		edgeViewTFs.remove(evtf);
	}

	public void addNetworkViewTaskFactory(NetworkViewTaskFactory evtf, Map props) {
		if (evtf == null)
			return;

		emptySpaceTFs.put(evtf, props);
	}

	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory evtf,
			Map props) {
		if (evtf == null)
			return;

		emptySpaceTFs.remove(evtf);
	}

	public void addDropNetworkViewTaskFactory(DropNetworkViewTaskFactory evtf,
			Map props) {
		if (evtf == null)
			return;

		dropEmptySpaceTFs.put(evtf, props);
	}

	public void removeDropNetworkViewTaskFactory(
			DropNetworkViewTaskFactory evtf, Map props) {
		if (evtf == null)
			return;

		dropEmptySpaceTFs.remove(evtf);
	}

	public void addDropNodeViewTaskFactory(DropNodeViewTaskFactory nvtf, Map props) {
		if (nvtf == null)
			return;

		dropNodeViewTFs.put(nvtf, props);
	}

	public void removeDropNodeViewTaskFactory(DropNodeViewTaskFactory nvtf, Map props) {
		if (nvtf == null)
			return;

		dropNodeViewTFs.remove(nvtf);
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return dingLexicon;
	}
}
