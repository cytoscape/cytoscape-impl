package org.cytoscape.tableimport.internal.ui;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.BOOLEAN_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.FLOAT_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.INT_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.LIST_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.STRING_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.PRIMARY_KEY;

import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.cytoscape.tableimport.internal.ui.theme.IconManager;
import org.cytoscape.tableimport.internal.util.AttributeTypes;


/**
 * Cell renderer for alias table in Import Dialog.<br>
 *
 * @author kono
 */
class AliasTableRenderer extends DefaultTableCellRenderer {
	
	private static final long serialVersionUID = -7334784380791509959L;
	
	private List<Byte> dataTypes;
	private int primaryKey;
	private final IconManager iconManager;

	private final Font DEFAULT_FONT;

	public AliasTableRenderer(final List<Byte> dataTypes, final int primaryKey, final IconManager iconManager) {
		this.dataTypes = dataTypes;
		this.primaryKey = primaryKey;
		this.iconManager = iconManager;
		
		DEFAULT_FONT = getFont();
	}

	public void setDataTypes(List<Byte> dataTypes) {
		this.dataTypes = dataTypes;
	}

	public void setPrimaryKey(int newKey) {
		this.primaryKey = newKey;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
	                                               boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		setIcon(null);
		setText("");
		setHorizontalAlignment(SwingConstants.LEFT);
		setFont(DEFAULT_FONT);
		
		if (column == 1) {
			setHorizontalAlignment(SwingConstants.CENTER);
			setFont(iconManager.getIconFont(14.0f));
			
			if (primaryKey == row)
				setText(PRIMARY_KEY.getText());
		} else if (column == 2) {
			setText(value == null ? "" : value.toString());
		} else if (column == 3) {
			final Byte dataType = dataTypes.get(row);
			
			if (dataType == AttributeTypes.TYPE_STRING) {
				setIcon(STRING_ICON.getIcon());
				setText("String");
			} else if (dataType == AttributeTypes.TYPE_INTEGER) {
				setIcon(INT_ICON.getIcon());
				setText("Integer");
			} else if (dataType == AttributeTypes.TYPE_FLOATING) {
				setIcon(FLOAT_ICON.getIcon());
				setText("Float");
			} else if (dataType == AttributeTypes.TYPE_BOOLEAN) {
				setIcon(BOOLEAN_ICON.getIcon());
				setText("Boolean");
			} else if (dataType == AttributeTypes.TYPE_SIMPLE_LIST) {
				setIcon(LIST_ICON.getIcon());
				setText("List");
			}
		}
		
		return this;
	}
}
