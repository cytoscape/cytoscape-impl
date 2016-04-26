package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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


import java.awt.BorderLayout;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;
import javax.swing.RootPaneContainer;

import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
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
	private final IconManager iconManager;
	
	private ViewTaskFactoryListener vtfListener;
	
	private DingGraphLOD dingGraphLOD;
	private final VisualMappingManager vmm;
	
	private final CyNetworkViewManager netViewMgr; 
	private final HandleFactory handleFactory; 

	public DingRenderingEngineFactory(CyTableFactory dataTableFactory,
			CyRootNetworkManager rootNetworkManager, UndoSupport undo,
			SpacialIndex2DFactory spacialFactory, VisualLexicon dingLexicon,
			DialogTaskManager dialogTaskManager, 
			CyServiceRegistrar registrar,
			CyNetworkTableManager tableMgr,
			CyEventHelper eventHelper,
			IconManager iconManager,
			ViewTaskFactoryListener vtfListener,
			AnnotationFactoryManager annMgr, DingGraphLOD dingGraphLOD, final VisualMappingManager vmm,
			final CyNetworkViewManager netViewMgr, final HandleFactory handleFactory) {
		
		this.dataTableFactory = dataTableFactory;
		this.rootNetworkManager = rootNetworkManager;
		this.spacialFactory = spacialFactory;
		this.undo = undo;
		this.dingLexicon = dingLexicon;
		this.dialogTaskManager = dialogTaskManager;
		this.registrar = registrar;
		this.tableMgr = tableMgr;
		this.eventHelper = eventHelper;
		this.iconManager = iconManager;
		this.annMgr = annMgr;
		this.vmm=vmm;
		this.handleFactory = handleFactory;

		this.netViewMgr = netViewMgr;
		
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
		
		if (presentationContainer instanceof JComponent || presentationContainer instanceof RootPaneContainer) {
			if (view instanceof DGraphView) {
				dgv = (DGraphView) view;				
			} else {
				dgv = new DGraphView(targetView,
					rootNetworkManager, undo, spacialFactory, dingLexicon,
					vtfListener,dialogTaskManager, eventHelper, annMgr, dingGraphLOD, vmm, netViewMgr, handleFactory, iconManager, registrar);
				dgv.registerServices();
			}
			
			vtfListener.viewMap.put(targetView, new WeakReference<>(dgv));

			if (presentationContainer instanceof RootPaneContainer) {
				final RootPaneContainer container = (RootPaneContainer) presentationContainer;
				final InternalFrameComponent ifComp = new InternalFrameComponent(container.getLayeredPane(), dgv);
				container.setContentPane(ifComp);
			} else {
				final JComponent component = (JComponent) presentationContainer;
				component.setLayout(new BorderLayout());
				component.add(dgv.getComponent(), BorderLayout.CENTER);
			}
		} else {
			throw new IllegalArgumentException(
					"frame object is not of type JComponent or RootPaneContainer, which is invalid for this implementation of PresentationFactory");
		}

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
