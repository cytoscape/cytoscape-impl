package org.cytoscape.ding.impl.cyannotator.utils;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.getErrorColor;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
public class EnhancedSlider extends JPanel {
	
	private JSlider slider;
	private JFormattedTextField textField;
	
	private final int min, max;
	private int value;
	private final int defValue;
	private final int majorTickSpacing, minorTickSpacing;
	
	private final List<Object> listeners;
	
	private boolean ignore;
	
	private final String newline = System.getProperty("line.separator");
	
	private static boolean shiftPressed;
	
	static {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent ke) {
				if (ke.getID() == KeyEvent.KEY_PRESSED) {
					if (ke.getKeyCode() == KeyEvent.VK_SHIFT)
						shiftPressed = true;
				} else if (ke.getID() == KeyEvent.KEY_RELEASED) {
					if (ke.getKeyCode() == KeyEvent.VK_SHIFT)
						shiftPressed = false;
				}
				
				return false;
			}
		});
	}
	
	/**
	 * Creates a default slider, from 0 to 100.
	 * @param value a number between 0 and 100
	 */
	public EnhancedSlider(int value) {
		this(0, 100, value, 50, 25);
	}
	
	public EnhancedSlider(int min, int max, int value, int majorTickSpacing, int minorTickSpacing) {
		this.min = min;
		this.max = max;
		this.value = this.defValue = value;
		this.majorTickSpacing = majorTickSpacing;
		this.minorTickSpacing = minorTickSpacing;
		listeners = new ArrayList<>();
		
		initUI();
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		ignore = true;
		this.value = value;
		getSlider().setValue(value);
		getTextField().setValue(value);
		ignore = false;
	}
	
	public void addChangeListener(ChangeListener cl) {
		if (!listeners.contains(cl))
			listeners.add(cl);
	}

	public void removeChangeListener(ChangeListener cl) {
		listeners.remove(cl);
	}
	
	protected void fireChangeEvent() {
		var iter = listeners.iterator();
		var evt = new ChangeEvent(this);
		
		while (iter.hasNext()) {
			var cl = (ChangeListener) iter.next();
			cl.stateChanged(evt);
		}
	}
	
	public JFormattedTextField getTextField() {
		if (textField == null) {
			var format = NumberFormat.getInstance();
			var formatter = new NumberFormatter(format);
		    formatter.setValueClass(Integer.class);
		    formatter.setMinimum(min);
		    formatter.setMaximum(max);
		    formatter.setAllowsInvalid(false);
			
			textField = new MyFormattedTextField(format);
			textField.setHorizontalAlignment(JTextField.RIGHT);
			
			if (isAquaLAF())
				textField.putClientProperty("JComponent.sizeVariant", "small");
			else if (textField.getFont() != null)
				textField.setFont(textField.getFont().deriveFont(getSmallFontSize()));
			
			textField.addActionListener(evt -> {
				textFieldValueChanged();
			});
			textField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent evt) {
					textFieldValueChanged();
				}
			});
		}
		
		return textField;
	}
	
	public JSlider getSlider() {
		if (slider == null) {
			slider = new JSlider(min, max, value) {
				@Override
				public boolean getSnapToTicks() {
					return shiftPressed;
				}
			};
			slider.setMajorTickSpacing(majorTickSpacing);
			slider.setMinorTickSpacing(minorTickSpacing);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			
			slider.setPreferredSize(new Dimension(120, slider.getPreferredSize().height));
			getTextField().setValue(value);
			
			slider.addChangeListener(evt -> {
				if (ignore)
					return;
				
				ignore = true;
				// update the value
				value = getSlider().getValue();

				// Due to small inaccuracies in the slider position, it's possible
				// to get values less than the min or greater than the max.  If so,
				// just adjust the value and don't issue a warning.
				value = clamp(value);

				// set text field value
				getTextField().setValue(value);
				// fire event
				fireChangeEvent();
				ignore = false;
			});
		}
		
		return slider;
	}
	
	private void initUI() {
		var layout = new GroupLayout(this);
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
	}
  
	private int getFieldValue() {
		int val = defValue;
		String errorMsg = null;
		
		try {
			val = Integer.valueOf(getTextField().getText());
		} catch (NumberFormatException nfe) {
			errorMsg = "Please enter a valid number.";
		}
		
		if (val < min) {
			errorMsg = "Value (" + val + ") is less than lower limit (" + min + ")."
					+ newline + "Value will be set to the lowest allowed number: " + min;
			val = min;
		} else if (val > max) {
			errorMsg = "Value (" + val + ") is more than upper limit (" + max + ")."
					+ newline + "Value will be set to the highest allowed number: " + max;
			val = max;
		}
		
		if (errorMsg != null)
			showErrorMessage(errorMsg, val);
		
		return val;
	}

	private void showErrorMessage(String msg, int val) {
		getTextField().setForeground(getErrorColor());
		JOptionPane.showMessageDialog(null, msg, "Invalid Number", JOptionPane.ERROR_MESSAGE);
		getTextField().setValue(val);
		getTextField().setForeground(UIManager.getColor("TextField.foreground"));
	}
	
	private int clamp(int val) {
		val = Math.min(val, max);
		val = Math.max(val, min);

		return val;
	}
	
	private void textFieldValueChanged() {
		if (ignore)
			return;
		
		ignore = true;
		int v = getFieldValue();
		
		if (v != value) {
			value = v;
			getSlider().setValue(value);
		}
		
		fireChangeEvent();
		ignore = false;
	}
	
	private class MyFormattedTextField extends JFormattedTextField {
		
		private final NumberFormat numberFormat;
		
		public MyFormattedTextField(NumberFormat format) {
			super(format);
			this.numberFormat = format;
		}

		@Override
		public Dimension getPreferredSize() {
			var d = super.getPreferredSize();
			
			if (this.getGraphics() != null && numberFormat != null) {
				// Set the preferred text field size after it gets a Graphics
				var fm = this.getGraphics().getFontMetrics();
				int minW = fm.stringWidth(numberFormat.format(min));
				int maxW = fm.stringWidth(numberFormat.format(max));
				int sw = 16 + Math.max(minW, maxW);
				d.width = Math.max(sw, 48);
			}
			
			return d;
		}
	}
}
