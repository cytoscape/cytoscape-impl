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
