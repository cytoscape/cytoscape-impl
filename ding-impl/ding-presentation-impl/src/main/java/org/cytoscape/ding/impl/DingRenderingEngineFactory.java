package org.cytoscape.ding.impl;


import java.awt.BorderLayout;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

import org.cytoscape.di.util.DIUtil;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
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

import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactoryManager;


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
	private final AnnotationFactoryManager annMgr;
	
	private TaskManager tm;
	private final CyNetworkTableManager tableMgr;
	private final CyEventHelper eventHelper;
	
	private ViewTaskFactoryListener vtfListener;
	
	
	public DingRenderingEngineFactory(CyTableFactory dataTableFactory,
			CyRootNetworkFactory rootNetworkFactory, UndoSupport undo,
			SpacialIndex2DFactory spacialFactory, VisualLexicon dingLexicon,
			TaskManager tm, CyServiceRegistrar registrar,
			CyNetworkTableManager tableMgr,
			CyEventHelper eventHelper,
			RenderingEngineManager renderingEngineManager,
			ViewTaskFactoryListener vtfListener,
			AnnotationFactoryManager annMgr) {
		
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
		this.annMgr = annMgr;

		this.vtfListener = vtfListener;
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
					vtfListener.nodeViewTFs, vtfListener.edgeViewTFs, vtfListener.emptySpaceTFs, vtfListener.dropNodeViewTFs,
					vtfListener.dropEmptySpaceTFs, tm, eventHelper, tableMgr, annMgr);

			logger.info("DGraphView created as a presentation for view model: "
					+ targetView.getSUID());
			vtfListener.viewMap.put(targetView, dgv);

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
		DGraphView gv = vtfListener.viewMap.get(nvce.getSource());
		logger.debug("NetworkViewChangedEvent listener got view update request: "
				+ nvce.getSource().getSUID());
		if (gv != null)
			gv.updateView();
	}

	public DGraphView getGraphView(CyNetworkView cnv) {
		return vtfListener.viewMap.get(cnv);
	}
	
	
	@Override
	public VisualLexicon getVisualLexicon() {
		return dingLexicon;
	}	
}
