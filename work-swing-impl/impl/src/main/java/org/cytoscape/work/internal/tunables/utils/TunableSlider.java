package org.cytoscape.work.internal.tunables.utils;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.getErrorColor;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

@SuppressWarnings("serial")
public class TunableSlider extends JPanel {
	
	private static final int S_MIN = 0;
	private static final int S_MAX = 1000;
	
	private JSlider slider;
	private JFormattedTextField textField;
	
	private final Number min, max;
	private Number value;
	private final List<Object> listeners;
	private final DecimalFormat format;
	private boolean ignore;
	
	String newline = System.getProperty("line.separator");
	Boolean upper;
	Boolean lower;
	
	public TunableSlider(
			final String title,
			final Number min,
			final Number max,
			final Number value,
			final Boolean lowerstrict,
			final Boolean upperstrict,
			final DecimalFormat format
	) {
		this.min = min;
		this.max = max;
		this.value = value;
		upper = upperstrict;
		lower = lowerstrict;
		listeners = new ArrayList<>();
		this.format = format;

		initUI();
	}

	@SuppressWarnings("unchecked")
	protected void initUI() {
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getSlider(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, false)
				.addComponent(getSlider())
				.addGroup(layout.createSequentialGroup()
						.addGap(isAquaLAF() ? 4 : 0)
						.addComponent(getTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
		
		if (isAquaLAF())
			setOpaque(false);
		
		// Change the slider's label sizes -- only works if it's done after the slider has been added to
		// its parent container and had its UI assigned
		final Font tickFont = getSlider().getFont().deriveFont(getSmallFontSize());
		final Dictionary<Integer, JLabel> labelTable = getSlider().getLabelTable();
		
		for (Enumeration<Integer> enumeration = labelTable.keys(); enumeration.hasMoreElements();) {
			int k = enumeration.nextElement();
			final JLabel label = labelTable.get(k);
			label.setFont(tickFont); // Updates the font size
			label.setSize(label.getPreferredSize()); // Updates the label size and slider layout
		}
	}
	
	public Number getValue(){
		return getFieldValue();
	}

	public void setValue(final Number value) {
		ignore = true;
		this.value = value;
		setSliderValue();
		setFieldValue();
		ignore = false;
	}
	
	private JFormattedTextField getTextField() {
		if (textField == null) {
			textField = new JFormattedTextField(format) {
				@Override
				public Dimension getPreferredSize() {
					final Dimension d = super.getPreferredSize();
					
					if (this.getGraphics() != null) {
						// Set the preferred text field size after it gets a Graphics
						int sw = 16 + this.getGraphics().getFontMetrics().stringWidth(format.format(max.doubleValue()));
						d.width = Math.max(sw, 48);
					}
					
					return d;
				}
			};
			
			textField.setHorizontalAlignment(JTextField.RIGHT);
			
			if (isAquaLAF())
				textField.putClientProperty("JComponent.sizeVariant", "small");
			else if (textField.getFont() != null)
				textField.setFont(textField.getFont().deriveFont(getSmallFontSize()));
			
			textField.addActionListener((ActionEvent e) -> {
				textFieldValueChanged();
			});
			textField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					textFieldValueChanged();
				}
			});
		}
		
		return textField;
	}
	
	private JSlider getSlider() {
		if (slider == null) {
			final int S_RANGE = S_MAX - S_MIN;
			
			slider = new JSlider(S_MIN, S_MAX);
			final Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
			
			if (value instanceof Double || value instanceof Float) {
				final double range = max.doubleValue() - min.doubleValue();
				
				slider.setMajorTickSpacing(S_RANGE / 4);
				slider.setMinorTickSpacing(S_RANGE / 20);
				
				labelTable.put(S_MIN, new JLabel(format.format(min.doubleValue())));
				labelTable.put(S_RANGE / 2, new JLabel(format.format(range / 2)));
				labelTable.put(S_MAX, new JLabel(format.format(max.doubleValue())));
			} else if (value instanceof Long || value instanceof Integer) {
				final long range = max.longValue() - min.longValue();
				int minorTick = Math.round(S_RANGE / (float) range);
				
				slider.setSnapToTicks(minorTick > 0 && S_RANGE / minorTick <= 20);
				
				minorTick = Math.max(minorTick, S_RANGE / 20);
				slider.setMinorTickSpacing(minorTick);
				
				if (range >= 12 && range % 4 == 0)
					slider.setMajorTickSpacing(S_RANGE / 4);
				else if (range % 2 == 0)
					slider.setMajorTickSpacing(S_RANGE / 2);
				
				labelTable.put(S_MIN, new JLabel(format.format(min.longValue())));
				
				if (range % 2 == 0)
					labelTable.put(S_RANGE / 2, new JLabel(format.format(range / 2)));
				
				labelTable.put(S_MAX, new JLabel(format.format(max.longValue())));
			}
			
			slider.setPreferredSize(new Dimension(280, slider.getPreferredSize().height));
			slider.setLabelTable(labelTable);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			setSliderValue();
			setFieldValue();
			
			slider.addChangeListener((ChangeEvent e) -> {
				if (ignore)
					return;
				
				ignore = true;
				// update the value
				value = getTunableValue();
				// set text field value
				setFieldValue();
				// fire event
				fireChangeEvent();
				ignore = false;
			});
		}
		
		return slider;
	}
	
	private Number getTunableValue() {
		final int S_RANGE = S_MAX - S_MIN;
		
		if (value instanceof Integer) {
			int val = getSlider().getValue();
			int min = this.min.intValue();
			int max = this.max.intValue();
			
			if (upper) max--;
			if (lower) min++;
			
			val = Math.min(val, S_MAX);
			val = Math.max(val, S_MIN);
			
			return Math.round(min + (val - S_MIN) * (max - min) / (float) S_RANGE);
		} else if (value instanceof Long) {
			long val = getSlider().getValue();
			long min = this.min.longValue();
			long max = this.max.longValue();
			
			if (upper) max--;
			if (lower) min++;
			
			val = Math.min(val, S_MAX);
			val = Math.max(val, S_MIN);
			
			return Math.round(min + (val - S_MIN) * (max - min) / (double) S_RANGE);
		} else {
			double val = getSlider().getValue();
			double min = this.min.doubleValue();
			double max = this.max.doubleValue();
			
			if (upper) max -= 0.000000001;
			if (lower) min += 0.000000001;
			
			val = Math.min(val, S_MAX);
			val = Math.max(val, S_MIN);
			val = Math.round(min + (val - S_MIN) * (max - min) / (double) S_RANGE);
			
			return (value instanceof Double ? (Number) new Double(val) : new Float((float) val));
		}
	}
  
	private void setSliderValue() {
		int val;
		
		if (value instanceof Double || value instanceof Float) {
			double value = this.value.doubleValue();
			double min = this.min.doubleValue();
			double max = this.max.doubleValue();
			
			val = S_MIN + (int) Math.round(((S_MAX - S_MIN) * (value - min)) / (max - min));
		} else {
			long value = this.value.longValue();
			long min = this.min.longValue();
			long max = this.max.longValue();
			
			val = S_MIN + Math.round(((S_MAX - S_MIN) * (value - min)) / (float) (max - min));
		}
		
		getSlider().setValue(val);
	}
  
	private Number getFieldValue(){
		Double val = null;
		Number n = format.parse(getTextField().getText(), new ParsePosition(0));
		final Color errColor = getErrorColor();
		
		if (n == null) {
			try {
				val = Double.valueOf(getTextField().getText());
			} catch (NumberFormatException nfe) {
				getTextField().setForeground(errColor);
				JOptionPane.showMessageDialog(null, "Please enter a Value", "Alert", JOptionPane.ERROR_MESSAGE);
				setFieldValue();
				getTextField().setForeground(UIManager.getColor("TextField.foreground"));

				try {
					val = value.doubleValue();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			val = n.doubleValue();
		}
		
		if (value instanceof Double || value instanceof Float) {
			if (val < min.doubleValue()) {
				getTextField().setForeground(errColor);
				JOptionPane.showMessageDialog(
						null,
						"Value ("+val.doubleValue()+") is less than lower limit ("+format.format(min.doubleValue())+")"+newline+"Value will be set to default : "+value,
						"Alert",
						JOptionPane.ERROR_MESSAGE);
				setFieldValue();
				getTextField().setForeground(UIManager.getColor("TextField.foreground"));
				
				return value;
			}
			if (val > max.doubleValue()) {
				getTextField().setForeground(errColor);
				JOptionPane.showMessageDialog(
						null,
						"Value ("+val.doubleValue()+") is more than upper limit ("+format.format(max.doubleValue())+")"+newline+"Value will be set to default : "+value,
						"Alert",
						JOptionPane.ERROR_MESSAGE);
				setFieldValue();
				getTextField().setForeground(UIManager.getColor("TextField.foreground"));
				
				return value;
			}
			
			return value instanceof Double ? (Number)val.doubleValue() : val.floatValue();
		} else {
			if (val < min.longValue()) {
				getTextField().setForeground(errColor);
				JOptionPane.showMessageDialog(
						null,
						"Value ("+val.longValue()+") is less than lower limit ("+min.longValue()+")"+newline+"Value will be set to default : "+value,
						"Alert",
						JOptionPane.ERROR_MESSAGE);
				setFieldValue();
				getTextField().setForeground(UIManager.getColor("TextField.foreground"));
				
				return value;
			}
			if (val > max.longValue()) {
				getTextField().setForeground(errColor);
				JOptionPane.showMessageDialog(
						null,
						"Value ("+val.longValue()+") is much than upper limit ("+max.longValue()+")"+newline+"Value will be set to default : "+value,
						"Alert",
						JOptionPane.ERROR_MESSAGE);
				setFieldValue();
				getTextField().setForeground(UIManager.getColor("TextField.foreground"));
				
				return value;
			}
			
			return value instanceof Long ? (Number)val.longValue() : val.intValue();
		}
	}
	
	private void setFieldValue() {
		getTextField().setValue(value);
	}
	
	public void addChangeListener(ChangeListener cl) {
		if (!listeners.contains(cl))
			listeners.add(cl);
	}

	public void removeChangeListener(ChangeListener cl) {
		listeners.remove(cl);
	}
	
	protected void fireChangeEvent() {
		Iterator<Object> iter = listeners.iterator();
		ChangeEvent evt = new ChangeEvent(this);
		
		while (iter.hasNext()) {
			ChangeListener cl = (ChangeListener) iter.next();
			cl.stateChanged(evt);
		}
	}
	
	private void textFieldValueChanged() {
		if (ignore)
			return;
		
		ignore = true;
		Number v = getFieldValue();
		
		if (v != value) {
			// update the value
			value = v;
			// set slider value
			setSliderValue();
		}
		
		// fire event
		fireChangeEvent();
		ignore = false;
	}
}
