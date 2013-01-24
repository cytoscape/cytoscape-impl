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
