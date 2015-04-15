package org.cytoscape.work.internal.tunables.utils;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;


public class TunableBoundedField extends JTextField {
	
	private static final long serialVersionUID = -3420958244665421522L;
	
	private final Number initVal;
	private Number defaultValue;
	private final Number min, max;
	private java.text.DecimalFormat df = new DecimalFormat("##.##");

	public TunableBoundedField(
			final Number initVal,
			final Number min,
			final Number max,
			final Boolean lower,
			final Boolean upper
	) {
		this.initVal = initVal;
		this.min = min;
		this.max = max;
		defaultValue = initVal;
		setInitValue();
		initUI();
	}

	@SuppressWarnings("serial")
	protected void initUI() {
		this.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				Number val = getFieldValue();
				if (!val.equals(defaultValue))
					defaultValue = val;
				setFieldValue();
			}
		});
		setHorizontalAlignment(JTextField.RIGHT);
	}

	private void setFieldValue(){
		setText(defaultValue.toString());
	}

	private void setInitValue(){
		setText(initVal.toString());
	}

	public Number getFieldValue() {
		if (defaultValue instanceof Double)
			return getDoubleFieldValue();
		else if (defaultValue instanceof Float)
			return getFloatFieldValue();
		else if (defaultValue instanceof Integer)
			return getIntegerFieldValue();
		else if (defaultValue instanceof Long)
			return getLongFieldValue();
		else
			throw new IllegalStateException("unexpected type: " + defaultValue.getClass() + ".");
	}

	private Double getDoubleFieldValue() {
		Double val = null;
		
		try {
			val = Double.parseDouble(getText());
		} catch (NumberFormatException nfe) {
			setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "Please enter a double", "Alert", JOptionPane.ERROR_MESSAGE);
			setFieldValue();
			setBackground(UIManager.getColor("TextField.background"));

			try {
				val = (Double) defaultValue;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (val < (Double)min) {
			setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "Value (" + val.doubleValue()
						      + ") is less than lower limit ("
						      + df.format(min.doubleValue())
						      + ")"+"\n"+"Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(UIManager.getColor("TextField.background"));
			
			return (Double)defaultValue;
		}
		
		if (val > (Double)max) {
			setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "Value (" + val.doubleValue()
						      +") is larger than upper limit ("
						      + df.format(max.doubleValue()) + ")"
						      + "\n" + "Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(UIManager.getColor("TextField.background"));
			
			return (Double)defaultValue;
		}
		
		return val;
	}

	private Float getFloatFieldValue() {
		Float val = null;
		
		try {
			val = Float.parseFloat(getText());
		} catch (NumberFormatException nfe) {
			setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "Please enter a float", "Alert", JOptionPane.ERROR_MESSAGE);
			setFieldValue();
			setBackground(UIManager.getColor("TextField.background"));

			try {
				val = (Float) defaultValue;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (val < (Float)min) {
			setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "Value (" + val.floatValue()
						      + ") is less than lower limit ("
						      + df.format(min.floatValue())
						      + ")"+"\n"+"Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(UIManager.getColor("TextField.background"));
			
			return (Float)defaultValue;
		}
		if (val > (Float)max) {
			setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "Value (" + val.floatValue()
						      +") is larger than upper limit ("
						      + df.format(max.floatValue()) + ")"
						      + "\n" + "Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(UIManager.getColor("TextField.background"));
			
			return (Float)defaultValue;
		}
		
		return val;
	}

	private Integer getIntegerFieldValue() {
		Integer val = null;
		
		try {
			val = Integer.parseInt(getText());
		} catch (NumberFormatException nfe) {
			setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "Please enter an integer", "Alert", JOptionPane.ERROR_MESSAGE);
			setFieldValue();
			setBackground(UIManager.getColor("TextField.background"));

			try {
				val = (Integer) defaultValue;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (val < (Integer)min) {
			setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "Value (" + val.intValue()
						      + ") is less than lower limit ("
						      + df.format(min.intValue())
						      + ")"+"\n"+"Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(UIManager.getColor("TextField.background"));
			
			return (Integer)defaultValue;
		}
		
		if (val > (Integer)max) {
			setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "Value (" + val.intValue()
						      +") is larger than upper limit ("
						      + df.format(max.intValue()) + ")"
						      + "\n" + "Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(UIManager.getColor("TextField.background"));
			
			return (Integer)defaultValue;
		}
		
		return val;
	}

	private Long getLongFieldValue() {
		Long val = null;
		
		try {
			val = Long.parseLong(getText());
		} catch (NumberFormatException nfe) {
			setBackground(Color.RED);
			JOptionPane.showMessageDialog(
					null,
					"Please enter a large integer (\"long\")", "Alert",
					JOptionPane.ERROR_MESSAGE);
			setFieldValue();
			setBackground(UIManager.getColor("TextField.background"));

			try {
				val = (Long) defaultValue;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (val < (Long)min) {
			setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "Value (" + val.longValue()
						      + ") is less than lower limit ("
						      + df.format(min.longValue())
						      + ")"+"\n"+"Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(UIManager.getColor("TextField.background"));
			
			return (Long)defaultValue;
		}
		
		if (val > (Long)max) {
			setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "Value (" + val.longValue()
						      +") is larger than upper limit ("
						      + df.format(max.longValue()) + ")"
						      + "\n" + "Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(UIManager.getColor("TextField.background"));
			
			return (Long)defaultValue;
		}
		
		return val;
	}
}
