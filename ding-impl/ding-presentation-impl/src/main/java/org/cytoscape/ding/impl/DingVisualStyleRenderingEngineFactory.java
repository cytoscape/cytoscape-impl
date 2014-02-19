package org.cytoscape.ding.impl;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseListener;

import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.UndoSupport;

public class DingVisualStyleRenderingEngineFactory extends DingRenderingEngineFactory {
	public DingVisualStyleRenderingEngineFactory(
			CyTableFactory dataTableFactory,
			CyRootNetworkManager rootNetworkManager, UndoSupport undo,
			SpacialIndex2DFactory spacialFactory, VisualLexicon dingLexicon,
			DialogTaskManager dialogTaskManager, CyServiceRegistrar registrar,
			CyNetworkTableManager tableMgr, CyEventHelper eventHelper,
			ViewTaskFactoryListener vtfListener,
			AnnotationFactoryManager annMgr, DingGraphLOD dingGraphLOD,
			VisualMappingManager vmm, CyNetworkViewManager netViewMgr,
			HandleFactory handleFactory) {
		super(dataTableFactory, rootNetworkManager, undo, spacialFactory, dingLexicon,
				dialogTaskManager, registrar, tableMgr, eventHelper, vtfListener,
				annMgr, dingGraphLOD, vmm, netViewMgr, handleFactory);
	}

	@Override
	public RenderingEngine<CyNetwork> createRenderingEngine(Object presentationContainer, View<CyNetwork> view) {
		RenderingEngine<CyNetwork> engine = super.createRenderingEngine(presentationContainer, view);
		
		Container component = (Container) presentationContainer;
		// Remove unnecessary mouse listeners.
		final int compCount = component.getComponentCount();
		for (int i = 0; i < compCount; i++) {
			final Component comp = component.getComponent(i);
			final MouseListener[] listeners = comp.getMouseListeners();
			for (MouseListener ml : listeners)
				comp.removeMouseListener(ml);
		}
		
		return engine;
	}
}
