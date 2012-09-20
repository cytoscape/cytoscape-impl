/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.tableimport.internal.reader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cytoscape.tableimport.internal.util.AttributeTypes;
import org.cytoscape.model.CyTable;

/**
 * Reader for Excel attribute workbook.<br>
 * This class creates string array and pass it to the AttributeLineParser.<br>
 *
 * <p>
 * This reader takes one sheet at a time.
 * </p>
 *
 * @version 0.7
 * @since Cytoscape 2.4
 * @author kono
 *
 */
public class ExcelAttributeSheetReader implements TextTableReader {
	private final Sheet sheet;
	private final AttributeMappingParameters mapping;
	private final AttributeLineParser parser;
	private final int startLineNumber;
	private int globalCounter = 0;

	private static final Logger logger = LoggerFactory.getLogger(ExcelAttributeSheetReader.class);
	
	
	/**
	 * Creates a new ExcelAttributeSheetReader object.
	 *
	 * @param sheet  DOCUMENT ME!
	 * @param mapping  DOCUMENT ME!
	 * @param startLineNumber  DOCUMENT ME!
	 * @param importAll  DOCUMENT ME!
	 */
	public ExcelAttributeSheetReader(final Sheet sheet,
	                                 final AttributeMappingParameters mapping){
		this.sheet = sheet;
		this.mapping = mapping;
		this.startLineNumber = mapping.getStartLineNumber();
		this.parser = new AttributeLineParser(mapping);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public List<String> getColumnNames() {
		return Arrays.asList(mapping.getAttributeNames());
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @throws IOException DOCUMENT ME!
	 */
	public void readTable(CyTable table) throws IOException {
		Row row;
		int rowCount = startLineNumber;
		String[] cellsInOneRow;

		while ((row = sheet.getRow(rowCount)) != null) {
			cellsInOneRow = createElementStringArray(row);
			try {
				//if(importAll)
					parser.parseAll(table, cellsInOneRow);
				//else 
				//	parser.parseEntry(table, cellsInOneRow);
			} catch (Exception ex) {
				logger.warn("Couldn't parse row: " + rowCount, ex);
			}
			
			rowCount++;
			globalCounter++;
		}
	}

	/**
	 * For a given Excell row, convert the cells into String.
	 *
	 * @param row
	 * @return
	 */
	private String[] createElementStringArray(Row row) {
		String[] cells = new String[mapping.getColumnCount()];
		Cell cell = null;

		for (short i = 0; i < mapping.getColumnCount(); i++) {
			cell = row.getCell(i);

			if (cell == null) {
				cells[i] = null;
			} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				cells[i] = cell.getRichStringCellValue().getString();
			} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				if (mapping.getAttributeTypes()[i] == AttributeTypes.TYPE_INTEGER) {
					Double dblValue = cell.getNumericCellValue();
					Integer intValue = dblValue.intValue();
					cells[i] = intValue.toString();
				} else {
					cells[i] = Double.toString(cell.getNumericCellValue());
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

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getReport() {
		final StringBuilder sb = new StringBuilder();
		final Map<String, Object> invalid = parser.getInvalidMap();
		sb.append(globalCounter + " entries are loaded and mapped into table.");

		int limit = 10;
		if (invalid.size() > 0) {
			sb.append("\n\nThe following enties are invalid and were not imported:\n");

			for (String key : invalid.keySet()) {
				sb.append(key + " = " + invalid.get(key) + "\n");
				if ( limit-- <= 0 ) {
					sb.append("Approximately " + (invalid.size() - 10) +
					          " additional entries were not imported...");
					break;
				}

			}
		}

		return sb.toString();
	}
	
	public MappingParameter getMappingParameter(){
		return mapping;
	}
}
