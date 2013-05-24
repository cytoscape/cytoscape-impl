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


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.internal.tunables.utils.myBoundedSwing;
import org.cytoscape.work.internal.tunables.utils.mySlider;
import org.cytoscape.work.util.AbstractBounded;
import org.cytoscape.work.util.BoundedDouble;


/**
 * Handler for the type <i>Bounded</i> of <code>Tunable</code>
 *
 * @author pasteur
 *
 * @param <T> type of <code>AbstractBounded</code>
 */
@SuppressWarnings("unchecked")
public class BoundedHandler<T extends AbstractBounded> extends AbstractGUITunableHandler implements ChangeListener, ActionListener {
	/**
	 * Representation of the <code>Bounded</code> in a <code>JSlider</code>
	 */
	private boolean useSlider = false;

	/**
	 * 1st representation of this <code>Bounded</code> object in its <code>GUIHandler</code>'s JPanel : a <code>JSlider</code>
	 */
	private mySlider slider;

	/**
	 * 2nd representation of this <code>Bounded</code> object : a <code>JTextField</code> that will display to the user all the informations about the bounds
	 */
	private myBoundedSwing boundedField;


	/**
	 * Construction of the <code>GUIHandler</code> for the <code>Bounded</code> type
	 *
	 * If <code>useSlider</code> is set to <code>true</code> : displays the bounded object in a <code>JSlider</code> by using its bounds
	 * else diplays it in a <code>JTextField</code> with informations about the bounds
	 *
	 * The Swing representation is then added to the <code>JPanel</code> for GUI representation
	 *
	 * @param f field that has been annotated
	 * @param o object containing <code>f</code>
	 * @param t tunable associated with <code>f</code>
	 */
	public BoundedHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
		init();
	}

	public BoundedHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	private void init() {
		final String title = getDescription();
		useSlider = getParams().getProperty("slider", "false").equalsIgnoreCase("true");
		panel = new JPanel(new BorderLayout(GUIDefaults.hGap, GUIDefaults.vGap));

		try {
			final T bounded = getBounded();
			if (useSlider) {
				JLabel label = new JLabel(title);
				label.setFont(GUIDefaults.LABEL_FONT);
				label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				panel.add(label,BorderLayout.WEST);
				slider = new mySlider(title, (Number)bounded.getLowerBound(), (Number)bounded.getUpperBound(),
				                      (Number)bounded.getValue(), bounded.isLowerBoundStrict(), bounded.isUpperBoundStrict());
				slider.addChangeListener(this);
				panel.add(slider,BorderLayout.CENTER);
			} else {
				final JLabel label =
					new JLabel(title + " (max: " + bounded.getLowerBound().toString()
					          + " min: " + bounded.getUpperBound().toString() + ")" );
				label.setFont(GUIDefaults.LABEL_FONT);
				label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				boundedField = new myBoundedSwing((Number)bounded.getValue(), (Number)bounded.getLowerBound(),
				                                  (Number)bounded.getUpperBound(), bounded.isLowerBoundStrict(),
				                                  bounded.isUpperBoundStrict());
				panel.add(label, BorderLayout.WEST);
				panel.add(boundedField, BorderLayout.CENTER);
				boundedField.addActionListener(this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Set the tooltip.  Note that at this point, we're setting
		// the tooltip on the entire panel.  This may or may not be
		// the right thing to do.
		if (getTooltip() != null && getTooltip().length() > 0) {
			final ToolTipManager tipManager = ToolTipManager.sharedInstance();
			tipManager.setInitialDelay(1);
			tipManager.setDismissDelay(7500);
			panel.setToolTipText(getTooltip());
		}
	}

	private T getBounded() throws Exception {
		try {
			return (T)getValue();
		} catch (final Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void update(){
		
		final String title = getDescription();

		try {
			final T bounded = getBounded();
			if (useSlider){
				Number n = (Number) bounded.getValue();
				slider.setValue(n);
			}else{
				final JLabel label =
					new JLabel(title + " (max: " + bounded.getLowerBound().toString()
					          + " min: " + bounded.getUpperBound().toString() + ")" );
				label.setFont(GUIDefaults.LABEL_FONT);
				boundedField = new myBoundedSwing((Number)bounded.getValue(), (Number)bounded.getLowerBound(),
				                                  (Number)bounded.getUpperBound(), bounded.isLowerBoundStrict(),
				                                  bounded.isUpperBoundStrict());
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * To set the value (from the JSlider or the JTextField) to the <code>Bounded</code> object
	 *
	 * The constraints of the bound values have to be respected : <code>lowerBound &lt; value &lt; upperBound</code> or <code>lowerBound &lti; value &lti; upperBound</code> ....
	 */
	public void handle() {
		try {
			final T bounded = getBounded();
			final Number fieldValue = useSlider ? slider.getValue() : boundedField.getFieldValue();
			if (fieldValue instanceof Double){
				bounded.setValue((Double)fieldValue);
			}
			else if (fieldValue instanceof Float)
				bounded.setValue((Float)fieldValue);
			else if (fieldValue instanceof Integer)
				bounded.setValue((Integer)fieldValue);
			else if (fieldValue instanceof Long)
				bounded.setValue((Long)fieldValue);
			else
				throw new IllegalStateException("unexpected type: " + fieldValue.getClass() + ".");
			
			setValue(bounded);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * To get the current value of the <code>Bounded</code> object
	 * @return the value of the <code>Bounded</code> object
	 */
	public String getState() {
		try {
			return getBounded().getValue().toString();
		} catch (final Exception e) {
			return "";
		}
	}

	
	public void stateChanged(ChangeEvent e) {
		handle();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		handle();
	}
}
