package org.cytoscape.view.vizmap.gui.internal.cellrenderer;

import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JTable;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

public class FontTableCellRenderer extends DefaultCellRenderer {

	private static final long serialVersionUID = -667720462619223580L;

	@Override
	protected String convertToString(Object value) {

		if (value == null)
			return null;


		if (value instanceof Font) {
			final Font font = (Font) value;
			return font.getFontName();
		} else
			return "Unknown Font";
	}

	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		
		System.out.println("%%%%%%%%%%%% CELL Value= " + value);
		
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}

		if (value != null && value instanceof Font) {
			final Font font = (Font) value;
			final Font resizedFont = font.deriveFont(12);
			this.setFont(resizedFont);
			this.setText(convertToString(value));
		} else {
			this.setIcon(null);
			this.setText(null);
		}

		return this;
	}

}
