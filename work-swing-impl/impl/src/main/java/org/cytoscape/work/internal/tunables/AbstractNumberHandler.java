package org.cytoscape.work.internal.tunables;

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


import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.setTooltip;
import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.updateFieldPanel;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.ParsePosition;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


/**
 * Base class for numeric Tunable handlers
 *
 * @author pasteur
 */
public abstract class AbstractNumberHandler extends AbstractGUITunableHandler implements FocusListener {
	
	private JTextField textField;
	private DecimalFormat format;
	private boolean isUpdating = false;

	/**
	 * It creates the Swing component for this Object (JTextField) that contains the initial value
	 * of the Double Object annotated as <code>Tunable</code>, its description, and displays it in a proper way.
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	public AbstractNumberHandler(Field f, Object o, Tunable t) {
		super(f,o,t);
		init();
	}

	public AbstractNumberHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	private void init() {
		format = null;
		
		if (getFormat() != null && getFormat().length() > 0) {
			format = new DecimalFormat(getFormat());
		}

		Number d = null;
		try {
			d = (Number)getNumberValue();
		} catch(final Exception e) {
			e.printStackTrace();
			d = Double.valueOf(0.0);
		}

		// Figure out how to handle the format for this particular value
		if (format == null) {
			double dx = d.doubleValue();
			if (dx > 1000000.0 || dx < 0.001)
				format = new DecimalFormat("0.#####E0");
			else
				format = new DecimalFormat();
		}
		
		format.setGroupingUsed(false);

		// Set Gui
		DocumentFilter filter = (new DocumentFilter() {
			@Override
			public void remove(DocumentFilter.FilterBypass fb,
			          int offset,
			          int length)
			            throws BadLocationException {
				Document doc = fb.getDocument();
				StringBuilder sb = new StringBuilder();
				sb.append(doc.getText(0, doc.getLength()));
				sb.delete(offset, offset + length);
				
				if(!isUpdating) {
					if(!setValueFromText(sb.toString())) {
						return;
					}
				}
				super.remove(fb, offset, length);
			}
			
			@Override
			public void insertString(DocumentFilter.FilterBypass fb,
	                int offset,
	                String string,
	                AttributeSet attr)
	                  throws BadLocationException {
				Document doc = fb.getDocument();
				StringBuilder sb = new StringBuilder();
				sb.append(doc.getText(0, doc.getLength()));
				sb.insert(offset, string);
				
				if(!isUpdating) {
					if(!setValueFromText(sb.toString())) {
						return;
					}
				}
				super.insertString(fb, offset, string, attr);
			}
			
			@Override
		    public void replace(FilterBypass fb, int i, int i1, String string, AttributeSet as) throws BadLocationException {
				Document doc = fb.getDocument();
				StringBuilder sb = new StringBuilder();
				sb.append(doc.getText(0, doc.getLength()));
				sb.replace(i, i + i1, string);
				
				if(!isUpdating) {
					if(!setValueFromText(sb.toString())) {
						return;
					}
				}
				super.replace(fb, i, i1, string, as);
			}
			
		});
		
		textField = new JTextField(format.format(d));
		textField.setPreferredSize(new Dimension(GUIDefaults.TEXT_BOX_WIDTH, textField.getPreferredSize().height));
		textField.setHorizontalAlignment(JTextField.RIGHT);
		textField.addFocusListener(this);
		((AbstractDocument)textField.getDocument()).setDocumentFilter(filter);
		
		final JLabel label = new JLabel(getDescription());
		
		updateFieldPanel(panel, label, textField, horizontal);
		setTooltip(getTooltip(), label, textField);
	}

	@Override
	public void update(){
		isUpdating = true;
		Number d;
		try {
			d = (Number)getNumberValue();
			textField.setText(format.format(d));
		} catch(final Exception e) {
			e.printStackTrace();
		}
		isUpdating = false;
	}
	
	/**
	 * Catches the value inserted in the JTextField, parses it to a <code>Double</code> value,
	 * and tries to set it to the initial object.
	 * If it can't, throws an exception that displays the source error to the user
	 */
	@Override
	public void handle() {
		//stub - all value changes are handled on the fly by DocumentFilter
	}
	
	private boolean setValueFromText(String text) {
		Number d = null;
		
		try {
			d = getFieldValue(text);
		} catch(NumberFormatException nfe) {
			// Got a format exception -- try parsing it according to the format to handle exponential
			ParsePosition pos = new ParsePosition(0);
			d = format.parse(text, pos);
			if(d == null || (text.endsWith(".")) ||
							(text.length() != pos.getIndex() &&
							!text.substring(pos.getIndex()).equals("E") &&
							!text.substring(pos.getIndex()).equals("E-"))) {
				Toolkit.getDefaultToolkit().beep();
				return false;
			}
		}
		try {
			d = getTypedValue(d); 
		} catch (NumberFormatException nfe) {
			Toolkit.getDefaultToolkit().beep();
			return false;
		}

		// Make sure we got a reasonable value by attempting to set the value
		try {
			setValue(d);
		} catch (final InvocationTargetException e) {
			JOptionPane.showMessageDialog(
					null,
					e.getTargetException().getMessage(), 
					"Error",
					JOptionPane.ERROR_MESSAGE
			);
			return false;
		} catch (IllegalAccessException e) {
			JOptionPane.showMessageDialog(
					null,
					e.getMessage(), 
					"Error",
					JOptionPane.ERROR_MESSAGE
			);
			return false;
		}
		return true;
	}

	abstract public Number getFieldValue(String text);

	// We need this because format.parse will not necessarily gives us the right
	// type back
	abstract public Number getTypedValue(Number number);

	/**
	 * To get the item that is currently selected
	 */
	@Override
	public String getState() {
		if ( textField == null )
			return "";

		final String text = textField.getText();
		if ( text == null )
			return "";

		return text;
	}
	
	@Override
	public void focusLost(FocusEvent e) {
		update();
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		
	}

	private Number getNumberValue() {
		try {
			return (Number)getValue();
		} catch (Exception e) {
			return (Number)new Double(0.0);
		}
	}
}
