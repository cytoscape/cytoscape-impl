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

import java.io.File;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Cell renderer for instance of the {@link File} type.
 * <p>
 * This class is to be used for visualization of files (file names) in the {@link JTable} Swing control.
 * </p>
 * 
 * @author Yassen Assenov
 */
class FileCellRenderer extends DefaultTableCellRenderer {

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 6855862789588816843L;

	/**
	 * Initializes a new instance of <code>FileCellRenderer</code>.
	 */
	public FileCellRenderer() {
		// No fields to initialize
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableCellRenderer#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object value) {
		String text = "";
		if (value != null) {
			final File f = (File) value;
			final String name = f.getName();
			text = "<html><a href=\"" + name + "\">" + name + "</a></html>";
			setToolTipText(f.getAbsolutePath());
		}
		setText(text);
	}
}
