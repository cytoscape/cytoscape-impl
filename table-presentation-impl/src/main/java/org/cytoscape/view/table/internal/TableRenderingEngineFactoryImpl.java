package org.cytoscape.view.table.internal;

import javax.swing.JComponent;

import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.table.internal.impl.PopupMenuHelper;

public class TableRenderingEngineFactoryImpl implements RenderingEngineFactory<CyTable> {

	private final VisualLexicon visualLexicon;
	private final CyServiceRegistrar registrar;
	private final PopupMenuHelper popupMenuHelper;
	
	public TableRenderingEngineFactoryImpl(CyServiceRegistrar registrar, PopupMenuHelper popupMenuHelper, VisualLexicon visualLexicon) {
		this.registrar = registrar;
		this.visualLexicon = visualLexicon;
		this.popupMenuHelper = popupMenuHelper;
	}
	
	@Override
	public RenderingEngine<CyTable> createRenderingEngine(Object visualizationContainer, View<CyTable> viewModel) {
		var engine = new TableRenderingEngineImpl((CyTableView) viewModel, visualLexicon, popupMenuHelper, registrar);
		engine.install((JComponent) visualizationContainer);
		
		return engine;
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return visualLexicon;
	}
}
