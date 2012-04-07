package org.cytoscape.ding.customgraphicsmgr.internal.ui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.cytoscape.ding.customgraphics.CyCustomGraphics;

public class CyCustomGraphicsTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -5155795609971962107L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (value instanceof CyCustomGraphics == false && value != null) {
			this.setText(value.toString());
			return this;
		}

		final CyCustomGraphics cg = (CyCustomGraphics) value;

		this.setText(cg.getDisplayName());
		this.setIcon(new ImageIcon(cg.getRenderedImage()));
		
		return this;
	}
}
