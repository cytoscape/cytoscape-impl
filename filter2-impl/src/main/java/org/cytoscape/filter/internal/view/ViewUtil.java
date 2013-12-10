package org.cytoscape.filter.internal.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.text.NumberFormatter;

public class ViewUtil {
	public static final int INTERNAL_VERTICAL_PADDING = 4;
	
	public static final Border COMPOSITE_PANEL_BORDER = BorderFactory.createCompoundBorder(
			new DashedBorder(Color.LIGHT_GRAY, 3),
			BorderFactory.createEmptyBorder(INTERNAL_VERTICAL_PADDING, 0, INTERNAL_VERTICAL_PADDING, 0));
	
	public static void configureFilterView(JComponent component) {
		component.setBackground(Color.WHITE);
		component.setBorder(COMPOSITE_PANEL_BORDER);
	}
	
	public static NumberFormatter createIntegerFormatter(int minimum, int maximum) {
		NumberFormat format = NumberFormat.getIntegerInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setMinimum(minimum);
		formatter.setMaximum(maximum);
		formatter.setValueClass(Integer.class);
		formatter.setCommitsOnValidEdit(true);
		return formatter;
	}
	
	public static NumberFormatter createNumberFormatter() {
		NumberFormat format = NumberFormat.getNumberInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Double.class);
		formatter.setCommitsOnValidEdit(true);
		return formatter;
	}
	
	private static class DashedBorder extends AbstractBorder {

		private static final long serialVersionUID = 3637717695720415472L;
		
		private final Color color;
		private final int width;
		
		
		public DashedBorder(Color color, int width) {
			this.color = color;
			this.width = width;
		}

		@Override
	    public void paintBorder(Component comp, Graphics g, int x, int y, int w, int h) {
	        Graphics2D gg = (Graphics2D) g;
	        gg.setColor(color);
	        gg.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{width}, 0));
	        gg.drawLine(x, y + comp.getHeight(), x + w - 1, y + comp.getHeight());
	    }
	}
}
