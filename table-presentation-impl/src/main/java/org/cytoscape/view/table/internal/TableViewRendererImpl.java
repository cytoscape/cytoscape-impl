package org.cytoscape.view.table.internal;

import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.table.CyTableViewFactory;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.table.internal.impl.PopupMenuHelper;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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
		if (TableViewRenderer.DEFAULT_CONTEXT.equals(contextId)) {
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
