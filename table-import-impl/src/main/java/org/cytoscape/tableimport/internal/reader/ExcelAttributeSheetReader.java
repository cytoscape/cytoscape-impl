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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cytoscape.tableimport.internal.util.AttributeDataType;
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
	 */
	public ExcelAttributeSheetReader(final Sheet sheet,
	                                 final AttributeMappingParameters mapping){
		this.sheet = sheet;
		this.mapping = mapping;
		this.startLineNumber = mapping.getStartLineNumber();
		this.parser = new AttributeLineParser(mapping);
	}

	@Override
	public List<String> getColumnNames() {
		return Arrays.asList(mapping.getAttributeNames());
	}

	@Override
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
	 * For a given Excel row, convert the cells into String.
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
				if (mapping.getAttributeTypes()[i] == AttributeDataType.TYPE_INTEGER) {
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

	@Override
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
	
	@Override
	public MappingParameter getMappingParameter(){
		return mapping;
	}
}
