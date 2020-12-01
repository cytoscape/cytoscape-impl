package org.cytoscape.view.table.internal;

import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.table.CyTableViewFactory;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.table.internal.impl.PopupMenuHelper;

public class TableViewRendererImpl implements TableViewRenderer {

	// Note, there is a use of this ID string in RenderingEngineManagerImpl.
	public static final String ID = "org.cytoscape.view.table.renderer";
	public static final String DISPLAY_NAME = "Cytoscape Table Browser";
	
	private final CyServiceRegistrar registrar;
	private final CyTableViewFactory tableViewFactory;
	private final BasicTableVisualLexicon lexicon;
	private final PopupMenuHelper popupMenuHelper;
	
	public TableViewRendererImpl(CyServiceRegistrar registrar, CyTableViewFactory tableViewFactory, BasicTableVisualLexicon lexicon, PopupMenuHelper popupMenuHelper) {
		this.registrar = registrar;
		this.tableViewFactory = tableViewFactory;
		this.lexicon = lexicon;
		this.popupMenuHelper = popupMenuHelper;
	}

	@Override
	public RenderingEngineFactory<CyTable> getRenderingEngineFactory(String contextId) {
		if(TableViewRenderer.DEFAULT_CONTEXT.equals(contextId)) {
			return new TableRenderingEngineFactoryImpl(registrar, popupMenuHelper, lexicon);
		}
		return null;
	}

	@Override
	public CyTableViewFactory getTableViewFactory() {
		return tableViewFactory;
	}

	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public String toString() {
		return DISPLAY_NAME;
	}

}
