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

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;


import de.mpg.mpi_inf.bioinf.netanalyzer.data.LongHistogram;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.ComplexParamFilter;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.LongHistogramFilter;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.LongHistogramGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.SpringUtilities;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;

/**
 * Dialog for creating {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.LongHistogramFilter} based on
 * user's input.
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 */
public class LongHistogramFilterDialog extends ComplexParamFilterDialog {

	/**
	 * Initializes a new instance of <code>LongHistogramFilterDialog</code> based on the given
	 * IntHistgoram instance.
	 * 
	 * @param aOwner The <code>Dialog</code> from which this dialog is displayed.
	 * @param aTitle Title of the dialog.
	 * @param aHistogram LongHistogram instance, based on which the ranges for the minimum and maximum
	 *        degrees are to be chosen.
	 * @param aSettings Visual settings for <code>aHistogram</code>.
	 */
	public LongHistogramFilterDialog(Dialog aOwner, String aTitle, LongHistogram aHistogram,
		LongHistogramGroup aSettings) {
		super(aOwner, aTitle);

		populate(aHistogram, aSettings);
		pack();
		setResizable(false);
		setLocationRelativeTo(aOwner);
	}

	/**
	 * Creates and initializes a filter instance based on user's choice for minimum and maximum
	 * degree.
	 * 
	 * @return Instance of <code>LongHistogramFilter</code> reflecting the user's filtering
	 *         criteria.
	 */
	@Override
	protected ComplexParamFilter createFilter() {
		return new LongHistogramFilter(Utils.getSpinnerInt(spnMin), Utils.getSpinnerInt(spnMax));
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 1556691517305385646L;

	/**
	 * Creates and lays out the two spinner controls for choosing minimum and maximum degree.
	 * 
	 * @param aHistogram Histogram instance, based on which the ranges for the minimum and maximum
	 *        degrees are to be chosen.
	 * @param aSettings Visual settings for <code>aHistogram</code>.
	 */
	private void populate(LongHistogram aHistogram, LongHistogramGroup aSettings) {
		centralPane.setLayout(new SpringLayout());
		long[] range = aHistogram.getObservedRange();

		// Add a spinner for minimum observation
		centralPane.add(new JLabel(aSettings.filter.getMinObservationLabel() + ":", SwingConstants.RIGHT));
		SpinnerModel minSettings = new SpinnerNumberModel(range[0], range[0], range[1], 1);
		centralPane.add(spnMin = new JSpinner(minSettings));

		// Add a spinner for maximum observation
		centralPane.add(new JLabel(aSettings.filter.getMaxObservationLabel() + ":", SwingConstants.RIGHT));
		SpinnerModel maxSettings = new SpinnerNumberModel(range[1], range[0], range[1], 1);
		centralPane.add(spnMax = new JSpinner(maxSettings));

		final int gap = Utils.BORDER_SIZE / 2;
		SpringUtilities.makeCompactGrid(centralPane, 2, 2, 0, 0, gap, gap);
	}

	/**
	 * Spinner to choose the maximum observation value to display.
	 */
	private JSpinner spnMin;

	/**
	 * Spinner to choose the maximum observation value to display.
	 */
	private JSpinner spnMax;
}
