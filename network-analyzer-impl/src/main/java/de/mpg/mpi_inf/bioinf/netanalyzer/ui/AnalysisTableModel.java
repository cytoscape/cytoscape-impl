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

import java.awt.Dimension;
import java.io.File;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.AnalysisError;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkAnalysisReport;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInterpretation;

/**
 * Table model presenting a table of analysis reports.
 * <p>
 * This model contains three columns:
 * <ol>
 * <li><b>Network</b> The file from which the was loaded;</li>
 * <li><b>Interpretation</b> Network interpretation applied;</li>
 * <li><b>Network Statistics File</b> File in which the network statistics were saved.</li>
 * </ol>
 * </p>
 * 
 * @author Yassen Assenov
 */
public class AnalysisTableModel extends AbstractTableModel {

	/**
	 * Initializes a new instance of <code>AnalysisTableModel</code>.
	 * 
	 * @param aReports
	 *            List of reports that will fill the table.
	 */
	public AnalysisTableModel(List<NetworkAnalysisReport> aReports) {
		reports = aReports;
	}

	/**
	 * Sets preferred widths for columns in the table, as well as row height.
	 * 
	 * @param aTable
	 *            Table instance which uses <code>AnalysisTableModel</code> for managing its data.
	 *            If the table does not use <code>AnalysisTableModel</code>, the result of this
	 *            method is unspecified; in particular, the method might throw an instance of
	 *            <code>ClassCastException</code>, <code>IndexOutOfBoundsException</code> or
	 *            another exception type.
	 * 
	 * @throws NullPointerException
	 *             If <code>aTable</code> is <code>null</code>.
	 */
	public void adjustDimensions(final JTable aTable) {
		// Compute values for preferred width and height
		final TableColumnModel model = aTable.getColumnModel();
		final int cc = getColumnCount(); // = 3
		final int rc = getRowCount();
		final int[] widths = new int[cc];
		int height = 0;
		// Traverse the table header to set initial preferred widths
		final TableCellRenderer hr = aTable.getTableHeader().getDefaultRenderer();
		for (int i = 0; i < cc; ++i) {
			final Object header = model.getColumn(i).getHeaderValue();
			final Dimension size = getPreferredSize(hr, header);
			widths[i] = size.width;
		}
		// Traverse all table records
		for (int i = 0; i < cc; ++i) {
			final TableCellRenderer rend = aTable.getDefaultRenderer(getColumnClass(i));
			for (int r = 0; r < rc; ++r) {
				final int h = updateDimensions(aTable, widths, i, rend, getValueAt(r, i));
				if (height < h) {
					height = h;
				}
			}
		}
		// Reduce widths if necessary
		for (int i = 0; i < cc; ++i) {
			widths[i] += 4;
			widths[i] = Math.max(Math.min(widths[i], ColumnWidthsMax[i]), ColumnWidthsMin[i]);
		}
		// Set computed values for width and height
		for (int i = 0; i < cc; ++i) {
			final TableColumn c = model.getColumn(i);
			c.setPreferredWidth(widths[i]);
		}
		height += 2 + aTable.getRowMargin();
		aTable.setRowHeight(height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return NetworkInterpretation.class;
		case 2:
			return String.class;
		}
		throw new IndexOutOfBoundsException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return reports.size();
	}

	/**
	 * Gets the network file of the report on the given line.
	 * 
	 * @param index
	 *            Zero-based index of a table row which contains the report of interest.
	 * @return Network file for the report on row <code>index</code>.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             If <code>index</code> is negative or if it not less than {@link #getRowCount()}.
	 * 
	 * @see NetworkAnalysisReport#getNetwork()
	 */
	public File getNetwork(int index) {
		return reports.get(index).getNetwork();
	}

	/**
	 * Gets the statistics file of the report on the given line.
	 * 
	 * @param index
	 *            Zero-based index of a table row which contains the report of interest.
	 * @return Network statistics file for the report on row <code>index</code>.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             If <code>index</code> is negative or if it not less than {@link #getRowCount()}.
	 * 
	 * @see NetworkAnalysisReport#getResultFile()
	 */
	public File getNetstatsFile(int index) {
		return reports.get(index).getResultFile();
	}

	/**
	 * Get a hyperlink representation of a string.
	 * 
	 * @param aName
	 *            String to be represented as hyperlink.
	 * @return String represented as a hyperlink.
	 */
	private String getHrefName(String aName) {
		return "<html><a href=\"" + aName + "\">" + aName + "</a></html>";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int aRowIndex, int aColumnIndex) {
		switch (aColumnIndex) {
		case 0:
			final File network = getNetwork(aRowIndex);
			if (reports.get(aRowIndex).getError() == AnalysisError.NETWORK_NOT_OPENED) {
				return network.getName();
			}
			return getHrefName(network.getName());
		case 1:
			return reports.get(aRowIndex).getInterpretation();
		case 2:
			final File f = getNetstatsFile(aRowIndex);
			if (f != null) {
				return getHrefName(f.getName());
			}
			final AnalysisError error = reports.get(aRowIndex).getError();
			return AnalysisError.getMessage(error);
		}
		throw new IndexOutOfBoundsException();
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -9125782709654482796L;

	/**
	 * Maximum widths, in pixels, of the table columns.
	 */
	private static final int[] ColumnWidthsMax = new int[] { 300, 350, 450 };

	/**
	 * Minimum widths, in pixels, of the table columns.
	 */
	private static final int[] ColumnWidthsMin = new int[] { 100, 120, 140 };

	/**
	 * Names of the columns in this table model.
	 */
	private static String[] columnNames;

	static {
		columnNames = new String[3];
		columnNames[0] = Messages.DI_NETFILE;
		columnNames[1] = Messages.DI_INTERPR;
		columnNames[2] = Messages.DI_NETSTATSFILE;
	}

	private static Dimension getPreferredSize(JTable aTable, TableCellRenderer aRend, Object aValue) {
		return aRend.getTableCellRendererComponent(aTable, aValue, false, false, 0, 0)
				.getPreferredSize();
	}

	private static Dimension getPreferredSize(TableCellRenderer aRend, Object aValue) {
		return aRend.getTableCellRendererComponent(null, aValue, false, false, 0, 0)
				.getPreferredSize();
	}

	private int updateDimensions(JTable aTable, int[] aWidths, int aIndex, TableCellRenderer aRend,
			Object aValue) {
		final Dimension size = getPreferredSize(aTable, aRend, aValue);
		final int width = size.width;
		if (aWidths[aIndex] < width) {
			aWidths[aIndex] = width;
		}
		return size.height;
	}

	/**
	 * List of reports managed by this table model.
	 */
	private List<NetworkAnalysisReport> reports;
}
