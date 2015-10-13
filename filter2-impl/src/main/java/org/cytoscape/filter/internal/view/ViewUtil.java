package org.cytoscape.filter.internal.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.text.NumberFormatter;

public class ViewUtil {
	
	public static final Color SELECTED_BACKGROUND_COLOR = UIManager.getColor("Table.selectionBackground");
	public static final Color UNSELECTED_BACKGROUND_COLOR = UIManager.getColor("Table.background");
	
	public static final int INTERNAL_VERTICAL_PADDING = 4;
	
	public static final Border COMPOSITE_PANEL_BORDER = BorderFactory.createCompoundBorder(
			new DashedBorder(UIManager.getColor("Separator.foreground"), 3),
			BorderFactory.createEmptyBorder(INTERNAL_VERTICAL_PADDING, 0, INTERNAL_VERTICAL_PADDING, 0));
	
	public static void configureFilterView(JComponent component) {
		component.setBackground(UNSELECTED_BACKGROUND_COLOR);
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
	
	public static NumberFormatter createIntegerFormatter() {
		NumberFormat format = NumberFormat.getIntegerInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Integer.class);
		formatter.setCommitsOnValidEdit(true);
		return formatter;
	}
	
	public static AbstractFormatterFactory createIntegerFormatterFactory() {
		return new AbstractFormatterFactory() {
			@Override
			public AbstractFormatter getFormatter(JFormattedTextField tf) {
				return createIntegerFormatter();
			}
		};
	}
	
	
	
	public static NumberFormatter createNumberFormatter() {
		NumberFormat format = NumberFormat.getNumberInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Double.class);
		formatter.setCommitsOnValidEdit(true);
		return formatter;
	}
	
	public static NumberFormatterFactory createNumberFormatterFactory() {
		return new NumberFormatterFactory();
	}
	
	private static class NumberFormatterFactory extends AbstractFormatterFactory {
		NumberFormatter decimal;
		NumberFormatter scientific;
		
		public NumberFormatterFactory() {
			decimal = createNumberFormatter();
			scientific = createNumberFormatter();
			
			Format scientificFormat = scientific.getFormat();
			
			if(scientificFormat instanceof DecimalFormat)
				((DecimalFormat) scientificFormat).applyPattern("0.0####E0");
		}
		@Override
		public AbstractFormatter getFormatter(JFormattedTextField tf) {
			Double dx = 0.0;
			if(tf.getValue() != null && tf.getValue() instanceof Double)
				dx = Math.abs((Double) tf.getValue());
			if (dx != 0 && (dx > 1000000.0 || dx < 0.001))
				return scientific;
			else return decimal;
		}
		
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
	
	public static String abbreviate(String s, int maxLength) {
		s = String.valueOf(s); // null check
		if(s.length() > maxLength) {
			s = s.substring(0, maxLength) + "...";
		}
		return s;
	}
	
	
	@SuppressWarnings("serial")
	public static ListCellRenderer<Object> createElipsisRenderer(int maxLength) {
		return new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				String longText  = String.valueOf(value); // in case its null
				String shortText = abbreviate(longText, maxLength);
				setToolTipText(longText);
				setText(shortText);
				return this;
			}
		};
	}
	
}
