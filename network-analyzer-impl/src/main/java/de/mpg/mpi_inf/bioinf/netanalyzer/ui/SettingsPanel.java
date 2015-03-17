package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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
import java.awt.Component;
import java.awt.GridBagLayout;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import de.mpg.mpi_inf.bioinf.netanalyzer.InnerException;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.PointShape;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.Settings;

/**
 * Panel to view and edit a visual settings group.
 * <p>
 * This panel presents all the editable properties of a given settings instance.
 * </p>
 * 
 * @author Yassen Assenov
 */
public class SettingsPanel extends JPanel {

	private static final long serialVersionUID = -610827874488404038L;

	/** Preferred width, in pixels, for labels spanning a whole column. */
	private static final int CHECKBOX_INDENT = 15;

	/** Preferred width, in pixels, for controls spanning a whole column. */
	private static final int CONTROL_COLUMN_WIDTH = 200;

	/**
	 * Horizontal spacing for every control.
	 * <p>
	 * Settings this field to <i>k</i> results in effective horizontal spacing of <i>2k</i> pixels between
	 * controls.
	 * </p>
	 */
	private static final int HOR_INSETS = 6;

	/**
	 * Vertical spacing for every control.
	 * <p>
	 * Settings this field to <i>k</i> results in effective vertical spacing of <i>2k</i> pixels between
	 * controls.
	 * </p>
	 */
	private static final int VER_INSETS = 4;

	/** Settings instance, whose properties are presented in this panel. */
	private Settings data;
	
	private GroupLayout layout;
	private ParallelGroup hLabelGroup;
	private ParallelGroup hControlGroup;
	private SequentialGroup vGroup;

	/**
	 * Initializes a new instance of <code>SettingsPanel</code>.
	 * 
	 * @param aSettings Settings instance, whose editable properties are to be presented in the panel.
	 * @param aIsDoubleBuffered Flag indicating if double buffering for this panel must be enabled.
	 */
	public SettingsPanel(final Settings aSettings, final boolean aIsDoubleBuffered) {
		super(new GridBagLayout(), aIsDoubleBuffered);
		
		data = aSettings;
		populate();
	}

	/**
	 * Initializes a new instance of <code>SettingsPanel</code>.
	 * <p>
	 * The panel initialized has double buffering enabled.
	 * </p>
	 * 
	 * @param aSettings Settings instance, whose editable properties are to be presented in the panel.
	 */
	public SettingsPanel(Settings aSettings) {
		this(aSettings, true);
	}

	@Override
	public void setEnabled(boolean enabled) {
		final Component[] comps = getComponents();
		
		for (int i = 0; i < comps.length; ++i) {
			comps[i].setEnabled(enabled);
		}
		
		super.setEnabled(enabled);
	}

	/**
	 * Gets the value of the given editable property.
	 * 
	 * @param aPropName Name of the property to get the value of.
	 * @return Value of the property as instance of <code>Boolean</code>, <code>Color</code>,
	 *         <code>PointShape</code> or <code>String</code>; <code>null</code> if the property could
	 *         not be found.
	 */
	public Object getValueOf(String aPropName) {
		final Component[] comps = getComponents();
		
		for (int i = 0; i < comps.length; ++i) {
			final Component c = comps[i];
			
			if (aPropName.equals(c.getName())) {
				if (c instanceof ColorButton) {
					return ((ColorButton)c).getColor();
				} else if (c instanceof JCheckBox) {
					return new Boolean(((JCheckBox) c).isSelected());
				} else if (c instanceof JTextField) {
					return ((JTextField) c).getText();
				} else if (c instanceof JComboBox) {
					return PointShape.parse(((JComboBox) c).getSelectedItem().toString());
				}
			}
		}
		return null;
	}

	/**
	 * Updates the values of the properties in the settings instance passed to this panel.
	 * <p>
	 * This method uses reflection to update the values of the properties of the settings instance.
	 * </p>
	 * 
	 * @throws InvocationTargetException If an internal error occurs.
	 */
	public void updateData() throws InvocationTargetException {
		final Method[] getters = Settings.propertyGetters(data);
		
		for (int i = 0; i < getters.length; ++i) {
			final Method setter = Settings.findSetter(getters[i]);
			
			if (setter == null)
				continue;
			
			final String propName = Settings.propertyName(getters[i]);
			final Object[] value = new Object[] { getValueOf(propName) };
			
			try {
				setter.invoke(data, value);
			} catch (InvocationTargetException ex) {
				throw ex;
			} catch (Exception ex) {
				// IllegalAccessException
				// IllegalArgumentException
				throw new InvocationTargetException(ex);
			}
		}
	}

	/**
	 * Populates the panel with all the controls for the editable properties.
	 * <p>
	 * This method is only called upon initialization.
	 * </p>
	 */
	private void populate() {
		layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(hLabelGroup = layout.createParallelGroup(Alignment.TRAILING, false))
				.addGroup(hControlGroup = layout.createParallelGroup(Alignment.LEADING, false))
		);
		layout.setVerticalGroup(vGroup = layout.createSequentialGroup());
		
		final Method[] getters = Settings.propertyGetters(data);
		
		for (int i = 0; i < getters.length; ++i) {
			final Method m = getters[i];
			
			if (Settings.findSetter(m) == null) {
				// Ignore read-only properties
				continue;
			}
			
			try {
				final Object value = m.invoke(data, (Object[]) null);
				final String propName = Settings.propertyName(m);
				final Class<?> propType = m.getReturnType();
				
				addControls(propName, propType, value);
			} catch (InnerException ex) {
				throw ex;
			} catch (Exception ex) {
				// InvocationTargetException
				// IllegalArgumentException
				// IllegalAccessException
				throw new InnerException(ex);
			}
		}
	}
	
	private void addToPanel(final JLabel label, final Component c) {
		hLabelGroup.addComponent(label);
		hControlGroup.addComponent(c);
		
		vGroup.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
				.addComponent(label)
				.addComponent(c)
		);
	}
	
	/**
	 * Adds a control or a pair of controls for presenting the given property.
	 * 
	 * @param aPropName Name of the property to be presented.
	 * @param aPropType Type of the property in the form of a <code>Class</code> instance.
	 * @param aValue Value of the property to be presented.
	 */
	private void addControls(String aPropName, Class<?> aPropType, Object aValue) {
		try {
			final String labelID = Messages.SET_PREFIX + aPropName.toUpperCase();
			final String labelText = (String) Messages.class.getField(labelID).get(null);
			final String typeName = aPropType.getName();
			
			final JLabel label = new JLabel(" ", SwingConstants.TRAILING);
			Component c = null;
			
			if (boolean.class.getName().equals(typeName)) {
				c = createBooleanInput(aPropName, labelText, (Boolean) aValue);
			} else {
				label.setText(labelText);
				
				if (String.class.getName().equals(typeName))
					c = createStringInput(aPropName, (String) aValue);
				else if (Color.class.getName().equals(typeName))
					c = createColorInput(aPropName, (Color) aValue);
				else if (PointShape.class.getName().equals(typeName))
					c = createPointShapeInput(aPropName, (PointShape) aValue);
			}
			
			if (c != null)
				addToPanel(label, c);
		} catch (Exception ex) {
			throw new InnerException(ex);
		}
	}

	/**
	 * Creates a checkbox to present a boolean property.
	 * 
	 * @param aPropName Name of the property to be presented.
	 * @param aButtonLabel Label to be used for the checkbox.
	 * @param aValue Value of the property to be presented. Settings this parameter to <code>true</code>
	 *        results in the checkbox being selected.
	 */
	private JCheckBox createBooleanInput(String aPropName, String aButtonLabel, Boolean aValue) {
		final JCheckBox checkBox = new JCheckBox(aButtonLabel);
		checkBox.setName(aPropName);
		checkBox.setSelected(aValue.booleanValue());
		
		return checkBox;
	}

	/**
	 * Creates a label and a text field to present a string property.
	 * 
	 * @param aPropName Name of the property to be presented.
	 * @param aValue Value of the property in the form of a <code>String</code> instance.
	 */
	private JTextField createStringInput(String aPropName, String aValue) {
		final JTextField inputField = new JTextField(aValue);
		inputField.setName(aPropName);
		Utils.adjustWidth(inputField, CONTROL_COLUMN_WIDTH);
		
		return inputField;
	}

	/**
	 * Creates a label and a button to present a <code>Color</code> property.
	 * 
	 * @param aPropName Name of the property to be presented.
	 * @param aColor Value of the property in the form of a <code>Color</code> instance.
	 */
	private ColorButton createColorInput(String aPropName, Color aColor) {
		final ColorButton colorButton = new ColorButton(aColor);
		colorButton.setActionCommand("color");
		colorButton.setName(aPropName);
		
		return colorButton;
	}

	/**
	 * Creates a drop-down list to present the possible values of a <code>PointShape</code> property.
	 * 
	 * @param aPropName Name of the property to be presented.
	 * @param aShape Value of the property in the form of a <code>PointShape</code> instance.
	 */
	private JComboBox<String> createPointShapeInput(String aPropName, PointShape aShape) {
		final JComboBox<String> comShapes = new JComboBox<>(PointShape.Texts);
		comShapes.setSelectedItem(aShape.getDescription());
		comShapes.setName(aPropName);
		
		return comShapes;
	}
}
