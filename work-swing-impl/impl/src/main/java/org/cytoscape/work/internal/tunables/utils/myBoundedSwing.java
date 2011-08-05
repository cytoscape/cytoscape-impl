package org.cytoscape.work.internal.tunables.utils;


import javax.swing.*;

import java.awt.Color;
import java.awt.event.*;


public class myBoundedSwing extends JTextField {
	private JTextField jtf;
	private Number init;
	private Number defaultValue;
	private Number b_min, b_max;
	private Boolean lower, upper;
	private java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");

	public myBoundedSwing(Number initVal, Number b_min, Number b_max, Boolean lower, Boolean upper) {
		this.init = initVal;
		this.b_min = b_min;
		this.b_max = b_max;
		this.lower = lower;
		this.upper = upper;
		defaultValue = init;
		setInitValue();
		initUI();
	}

	@SuppressWarnings("serial")
	protected void initUI() {
		this.addActionListener(new AbstractAction() {
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
		setText(init.toString());
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
			throw new IllegalStateException("unexpected type: " + defaultValue.getClass() + "!");
	}

	private Double getDoubleFieldValue() {
		Double val = null;
		try {
                        val = Double.parseDouble(getText());
                } catch(NumberFormatException nfe) {
                        setBackground(Color.red);
                        JOptionPane.showMessageDialog(null, "Please enter a double", "Alert", JOptionPane.ERROR_MESSAGE);
                        setFieldValue();
                        setBackground(Color.white);

                        try {
                                val = (Double)defaultValue;
                        } catch(Exception e) {
				e.printStackTrace();
			}
                }

		if (val < (Double)b_min) {
			setBackground(Color.red);
			JOptionPane.showMessageDialog(null, "Value (" + val.doubleValue()
						      + ") is less than lower limit ("
						      + df.format(b_min.doubleValue())
						      + ")"+"\n"+"Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(Color.white);
			return (Double)defaultValue;
		}
		if (val > (Double)b_max) {
			setBackground(Color.red);
			JOptionPane.showMessageDialog(null, "Value (" + val.doubleValue()
						      +") is larger than upper limit ("
						      + df.format(b_max.doubleValue()) + ")"
						      + "\n" + "Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(Color.white);
			return (Double)defaultValue;
		}
		return val;
	}

	private Float getFloatFieldValue() {
		Float val = null;
		try {
                        val = Float.parseFloat(getText());
                } catch(NumberFormatException nfe) {
                        setBackground(Color.red);
                        JOptionPane.showMessageDialog(null, "Please enter a float", "Alert", JOptionPane.ERROR_MESSAGE);
                        setFieldValue();
                        setBackground(Color.white);

                        try {
                                val = (Float)defaultValue;
                        } catch(Exception e) {
				e.printStackTrace();
			}
                }

		if (val < (Float)b_min) {
			setBackground(Color.red);
			JOptionPane.showMessageDialog(null, "Value (" + val.floatValue()
						      + ") is less than lower limit ("
						      + df.format(b_min.floatValue())
						      + ")"+"\n"+"Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(Color.white);
			return (Float)defaultValue;
		}
		if (val > (Float)b_max) {
			setBackground(Color.red);
			JOptionPane.showMessageDialog(null, "Value (" + val.floatValue()
						      +") is larger than upper limit ("
						      + df.format(b_max.floatValue()) + ")"
						      + "\n" + "Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(Color.white);
			return (Float)defaultValue;
		}
		return val;
	}

	private Integer getIntegerFieldValue() {
		Integer val = null;
		try {
                        val = Integer.parseInt(getText());
                } catch(NumberFormatException nfe) {
                        setBackground(Color.red);
                        JOptionPane.showMessageDialog(null, "Please enter an integer", "Alert", JOptionPane.ERROR_MESSAGE);
                        setFieldValue();
                        setBackground(Color.white);

                        try {
                                val = (Integer)defaultValue;
                        } catch(Exception e) {
				e.printStackTrace();
			}
                }

		if (val < (Integer)b_min) {
			setBackground(Color.red);
			JOptionPane.showMessageDialog(null, "Value (" + val.intValue()
						      + ") is less than lower limit ("
						      + df.format(b_min.intValue())
						      + ")"+"\n"+"Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(Color.white);
			return (Integer)defaultValue;
		}
		if (val > (Integer)b_max) {
			setBackground(Color.red);
			JOptionPane.showMessageDialog(null, "Value (" + val.intValue()
						      +") is larger than upper limit ("
						      + df.format(b_max.intValue()) + ")"
						      + "\n" + "Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(Color.white);
			return (Integer)defaultValue;
		}
		return val;
	}

	private Long getLongFieldValue() {
		Long val = null;
		try {
                        val = Long.parseLong(getText());
                } catch(NumberFormatException nfe) {
                        setBackground(Color.red);
                        JOptionPane.showMessageDialog(null, "Please enter a large integer (\"long\")",
						      "Alert", JOptionPane.ERROR_MESSAGE);
                        setFieldValue();
                        setBackground(Color.white);

                        try {
                                val = (Long)defaultValue;
                        } catch(Exception e) {
				e.printStackTrace();
			}
                }

		if (val < (Long)b_min) {
			setBackground(Color.red);
			JOptionPane.showMessageDialog(null, "Value (" + val.longValue()
						      + ") is less than lower limit ("
						      + df.format(b_min.longValue())
						      + ")"+"\n"+"Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(Color.white);
			return (Long)defaultValue;
		}
		if (val > (Long)b_max) {
			setBackground(Color.red);
			JOptionPane.showMessageDialog(null, "Value (" + val.longValue()
						      +") is larger than upper limit ("
						      + df.format(b_max.longValue()) + ")"
						      + "\n" + "Value will be set to default : "
						      + defaultValue, "Alert",
						      JOptionPane.ERROR_MESSAGE);
			setInitValue();
			setFieldValue();
			setBackground(Color.white);
			return (Long)defaultValue;
		}
		return val;
	}
}