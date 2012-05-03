package org.cytoscape.ding.impl;


import java.awt.BorderLayout;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactoryManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DingRenderingEngineFactory implements RenderingEngineFactory<CyNetwork> {
	
	private static final Logger logger = LoggerFactory.getLogger(DingRenderingEngineFactory.class);

	private final CyTableFactory dataTableFactory;
	private final CyRootNetworkManager rootNetworkManager;
	private final SpacialIndex2DFactory spacialFactory;
	private final UndoSupport undo;
	private final VisualLexicon dingLexicon;
	private final CyServiceRegistrar registrar;
	private final AnnotationFactoryManager annMgr;
	
	private DialogTaskManager dialogTaskManager;
	private final CyNetworkTableManager tableMgr;
	private final CyEventHelper eventHelper;
	
	private ViewTaskFactoryListener vtfListener;
	
	private DingGraphLOD dingGraphLOD;
	
	public DingRenderingEngineFactory(CyTableFactory dataTableFactory,
			CyRootNetworkManager rootNetworkManager, UndoSupport undo,
			SpacialIndex2DFactory spacialFactory, VisualLexicon dingLexicon,
			DialogTaskManager dialogTaskManager, 
			CyServiceRegistrar registrar,
			CyNetworkTableManager tableMgr,
			CyEventHelper eventHelper,
			ViewTaskFactoryListener vtfListener,
			AnnotationFactoryManager annMgr, DingGraphLOD dingGraphLOD) {
		
		this.dataTableFactory = dataTableFactory;
		this.rootNetworkManager = rootNetworkManager;
		this.spacialFactory = spacialFactory;
		this.undo = undo;
		this.dingLexicon = dingLexicon;
		this.dialogTaskManager = dialogTaskManager;
		this.registrar = registrar;
		this.tableMgr = tableMgr;
		this.eventHelper = eventHelper;
		this.annMgr = annMgr;

		this.vtfListener = vtfListener;
		
		this.dingGraphLOD = dingGraphLOD;
	}

	/**
	 * Render given view model by Ding rendering engine.
	 * 
	 */
	@Override
	public RenderingEngine<CyNetwork> createRenderingEngine(
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
					rootNetworkManager, undo, spacialFactory, dingLexicon,
					vtfListener,dialogTaskManager, eventHelper, tableMgr, annMgr, dingGraphLOD);

			logger.info("DGraphView created as a presentation for view model: "
					+ targetView.getSUID());
			vtfListener.viewMap.put(targetView, new WeakReference<DGraphView>(dgv));

			if (presentationContainer instanceof JInternalFrame) {
				final JInternalFrame inFrame = (JInternalFrame) presentationContainer;
				final InternalFrameComponent ifComp = new InternalFrameComponent(inFrame.getLayeredPane(), dgv);
				inFrame.getContentPane().add(ifComp);
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
		
		return dgv;
	}

//	/**
//	 * This method simply redraw the canvas, NOT updating the view model. To
//	 * apply and draw the new view model, you need to call this after apply.
//	 * 
//	 */
//	@Override
//	public void handleEvent(UpdateNetworkPresentationEvent nvce) {
//		DGraphView gv = vtfListener.viewMap.get(nvce.getSource());
//		logger.debug("NetworkViewChangedEvent listener got view update request: "
//				+ nvce.getSource().getSUID());
//		if (gv != null)
//			gv.updateView();
//	}

	public DGraphView getGraphView(CyNetworkView cnv) {
		Reference<DGraphView> reference = vtfListener.viewMap.get(cnv);
		if (reference == null) {
			return null;
		}
		return reference.get();
	}
	
	
	@Override
	public VisualLexicon getVisualLexicon() {
		return dingLexicon;
	}	
}
