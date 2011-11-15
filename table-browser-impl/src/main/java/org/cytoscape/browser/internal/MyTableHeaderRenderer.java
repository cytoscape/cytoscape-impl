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
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableCellRenderer;
import org.cytoscape.model.CyColumn;


final class MyTableHeaderRenderer extends JLabel implements TableCellRenderer {
	
	private static Color defaultBackground = null;
	private static Color defaultForeground = null;
	
	MyTableHeaderRenderer() {
		setBorder(new BevelBorder(BevelBorder.RAISED));
	}

	// This method is called each time a column header
	// using this renderer needs to be rendered.
	public Component getTableCellRendererComponent(final JTable table, final Object value,
						       boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex)
	{
		if (defaultBackground == null)
			defaultBackground = getBackground();

		if (defaultForeground == null)
			defaultForeground = getForeground();
		
		// 'value' is column header value of column 'vColIndex'
		// rowIndex is always -1
		// isSelected is always false
		// hasFocus is always false

		// Configure the component with the specified value
		setText(value.toString());
		setForeground(defaultForeground);
		setBackground(defaultBackground);

		if (!(table.getModel() instanceof BrowserTableModel)) 
			return this;
		
		BrowserTableModel model = (BrowserTableModel)table.getModel();
		CyColumn col = model.getAttributes().getColumn(value.toString());
		if ( col == null ) 
			return this;
		
		String toolTip = col.getType().getName();
		if(col.getVirtualColumnInfo().isVirtual()){
			setForeground(defaultForeground);
			setBackground(Color.lightGray);
			setOpaque(true);
			toolTip = "<html>" + col.getType().getName()+ "<br />Virtual Column</html>";
		} 
		
		// Set tool tip if desired
        setToolTipText(toolTip);
		
		// Since the renderer is a component, return itself
		return this;
	}

	//
	// The following methods override the defaults for performance reasons
	//

	public void validate() { }

	public void revalidate() { }

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) { }

	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }
}
