package org.cytoscape.tableimport.internal.reader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.tableimport.internal.util.AttributeTypes;
import org.cytoscape.model.subnetwork.CyRootNetwork;

/**
 * Reader for Network file in Excel (.xls) format.<br>
 *
 * <p>
 * Currently supports only one sheet.
 * </p>
 *
 * @since Cytoscape 2.4
 * @version 0.6
 * @author Keiichiro Ono
 */
public class ExcelNetworkSheetReader extends NetworkTableReader {
	private final Sheet sheet;
	private static final Logger logger = LoggerFactory.getLogger(ExcelNetworkSheetReader.class);
	//private Map<Object, CyNode> nMap;
	
	/*
	 * Reader will read entries from this line.
	 */
	/**
	 * Creates a new ExcelNetworkSheetReader object.
	 *
	 * @param networkName  DOCUMENT ME!
	 * @param sheet  DOCUMENT ME!
	 * @param nmp  DOCUMENT ME!
	 */
	public ExcelNetworkSheetReader(final String networkName, final Sheet sheet,
	                               final NetworkTableMappingParameters nmp, 
	                               final Map<Object, CyNode> nMap, final CyRootNetwork rootNetwork) {
		super(networkName, null, nmp, nMap, rootNetwork);
		this.sheet = sheet;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @throws IOException DOCUMENT ME!
	 */
	@Override
	public void readTable(CyTable table) throws IOException {
		network.getRow(network).set("name", this.getNetworkName());		
		parser.setNetwork(network);

		Row row;
		int rowCount = startLineNumber;
		String[] cellsInOneRow;

		while ((row = sheet.getRow(rowCount)) != null) {
			cellsInOneRow = createElementStringArray(row);
			try {
				parser.parseEntry(cellsInOneRow);
			} catch (Exception e) {
				logger.warn("Couldn't parse row: " + rowCount, e);
			}
			rowCount++;
		}
	}

	/**
	 * For a given Excel row, convert the cells into String.
	 *
	 * @param row
	 * @return
	 */
	private String[] createElementStringArray(final Row row) {
		if (nmp.getColumnCount() == -1)
			return null;
		String[] cells = new String[nmp.getColumnCount()];
		Cell cell;

		for (short i = 0; i < nmp.getColumnCount(); i++) {
			cell = row.getCell(i);

			if (cell == null) {
				cells[i] = null;
			} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				cells[i] = cell.getRichStringCellValue().getString();
			} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				if (nmp.getAttributeTypes()[i] == AttributeTypes.TYPE_INTEGER) {
					Double dblValue = cell.getNumericCellValue();
					Integer intValue = dblValue.intValue();
					cells[i] = intValue.toString();
				} else {
					cells[i] = convertDoubleToString(cell.getNumericCellValue());
				}
			} else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
				cells[i] = Boolean.toString(cell.getBooleanCellValue());
			} else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
				cells[i] = null;
			} else if (cell.getCellType() == Cell.CELL_TYPE_ERROR) {
				cells[i] = null;
				logger.warn("Error found when reading a cell.");
			}
		}

		return cells;
	}

	private static String convertDoubleToString(Double v)
	{
		BigDecimal bd = new BigDecimal(v);
		try
		{
			BigInteger bi = bd.toBigIntegerExact();
			return bi.toString();
		}
		catch( ArithmeticException e )
		{
			return v.toString();
		}
	}

}
