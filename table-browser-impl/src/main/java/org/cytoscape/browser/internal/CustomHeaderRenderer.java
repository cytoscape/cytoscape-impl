/*
 Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.browser.internal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.cytoscape.browser.internal.util.SortArrowIcon;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;

final class CustomHeaderRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = 4656466166588715282L;

	private static final int FONT_SIZE = 12;

	private static final Font COLUMN_FONT = new Font("SansSerif", Font.PLAIN, FONT_SIZE);
	private static final Font COLUMN_SUID_FONT = new Font("SansSerif", Font.BOLD, FONT_SIZE);

	private static final Color COLUMN_COLOR = new Color(0xa0, 0xa0, 0xa0, 60);
	private static final Color COLUMN_VIRTUAL_COLOR = new Color(0x00, 0x9A, 0xCD, 70);
	private static final Color COLUMN_TITLE_COLOR = new Color(0x10, 0x10, 0x10);

	private static final Color BORDER_COLOR = new Color(0x10, 0x10, 0x10, 120);
	private static final Border BORDER_INSIDE = new EmptyBorder(4, 3, 4, 3);
	private static final Border BORDER_OUTSIDE = new MatteBorder(0, 0, 1, 1, BORDER_COLOR);
	private static final Border BORDER = new CompoundBorder(BORDER_OUTSIDE, BORDER_INSIDE);

	private static final ImageIcon STRING_ICON = new ImageIcon(CustomHeaderRenderer.class.getClassLoader().getResource(
			"images/datatype_string_16.png"));
	private static final ImageIcon INTEGER_ICON = new ImageIcon(CustomHeaderRenderer.class.getClassLoader()
			.getResource("images/datatype_int_16.png"));
	private static final ImageIcon DOUBLE_ICON = new ImageIcon(CustomHeaderRenderer.class.getClassLoader().getResource(
			"images/datatype_float_double_16.png"));
	private static final ImageIcon BOOLEAN_ICON = new ImageIcon(CustomHeaderRenderer.class.getClassLoader()
			.getResource("images/datatype_boolean2_16.png"));
	private static final ImageIcon LIST_ICON = new ImageIcon(CustomHeaderRenderer.class.getClassLoader().getResource(
			"images/datatype_list_16.png"));
	
	private static final ImageIcon PRIMARY_KEY_ICON = new ImageIcon(CustomHeaderRenderer.class.getClassLoader().getResource(
			"images/primary_key.png"));

	/**
	 * 
	 */
	public static Icon NONSORTED = new SortArrowIcon(SortArrowIcon.NONE);

	/**
	 * 
	 */
	public static Icon ASCENDING = new SortArrowIcon(SortArrowIcon.ASCENDING);

	/**
	 * 
	 */
	public static Icon DECENDING = new SortArrowIcon(SortArrowIcon.DECENDING);

	CustomHeaderRenderer() {
		setBorder(BORDER);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setIconTextGap(7);
	}

	// This method is called each time a column header
	// using this renderer needs to be rendered.
	public Component getTableCellRendererComponent(final JTable table, final Object value, boolean isSelected,
			boolean hasFocus, int rowIndex, int vColIndex) {

		this.setOpaque(true);

		// 'value' is column header value of column 'vColIndex'
		// rowIndex is always -1
		// isSelected is always false
		// hasFocus is always false

		// Configure the component with the specified value
		final String text = value.toString();
		setText(text);

		if (!(table.getModel() instanceof BrowserTableModel))
			return this;

		final BrowserTableModel model = (BrowserTableModel) table.getModel();
		final CyColumn col = model.getAttributes().getColumn(value.toString());
		if (col == null)
			return this;

		
		
		// Set datatype icon if available
		this.setIcon(getIcon(col.getType(), col));

		String toolTip = null;

		if(text.equals(CyIdentifiable.SUID))
			toolTip = "<html>Session-Unique ID (Primary Key).<br /> This column is uneditable.";
		else if(col.getType() == List.class) {
			toolTip = "<html>Name: " + col.getName() + "<br /> Type: List of "+ getMinimizedType( col.getListElementType().getName()) + "s";
		} else{
			toolTip = "<html>Name: " + col.getName()+ "<br /> Type: " + getMinimizedType(col.getType().getName());
		}
		if (col.getVirtualColumnInfo().isVirtual()) {
			setForeground(COLUMN_TITLE_COLOR);
			setBackground(COLUMN_VIRTUAL_COLOR);
			toolTip += "<br />Shared Attribute</html>";
		} else {
			setForeground(COLUMN_TITLE_COLOR);
			setBackground(COLUMN_COLOR);
			toolTip +="</html>";
		}

		if (text.equals(CyIdentifiable.SUID))
			this.setFont(COLUMN_SUID_FONT);
		else
			this.setFont(COLUMN_FONT);

		// Set tool tip if desired
		setToolTipText(toolTip);

		//*****sorting icon**
		
		int index = -1;
		boolean ascending = true;
		
		RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
		int modelColumn = table.convertColumnIndexToModel(vColIndex);
		List<? extends SortKey> sortKeys = rowSorter.getSortKeys();
		if (sortKeys.size() > 0) {
			SortKey key = sortKeys.get(0);
			if (key.getColumn() == modelColumn) {
				index = vColIndex;
				ascending = key.getSortOrder() == SortOrder.ASCENDING;
			}
		}
		
		Icon icon = ascending ? ASCENDING : DECENDING;
		setIcon((vColIndex == index) ? icon : NONSORTED);

		
		// Since the renderer is a component, return itself
		return this;
	}
	
	private String getMinimizedType (String type){
		return type.substring(type.lastIndexOf('.')+1);
	}
	
	private Icon getIcon(final Class<?> dataType, final CyColumn col) {
		
		if(col.isPrimaryKey())
			return PRIMARY_KEY_ICON;
	/*	
		if(dataType == String.class)
			return STRING_ICON;
		else if(dataType == Double.class || dataType == Float.class)
			return DOUBLE_ICON;
		else if(dataType == Boolean.class)
			return BOOLEAN_ICON;
		else if(dataType == List.class)
			return LIST_ICON;
		else if (dataType == Integer.class || dataType == Long.class)
			return INTEGER_ICON;
		*/
		return null;
	}

	//
	// The following methods override the defaults for performance reasons
	//
	public void validate() {
	}

	public void revalidate() {
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	}

	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
	}
}
