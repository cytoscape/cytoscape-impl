package org.cytoscape.io.internal.write.datatable.csv;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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


import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cytoscape.equations.Equation;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTable.Mutability;
import org.cytoscape.work.TaskMonitor;

import au.com.bytecode.opencsv.CSVWriter;


public class CSVCyWriter implements CyWriter {
	private static final int SCHEMA_VERSION = 1;
	private final OutputStream outputStream;
	private final CyTable table;
	private final boolean writeSchema;
	private boolean isCanceled;
	private final boolean handleEquations;
	private final boolean includeVirtualColumns;
	private String encoding;

	public CSVCyWriter(final OutputStream outputStream, final CyTable table,
			   final boolean writeSchema, final boolean handleEquations, final boolean includeVirtualColumns, final String encoding)
	{
		this.outputStream    = outputStream;
		this.table           = table;
		this.writeSchema     = writeSchema;
		this.handleEquations = handleEquations;
		this.includeVirtualColumns = includeVirtualColumns;
		this.encoding = encoding;
	}

	@Override
	public void cancel() {
		isCanceled = true;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, encoding), ',', '"', "\r\n");
		try {
			List<CyColumn> columns = new ArrayList<CyColumn>();
			for (CyColumn column : table.getColumns()) {
				if (column.getVirtualColumnInfo().isVirtual()) {
					if (!includeVirtualColumns)
						continue;
				}
				columns.add(column);
			}
			taskMonitor.setProgress(0.2);
			Collections.sort(columns, new Comparator<CyColumn>() {
				@Override
				public int compare(CyColumn o1, CyColumn o2) {
					// First column should be primary key
					if (o1.isPrimaryKey()) {
						return -1;
					}
					if (o2.isPrimaryKey()) {
						return 1;
					}
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});
			taskMonitor.setProgress(0.4);
			if (writeSchema) {
				writeVersion(writer);
			}
			writeHeader(writer, columns);
			if (writeSchema) {
				writeSchema(writer, columns);
			}
			taskMonitor.setProgress(0.6);
			writeValues(writer, columns);
		} finally {
			writer.flush();
		}
		taskMonitor.setProgress(1.0);
	}

	private void writeVersion(CSVWriter writer) {
		writer.writeNext(new String[] { "CyCSV-Version", String.valueOf(SCHEMA_VERSION) });
	}

	private void writeSchema(CSVWriter writer, List<CyColumn> columns) {
		String[] types = new String[columns.size()];
		String[] options = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++) {
			CyColumn column = columns.get(i);
			Class<?> type = column.getType();
			if (List.class.isAssignableFrom(type)) {
				types[i] = String.format("%s<%s>", List.class.getCanonicalName(), column.getListElementType().getCanonicalName());
			} else {
				types[i] = type.getCanonicalName();
			}
			options[i] = column.isImmutable() ? "" : "mutable";
		}
		writer.writeNext(types);
		writer.writeNext(options);
		
		String[] values = new String[2];
		values[0] = table.getTitle();

		StringBuilder builder = new StringBuilder();
		if (table.isPublic()) {
			builder.append("public");
		}
		if (table.getMutability() == Mutability.MUTABLE) {
			if (builder.length() > 0) {
				builder.append(",");
			}
			builder.append("mutable");
		}
		values[1] = builder.toString();
		writer.writeNext(values);
	}

	private void writeValues(CSVWriter writer, Collection<CyColumn> columns) {
		for (CyRow row : table.getAllRows()) {
			if (isCanceled)
				return;

			String[] values = new String[columns.size()];
			int index = 0;
			for (CyColumn column : columns) {
				if (handleEquations) {
					final Object rawValue = row.getRaw(column.getName());
					if (rawValue instanceof Equation) {
						values[index++] = rawValue.toString();
						continue;
					}
				}

				Class<?> type = column.getType();
				if (type.equals(List.class)) {
					StringBuilder builder = new StringBuilder();
					boolean first = true;
					List<?> list = row.getList(column.getName(), column.getListElementType());
					if (list != null) {
						for (Object value : list) {
							if (!first) {
								builder.append(" ");
							}
							if (value != null) {
								builder.append(value);
							}
							first = false;
						}
						values[index] = builder.toString();
					}
				} else {
					Object value = row.get(column.getName(), type);
					if (value != null) {
						values[index] = value.toString();
					} else {
						values[index] = null;
					}
				}
				index++;
			}
			writer.writeNext(values);
		}
	}

	private void writeHeader(CSVWriter writer, List<CyColumn> columns) {
		String[] values = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++) {
			values[i] = columns.get(i).getName();
		}
		writer.writeNext(values);
	}
}
