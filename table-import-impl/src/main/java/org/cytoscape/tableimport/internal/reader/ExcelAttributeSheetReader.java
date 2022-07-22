package org.cytoscape.tableimport.internal.reader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reader for Excel attribute workbook.<br>
 * This class creates string array and pass it to the AttributeLineParser.<br>
 *
 * <p>
 * This reader takes one sheet at a time.
 * </p>
 */
public class ExcelAttributeSheetReader implements TextTableReader {
	
	private final Sheet sheet;
	private final AttributeMappingParameters mapping;
	private final AttributeLineParser parser;
	private final DataFormatter formatter;
	private final FormulaEvaluator evaluator;
	private final int startLineNumber;
	private int globalCounter = 0;

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	
	public ExcelAttributeSheetReader(
			final Sheet sheet,
			final AttributeMappingParameters mapping,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.sheet = sheet;
		this.mapping = mapping;
		this.startLineNumber = mapping.getStartLineNumber();
		this.parser = new AttributeLineParser(mapping, serviceRegistrar);
		this.evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
		this.formatter = new DataFormatter();
	}
	
	private String formatCell(Cell cell, AttributeDataType cellDT) {
		if (cell == null) {
			return "";
		}
		
		CellType cellType = cell.getCellType();
		if (cellType == CellType.FORMULA) {
			if (evaluator == null) {
				return cell.getCellFormula();
			}
			cellType = evaluator.evaluateFormulaCell(cell);
		}
		switch (cellType) {
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return formatter.formatCellValue(cell, evaluator);
			}
			BigDecimal val = BigDecimal.valueOf(cell.getNumericCellValue());

			if (cellDT == AttributeDataType.TYPE_INTEGER) {
				return String.valueOf(val.intValue());
			}
			if (cellDT == AttributeDataType.TYPE_LONG) {
				return String.valueOf(val.longValue());
			}
			if (cellDT == AttributeDataType.TYPE_FLOATING) {
				return String.valueOf(val.doubleValue());
			}
      return formatter.formatCellValue(cell);
			// return val.toPlainString();

		case STRING:
			return cell.getRichStringCellValue().getString();
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case BLANK:
			return "";
		}

		return "";
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
	 */
	private String[] createElementStringArray(Row row) {
		String[] cells = new String[mapping.getColumnCount()];
		Cell cell = null;

		for (short i = 0; i < mapping.getColumnCount(); i++) {
			cell = row.getCell(i);

			if (cell == null || cell.getCellType() == CellType.ERROR || 
					(cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.ERROR)) {
				cells[i] = null;
			} else {
				cells[i] = formatCell(cell, mapping.getDataTypes()[i]);
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
