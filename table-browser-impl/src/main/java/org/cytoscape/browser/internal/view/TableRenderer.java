package org.cytoscape.browser.internal.view;

import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.CELL_TEXT_WRAPPED;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_FORMAT;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_GRAVITY;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_VISIBLE;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_VIEW_MODE;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.table.CellFormat;
import org.cytoscape.view.presentation.property.table.TableMode;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * This class just holds on to the RenderingEngine and its associated JComponenet
 * and makes it easier to pass them around.
 */
public class TableRenderer implements CyDisposable {

	private RenderingEngine<CyTable> renderingEngine;
	private JComponent component;
	
	public TableRenderer(RenderingEngine<CyTable> renderingEngine, JComponent component) {
		this.renderingEngine = renderingEngine;
		this.component = component;
	}

	public RenderingEngine<CyTable> getRenderingEngine() {
		return renderingEngine;
	}

	public JComponent getComponent() {
		return component;
	}
	
	public CyTableView getTableView() {
		return renderingEngine != null ? (CyTableView) renderingEngine.getViewModel() : null;
	}

	public CyTable getDataTable() {
		return renderingEngine.getViewModel().getModel();
	}

	public void setColumnVisible(String name, boolean visible) {
		var colView = getTableView().getColumnView(name);
		setColumnVisible(colView, visible);
	}
	
	public static void setColumnVisible(View<CyColumn> colView, boolean visible) {
		// Must use a Bypass because it will get saved to the session.
		if (colView != null)
			colView.setLockedValue(COLUMN_VISIBLE, visible);
	}
	
	public boolean isColumnVisible(View<CyColumn> colView) {
		return colView != null ? colView.getVisualProperty(COLUMN_VISIBLE) : COLUMN_VISIBLE.getDefault();
	}
	
	public void setColumnGravity(String name, double grav) {
		// Must use a Bypass because it will get saved to the session.
		var colView = getTableView().getColumnView(name);
		
		if (colView != null)
			colView.setLockedValue(COLUMN_GRAVITY, grav);
	}
	
	public List<View<CyColumn>> getColumnViewsSortedByGravity() {
		var sortedColViews = new ArrayList<>(getTableView().getColumnViews());
		sortedColViews.sort((cv1, cv2) -> {
			double grav1 = cv1.getVisualProperty(COLUMN_GRAVITY);
			double grav2 = cv2.getVisualProperty(COLUMN_GRAVITY);
			
			return Double.compare(grav1, grav2);
		});

		return sortedColViews;
	}
	
	public TableMode getTableMode() {
		return getTableView().getVisualProperty(TABLE_VIEW_MODE);
	}
	
	public void setTableMode(TableMode mode) {
		getTableView().setVisualProperty(TABLE_VIEW_MODE, mode);
	}
	
	public static TableMode getDefaultTableMode() {
		return TABLE_VIEW_MODE.getDefault();
	}
	
	public String getColumnFormat(String name) {
		var colView = getTableView().getColumnView(name);
		var f = colView.getVisualProperty(COLUMN_FORMAT);
		
		if (f == null)
			return null;
		
		var format = f.getFormat();
		
		if (format == null || format.isBlank())
			return null;
		
		return format;
	}
	
	public void setColumnFormat(String name, String format) {
		var colView = getTableView().getColumnView(name);
		
		if (colView != null)
			colView.setLockedValue(COLUMN_FORMAT, new CellFormat(format));
	}
	
	public void setTextWrap(String name, boolean wrap) {
		var colView = getTableView().getColumnView(name);
		
		if (colView != null)
			colView.setLockedValue(CELL_TEXT_WRAPPED, wrap);
	}
	
	public boolean isTextWrap(String name) {
		var colView = getTableView().getColumnView(name);
		
		return colView != null ? colView.getVisualProperty(CELL_TEXT_WRAPPED) : CELL_TEXT_WRAPPED.getDefault();
	}
	
	@Override
	public void dispose() {
		try {
			renderingEngine.dispose();
		} finally {
			component = null;
			renderingEngine = null;
		}
	}
}
