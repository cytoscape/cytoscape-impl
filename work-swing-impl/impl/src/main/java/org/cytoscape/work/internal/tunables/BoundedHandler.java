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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.TunableBoundedField;
import org.cytoscape.work.internal.tunables.utils.TunableSlider;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.util.AbstractBounded;
import org.cytoscape.work.util.BoundedChangeListener;


/**
 * Handler for the type <i>Bounded</i> of <code>Tunable</code>
 *
 * @author pasteur
 *
 * @param <T> type of <code>AbstractBounded</code>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class BoundedHandler<T extends AbstractBounded, N> extends AbstractGUITunableHandler 
                                                          implements BoundedChangeListener<N> {
	/**
	 * Representation of the <code>Bounded</code> in a <code>JSlider</code>
	 */
	private boolean useSlider;

	/**
	 * 1st representation of this <code>Bounded</code> object in 
	 * its <code>GUIHandler</code>'s JPanel : a <code>JSlider</code>
	 */
	private TunableSlider slider;

	/**
	 * 2nd representation of this <code>Bounded</code> object : a <code>JTextField</code> 
	 * that will display to the user all the informations about the bounds
	 */
	private TunableBoundedField boundedField;

	/**
 	 * Save the last object we fetched.  This will allow us to detect that we're
 	 * received a completely new object and reset the UI.
 	 */
	private T lastBounded;
	private String title;

	// Standard format
	DecimalFormat df = new java.text.DecimalFormat("##.###");
	// Scientific notation
	DecimalFormat sdf = new java.text.DecimalFormat("#.###E0");

	/**
	 * Construction of the <code>GUIHandler</code> for the <code>Bounded</code> type
	 *
	 * If <code>useSlider</code> is set to <code>true</code> : displays the bounded 
	 * object in a <code>JSlider</code> by using its bounds
	 * else displays it in a <code>JTextField</code> with informations about the bounds
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
		title = getDescription();
		useSlider = getParams().getProperty("slider", "false").equalsIgnoreCase("true");

		try {
			final T bounded = getBounded();
			lastBounded = bounded;
			lastBounded.addListener(this);
			initPanel(bounded);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initPanel(T bounded) {
		double min = ((Number)bounded.getLowerBound()).doubleValue();
		double max = ((Number)bounded.getUpperBound()).doubleValue();
		double range = max - min;
		final DecimalFormat format = getDecimalFormat(range);
		final JLabel label = new JLabel();
		
		if (useSlider) {
			label.setText(title);
			
			slider = new TunableSlider(title, (Number)bounded.getLowerBound(), (Number)bounded.getUpperBound(),
			                      (Number)bounded.getValue(), bounded.isLowerBoundStrict(), bounded.isUpperBoundStrict(),
			                      format);
			slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					handle();
				}
			});
			
			updateFieldPanel(panel, label, slider, horizontal);
			panel.validate();
			
			setTooltip(getTooltip(), label, slider);
		} else {
			// Do something reasonable for max and min...
			// At some point, we should use superscripts for scientific notation...
			label.setText(
					title + " (max: " + format.format((Number)bounded.getLowerBound()) +
					" min: " + format.format((Number)bounded.getUpperBound()) + ")"
			);
			boundedField = new TunableBoundedField((Number)bounded.getValue(), (Number)bounded.getLowerBound(),
			                                  (Number)bounded.getUpperBound(), bounded.isLowerBoundStrict(),
			                                  bounded.isUpperBoundStrict());
			boundedField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handle();
				}
			});
			boundedField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					handle();
				}
			});
			
			updateFieldPanel(panel, label, boundedField, horizontal);
			panel.validate();
			
			setTooltip(getTooltip(), label, boundedField);
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

	@Override
	public void update(){
		try {
			final T bounded = getBounded();
			if (lastBounded != bounded) {
				lastBounded = bounded;
				lastBounded.addListener(this);
				panel.removeAll();
				initPanel(bounded);
			} else {
				if (useSlider) {
					Number n = (Number) bounded.getValue();
					slider.setValue(n);
				} else {
					boundedField = new TunableBoundedField((Number)bounded.getValue(), (Number)bounded.getLowerBound(),
					                                  (Number)bounded.getUpperBound(), bounded.isLowerBoundStrict(),
					                                  bounded.isUpperBoundStrict());
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * To set the value (from the JSlider or the JTextField) to the <code>Bounded</code> object
	 *
	 * The constraints of the bound values have to be respected : 
	 * <code>lowerBound &lt; value &lt; upperBound</code> or <code>lowerBound &lti; value &lti; upperBound</code> ....
	 */
	@Override
	public void handle() {
		try {
			final T bounded = getBounded();
			final Number fieldValue = useSlider ? slider.getValue() : boundedField.getFieldValue();
			
			if (fieldValue instanceof Double)
				bounded.setValue((Double)fieldValue);
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
	@Override
	public String getState() {
		try {
			return getBounded().getValue().toString();
		} catch (final Exception e) {
			return "";
		}
	}

	@Override
	public void boundsChanged(AbstractBounded changedObject) {
		if (changedObject == lastBounded) {
			panel.removeAll();
			initPanel(lastBounded);
		} else {
			((AbstractBounded)changedObject).removeListener(this);
		}
	}

	@Override
	public void valueChanged(AbstractBounded changedObject) {
		if (changedObject == lastBounded) {
			update();
		} else {
			((AbstractBounded)changedObject).removeListener(this);
		}
	}

	private DecimalFormat getDecimalFormat(double range) {
		if (getFormat() != null && getFormat().length() > 0)
			return new DecimalFormat(getFormat());
		if (range < 0.001 || range > 10000)
			return sdf;
		
		return df;
	}
}
