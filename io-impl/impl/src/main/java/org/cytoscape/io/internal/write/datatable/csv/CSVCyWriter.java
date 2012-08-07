package org.cytoscape.io.internal.write.datatable.csv;


import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTable.Mutability;
import org.cytoscape.work.TaskMonitor;

import au.com.bytecode.opencsv.CSVWriter;


public class CSVCyWriter implements CyWriter {
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

	private void writeSchema(CSVWriter writer, List<CyColumn> columns) {
		String[] values = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++) {
			CyColumn column = columns.get(i);
			Class<?> type = column.getType();
			if (List.class.isAssignableFrom(type)) {
				values[i] = String.format("%s<%s>", List.class.getCanonicalName(), column.getListElementType().getCanonicalName());
			} else {
				values[i] = type.getCanonicalName();
			}
		}
		writer.writeNext(values);
		values = new String[2];
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
					if (rawValue instanceof String
					    && ((String)rawValue).startsWith("="))
					{
						values[index++] = (String)rawValue;
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
								builder.append("\r");
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
