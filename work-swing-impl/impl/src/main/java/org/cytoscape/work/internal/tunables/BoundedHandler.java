package org.cytoscape.work.internal.tunables;


import java.awt.BorderLayout;
import java.awt.Font;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.internal.tunables.utils.myBoundedSwing;
import org.cytoscape.work.internal.tunables.utils.mySlider;
import org.cytoscape.work.util.AbstractBounded;


/**
 * Handler for the type <i>Bounded</i> of <code>Tunable</code>
 *
 * @author pasteur
 *
 * @param <T> type of <code>AbstractBounded</code>
 */
@SuppressWarnings("unchecked")
public class BoundedHandler<T extends AbstractBounded> extends AbstractGUITunableHandler {
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
		panel = new JPanel(new BorderLayout());

		try {
			final T bounded = getBounded();
			if (useSlider) {
				JLabel label = new JLabel(title);
				label.setFont(new Font(null, Font.PLAIN,12));
				panel.add(label,BorderLayout.WEST);
				slider = new mySlider(title, (Number)bounded.getLowerBound(), (Number)bounded.getUpperBound(),
				                      (Number)bounded.getValue(), bounded.isLowerBoundStrict(), bounded.isUpperBoundStrict());
				slider.addChangeListener(this);
				panel.add(slider,BorderLayout.EAST);
			} else {
				final JLabel label =
					new JLabel(title + " (max: " + bounded.getLowerBound().toString()
					          + " min: " + bounded.getUpperBound().toString() + ")" );
				label.setFont(new Font(null, Font.PLAIN,12));
				boundedField = new myBoundedSwing((Number)bounded.getValue(), (Number)bounded.getLowerBound(),
				                                  (Number)bounded.getUpperBound(), bounded.isLowerBoundStrict(),
				                                  bounded.isUpperBoundStrict());
				panel.add(label, BorderLayout.WEST);
				panel.add(boundedField, BorderLayout.EAST);
			}
		} catch (Exception e) {
			e.printStackTrace();
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

	/**
	 * To set the value (from the JSlider or the JTextField) to the <code>Bounded</code> object
	 *
	 * The constraints of the bound values have to be respected : <code>lowerBound &lt; value &lt; upperBound</code> or <code>lowerBound &lti; value &lti; upperBound</code> ....
	 */
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
				throw new IllegalStateException("unexpected type: " + fieldValue.getClass() + "!");
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
}
