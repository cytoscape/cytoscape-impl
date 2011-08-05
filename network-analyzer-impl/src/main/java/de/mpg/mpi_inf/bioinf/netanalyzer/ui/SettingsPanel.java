/*
 * Copyright (c) 2006, 2007, 2008, 2010, Max Planck Institute for Informatics, Saarbruecken, Germany.
 *
 * This file is part of NetworkAnalyzer.
 * 
 * NetworkAnalyzer is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * NetworkAnalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with NetworkAnalyzer. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
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
public class SettingsPanel extends JPanel implements ActionListener {

	/**
	 * Initializes a new instance of <code>SettingsPanel</code>.
	 * 
	 * @param aSettings Settings instance, whose editable properties are to be presented in the panel.
	 * @param aIsDoubleBuffered Flag indicating if double buffering for this panel must be enabled.
	 */
	public SettingsPanel(Settings aSettings, boolean aIsDoubleBuffered) {
		super(new GridBagLayout(), aIsDoubleBuffered);

		initLayout();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if ("color".equals(e.getActionCommand())) {
			JButton invoker = (JButton) e.getSource();
			Color newColor = JColorChooser.showDialog(this, Messages.DI_SELECTCOLOR, invoker.getForeground());
			if (newColor != null) {
				invoker.setForeground(newColor);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		Component[] comps = getComponents();
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
		Component[] comps = getComponents();
		for (int i = 0; i < comps.length; ++i) {
			Component c = comps[i];
			if (aPropName.equals(c.getName())) {
				if (c instanceof JButton) {
					if ("color".equals(((JButton) c).getActionCommand())) {
						return c.getForeground();
					}
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
		Method[] getters = Settings.propertyGetters(data);
		for (int i = 0; i < getters.length; ++i) {
			Method setter = Settings.findSetter(getters[i]);
			if (setter == null) {
				continue;
			}
			String propName = Settings.propertyName(getters[i]);
			Object[] value = new Object[] { getValueOf(propName) };
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
	 * Initializes the properties of the <code>GridBagLayout</code> used by this panel.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 */
	private void initLayout() {
		layout = (GridBagLayout) getLayout();
		firstColumn = new GridBagConstraints();
		firstColumn.anchor = GridBagConstraints.LINE_END;
		firstColumn.weightx = firstColumn.weighty = 0.2;
		firstColumn.fill = GridBagConstraints.NONE;
		firstColumn.insets = new Insets(VER_INSETS, HOR_INSETS, VER_INSETS, HOR_INSETS);
		lastColumn = new GridBagConstraints();
		lastColumn.anchor = GridBagConstraints.LINE_START;
		lastColumn.weightx = lastColumn.weighty = 1.0;
		lastColumn.fill = GridBagConstraints.HORIZONTAL;
		lastColumn.insets = new Insets(VER_INSETS, HOR_INSETS, VER_INSETS, HOR_INSETS);
		lastColumn.gridwidth = GridBagConstraints.REMAINDER;
	}

	/**
	 * Populates the panel with all the controls for the editable properties.
	 * <p>
	 * This method is only called upon initialization.
	 * </p>
	 */
	private void populate() {
		final Method[] getters = Settings.propertyGetters(data);
		for (int i = 0; i < getters.length; ++i) {
			Method m = getters[i];
			if (Settings.findSetter(m) == null) {
				// Ignore read-only properties
				continue;
			}
			try {
				Object value = m.invoke(data, (Object[]) null);
				String propName = Settings.propertyName(m);
				Class<?> propType = m.getReturnType();
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

	/**
	 * Adds a control or a pair of controls for presenting the given property.
	 * 
	 * @param aPropName Name of the property to be presented.
	 * @param aPropType Type of the property in the form of a <code>Class</code> instance.
	 * @param aValue Value of the property to be presented.
	 */
	private void addControls(String aPropName, Class<?> aPropType, Object aValue) {
		try {
			String labelID = Messages.SET_PREFIX + aPropName.toUpperCase();
			String labelText = (String) Messages.class.getField(labelID).get(null);
			String typeName = aPropType.getName();
			if (boolean.class.getName().equals(typeName)) {
				addBooleanInput(aPropName, labelText, (Boolean) aValue);
			} else {
				JLabel label = new JLabel(labelText, SwingConstants.TRAILING);
				add(label, firstColumn);
				if (String.class.getName().equals(typeName)) {
					addStringInput(aPropName, (String) aValue);
				} else if (Color.class.getName().equals(typeName)) {
					addColorInput(aPropName, (Color) aValue);
				} else if (PointShape.class.getName().equals(typeName)) {
					addPointShapeInput(aPropName, (PointShape) aValue);
				}
			}
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
	private void addBooleanInput(String aPropName, String aButtonLabel, Boolean aValue) {
		JCheckBox checkBox = new JCheckBox(aButtonLabel);
		checkBox.setName(aPropName);
		checkBox.setSelected(aValue.booleanValue());
		lastColumn.fill = GridBagConstraints.NONE;
		lastColumn.insets.left += CHECKBOX_INDENT;
		lastColumn.insets.right += CHECKBOX_INDENT;
		add(checkBox, lastColumn);
		lastColumn.insets.left -= CHECKBOX_INDENT;
		lastColumn.insets.right -= CHECKBOX_INDENT;
		lastColumn.fill = GridBagConstraints.HORIZONTAL;
	}

	/**
	 * Creates a label and a text field to present a string property.
	 * 
	 * @param aPropName Name of the property to be presented.
	 * @param aValue Value of the property in the form of a <code>String</code> instance.
	 */
	private void addStringInput(String aPropName, String aValue) {
		JTextField inputField = new JTextField(aValue);
		inputField.setName(aPropName);
		Utils.adjustWidth(inputField, CONTROL_COLUMN_WIDTH);
		layout.setConstraints(inputField, lastColumn);
		add(inputField);
	}

	/**
	 * Creates a label and a button to present a <code>Color</code> property.
	 * 
	 * @param aPropName Name of the property to be presented.
	 * @param aColor Value of the property in the form of a <code>Color</code> instance.
	 */
	private void addColorInput(String aPropName, Color aColor) {
		final JButton colorButton = new JButton(Messages.SET_COLORBUTTON);
		colorButton.addActionListener(this);
		colorButton.setActionCommand("color");
		colorButton.setForeground(aColor);
		colorButton.setName(aPropName);
		lastColumn.fill = GridBagConstraints.NONE;
		add(colorButton, lastColumn);
		lastColumn.fill = GridBagConstraints.HORIZONTAL;
	}

	/**
	 * Creates a drop-down list to present the possible values of a <code>PointShape</code> property.
	 * 
	 * @param aPropName Name of the property to be presented.
	 * @param aShape Value of the property in the form of a <code>PointShape</code> instance.
	 */
	private void addPointShapeInput(String aPropName, PointShape aShape) {
		final JComboBox comShapes = new JComboBox(PointShape.Texts);
		comShapes.setSelectedItem(aShape.getDescription());
		comShapes.setName(aPropName);
		lastColumn.fill = GridBagConstraints.NONE;
		add(comShapes, lastColumn);
		lastColumn.fill = GridBagConstraints.HORIZONTAL;
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -610827874488404038L;

	/**
	 * Preferred width, in pixels, for labels spanning a whole column.
	 */
	private static final int CHECKBOX_INDENT = 15;

	/**
	 * Preferred width, in pixels, for controls spanning a whole column.
	 */
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

	/**
	 * Layout manager for the controls in the panel.
	 */
	private GridBagLayout layout;

	/**
	 * Constraints for the first column of the grid. This column contains labels.
	 */
	private GridBagConstraints firstColumn;

	/**
	 * Constraints for the last (second) column of the grid. This column contains input controls, such as text
	 * boxes and buttons.
	 */
	private GridBagConstraints lastColumn;

	/**
	 * Settings instance, whose properties are presented in this panel.
	 */
	private Settings data;
}
