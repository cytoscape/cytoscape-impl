package org.cytoscape.filter.internal.view;

import java.awt.Color;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.text.NumberFormatter;

public class ViewUtil {
	public static void configureFilterView(JComponent component) {
		component.setBackground(Color.WHITE);
		component.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY));
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
}
