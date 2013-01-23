package org.cytoscape.view.vizmap.gui.internal;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

public final class VizMapPropertySheetTableColumnModelListener implements
		TableColumnModelListener {

	private VizMapPropertySheetBuilder vizMapPropertySheetBuilder;

	public VizMapPropertySheetTableColumnModelListener(VizMapPropertySheetBuilder vizMapPropertySheetBuilder) {
		this.vizMapPropertySheetBuilder = vizMapPropertySheetBuilder;
	}

	public void columnAdded(TableColumnModelEvent e) {
		vizMapPropertySheetBuilder.updateTableView();
	}

	public void columnMarginChanged(ChangeEvent e) {
		vizMapPropertySheetBuilder.updateTableView();
	}

	public void columnMoved(TableColumnModelEvent e) {
	}

	public void columnRemoved(TableColumnModelEvent e) {
	}

	public void columnSelectionChanged(ListSelectionEvent e) {
	}
}