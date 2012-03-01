package org.cytoscape.ding.impl.editor;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.cytoscape.ding.Bend;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.icon.VisualPropertyIconFactory;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

public class EdgeBendCellRenderer extends DefaultCellRenderer {

	private static final long serialVersionUID = -7044183741963477557L;
	
	private static final int ICON_WIDTH = 48;
	private static final int ICON_HEIGHT = 48;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		final JLabel label = new JLabel();

		if (isSelected) {
			label.setBackground(table.getSelectionBackground());
			label.setForeground(table.getSelectionForeground());
		} else {
			label.setBackground(table.getBackground());
			label.setForeground(table.getForeground());
		}

		if ((value != null) && value instanceof Bend) {

			final Bend bend = (Bend) value;
			final Icon icon = VisualPropertyIconFactory.createIcon(DVisualLexicon.EDGE_BEND, bend, ICON_WIDTH, ICON_HEIGHT);
			label.setIcon(icon);
			label.setVerticalAlignment(SwingConstants.CENTER);
			label.setHorizontalAlignment(SwingConstants.CENTER);
		}

		return label;
	}

}
