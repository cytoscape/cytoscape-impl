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

import javax.swing.JTable;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInterpretation;

/**
 * Cell renderer for instance of the {@link NetworkInterpretation} type.
 * <p>
 * This class is to be used for visualization applied network interpretations in a {@link JTable} Swing
 * control.
 * </p>
 * 
 * @author Yassen Assenov
 */
public class InterpretationCellRenderer extends DefaultCellRenderer {

	/**
	 * Initializes a new instance of <code>InterpretationCellRenderer</code>.
	 */
	public InterpretationCellRenderer() {
		// No specific initialization is required.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableCellRenderer#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object value) {
		final StringBuilder text = new StringBuilder();
		text.append("<html>");
		if (value != null) {
			final NetworkInterpretation interpr = (NetworkInterpretation) value;
			if (interpr.isDirected()) {
				text.append(Messages.NI_R_DIR);
				if (interpr.isIgnoreUSL()) {
					text.append(Messages.NI_R_DIRL);
				}
			} else {
				text.append(Messages.NI_R_UNDIR);
				if (interpr.isPaired()) {
					text.append(Messages.NI_R_UNDIRC);
				}
			}
		}
		setText(text.toString());
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -4847632212033029150L;
}
