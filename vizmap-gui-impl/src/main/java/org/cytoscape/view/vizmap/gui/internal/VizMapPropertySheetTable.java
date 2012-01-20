package org.cytoscape.view.vizmap.gui.internal;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

public final class VizMapPropertySheetTable extends PropertySheetTable {

	private final static long serialVersionUID = 1213748836812161L;

	public String getToolTipText(MouseEvent me) {
		final Point pt = me.getPoint();
		final int row = rowAtPoint(pt);

		if (row < 0)
			return null;
		else {
			final Property prop = ((Item) getValueAt(row, 0)).getProperty();

			final Color fontColor;

			if ((prop != null) && (prop.getValue() != null) && (prop.getValue().getClass() == Color.class))
				fontColor = (Color) prop.getValue();
			else
				fontColor = Color.DARK_GRAY;

			final String colorString = Integer.toHexString(fontColor.getRGB());

			if (prop == null)
				return null;

			if (prop.getDisplayName().equals(AbstractVizMapperPanel.GRAPHICAL_MAP_VIEW))
				return "Click to edit this mapping...";

			if ((prop.getDisplayName() == "Controlling Attribute") || (prop.getDisplayName() == "Mapping Type"))
				return "<html><Body BgColor=\"white\"><font Size=\"4\" Color=\"#" + colorString.substring(2, 8)
						+ "\"><strong>" + prop.getDisplayName() + " = " + prop.getValue()
						+ "</font></strong></body></html>";
			else if ((prop.getSubProperties() == null) || (prop.getSubProperties().length == 0))
				return "<html><Body BgColor=\"white\"><font Size=\"4\" Color=\"#" + colorString.substring(2, 8)
						+ "\"><strong>" + prop.getDisplayName() + "</font></strong></body></html>";

			return null;
		}
	}
}