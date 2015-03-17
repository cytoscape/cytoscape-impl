package de.mpg.mpi_inf.bioinf.netanalyzer.ui.filter;

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

import java.awt.Dialog;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import de.mpg.mpi_inf.bioinf.netanalyzer.InnerException;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Points2D;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.ComplexParamFilter;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.Points2DFilter;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.Points2DGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.SpringUtilities;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;

/**
 * Dialog for creating {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.Points2DFilter} based on user's
 * input.
 * 
 * @author Yassen Assenov
 */
public class Points2DFilterDialog extends ComplexParamFilterDialog implements PropertyChangeListener {

	private static final long serialVersionUID = 4746985046081748678L;

	/**
	 * Number formatting style used in the text fields.
	 */
	private static final NumberFormat formatter = NumberFormat.getInstance(Locale.US);
	
	/**
	 * Initializes a new instance of <code>Points2DFilterDialog</code> based on the given
	 * <code>Points2D</code> instance.
	 * 
	 * @param aOwner The <code>Dialog</code> from which this dialog is displayed.
	 * @param aTitle Title of the dialog.
	 * @param aPoints Data points, based on which the ranges for the minimum and maximum coordinate values are
	 *        to be chosen.
	 * @param aSettings Visual settings for <code>aPoints</code>.
	 */
	public Points2DFilterDialog(Dialog aOwner, String aTitle, Points2D aPoints, Points2DGroup aSettings) {
		super(aOwner, aTitle);

		populate(aPoints, aSettings);
		pack();
		setResizable(false);
		setLocationRelativeTo(aOwner);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		updateStatus();
	}

	@Override
	protected ComplexParamFilter createFilter() {
		try {
			final double xmin = formatter.parse(txfXMin.getText()).doubleValue();
			final double xmax = formatter.parse(txfXMax.getText()).doubleValue();
			return new Points2DFilter(xmin, xmax);
		} catch (ParseException ex) {
			throw new InnerException(ex);
		}
	}

	/**
	 * 
	 * @param aPoints Data points, based on which the ranges for the minimum and maximum coordinate values are
	 *        to be chosen.
	 * @param aSettings Visual settings for <code>aPoints</code>.
	 */
	private void populate(Points2D aPoints, Points2DGroup aSettings) {
		centralPane.setLayout(new SpringLayout());
		rangeX = aPoints.getRangeX();

		formatter.setParseIntegerOnly(false);
		formatter.setMaximumFractionDigits(12);

		// Add a text field for minimum x
		centralPane.add(new JLabel(aSettings.filter.getMinXLabel() + ":", SwingConstants.RIGHT));
		centralPane.add(txfXMin = new JFormattedTextField(formatter));
		final Dimension size = txfXMin.getPreferredSize();
		size.width = 70;
		txfXMin.setPreferredSize(size);
		txfXMin.setHorizontalAlignment(JFormattedTextField.RIGHT);
		txfXMin.setValue(new Double(xMin = rangeX[0]));
		txfXMin.addPropertyChangeListener("value", this);

		// Add a text field for maximum x
		centralPane.add(new JLabel(aSettings.filter.getMaxXLabel() + ":", SwingConstants.RIGHT));
		centralPane.add(txfXMax = new JFormattedTextField(formatter));
		txfXMax.setPreferredSize(size);
		txfXMax.setHorizontalAlignment(JFormattedTextField.RIGHT);
		txfXMax.setValue(new Double(xMax = rangeX[1]));
		txfXMax.addPropertyChangeListener("value", this);

		final int gap = Utils.BORDER_SIZE / 2;
		SpringUtilities.makeCompactGrid(centralPane, 2, 2, 0, 0, gap, gap);
	}

	/**
	 * Updates the status of the OK button based on the values entered in the text fields of this dialog.
	 */
	private void updateStatus() {
		try {
			xMin = formatter.parse(txfXMin.getText()).doubleValue();
			if (xMin < rangeX[0] || xMin > rangeX[1]) {
				xMin = Math.min(Math.max(xMin, rangeX[0]), rangeX[1]);
				txfXMin.setText(String.valueOf(xMin));
				txfXMin.commitEdit();
			}
		} catch (Exception ex) {
			xMin = Double.MAX_VALUE;
		}
		try {
			xMax = formatter.parse(txfXMax.getText()).doubleValue();
			if (xMax < rangeX[0] || xMax > rangeX[1]) {
				xMax = Math.min(Math.max(xMax, rangeX[0]), rangeX[1]);
				txfXMax.setText(String.valueOf(xMax));
				txfXMax.commitEdit();
			}
		} catch (Exception ex) {
			xMax = Double.MIN_VALUE;
		}
		btnOK.setEnabled(xMin <= xMax);
	}

	/**
	 * Text field for typing in the value for minimum x.
	 */
	private JFormattedTextField txfXMin;

	/**
	 * Text field for typing in the value for maximum x.
	 */
	private JFormattedTextField txfXMax;

	/**
	 * Range of allowed values to be typed for X. The element at index <code>0</code> in this array is the
	 * minimum, and the element at index <code>1</code> - the maximum.
	 */
	private double[] rangeX;

	/**
	 * New value to be used for minimum x, as typed by the user.
	 */
	private double xMin;

	/**
	 * New value to be used for maximum x, as typed by the user.
	 */
	private double xMax;
}
