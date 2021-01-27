package org.cytoscape.cg.internal.editor;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.cytoscape.cg.internal.util.IconUtil;
import org.cytoscape.cg.model.SVGCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

import com.kitfox.svg.app.beans.SVGIcon;
import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

@SuppressWarnings("serial")
public class CyCustomGraphicsCellRenderer extends DefaultCellRenderer {

	private final int WIDTH = 20;
	private final int HEIGHT = 20;
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}
		
		if (value instanceof CyCustomGraphics) {
			var cg = (CyCustomGraphics<?>) value;
			
			if (cg instanceof SVGCustomGraphics) {
				var url = ((SVGCustomGraphics) cg).getSourceURL();
				var icon = new SVGIcon();
				icon.setSvgResourcePath(url.getPath());
				setIcon(icon);
			} else {
				var img = cg.getRenderedImage();
				setIcon(img != null ? IconUtil.resizeIcon(new ImageIcon(img), WIDTH, HEIGHT) : null);
			}
			
			setText(cg.getDisplayName());
			setHorizontalTextPosition(SwingConstants.RIGHT);
			setVerticalTextPosition(SwingConstants.CENTER);
			
			setIconTextGap(10);
		} else {
			setIcon(null);
			setText(null);
		}
		
		return this;
	}
}
