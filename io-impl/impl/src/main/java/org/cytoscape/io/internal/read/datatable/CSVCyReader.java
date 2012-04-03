package org.cytoscape.io.internal.read.datatable;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.work.TaskMonitor;

import au.com.bytecode.opencsv.CSVReader;


public class CSVCyReader implements CyTableReader {
	private final static Pattern classPattern = Pattern.compile("([^<>]+)(<(.*?)>)?");

	private final InputStream stream;
	private final boolean readSchema;
	private final boolean handleEquations;
	private final CyTableFactory tableFactory;
	private final EquationCompiler compiler;

	private boolean isCanceled;
	private CyTable table;

	public CSVCyReader(final InputStream stream, final boolean readSchema,
			   final boolean handleEquations, final CyTableFactory tableFactory,
			   final EquationCompiler compiler)
	{
		this.stream          = stream;
		this.readSchema      = readSchema;
		this.handleEquations = handleEquations;
		this.tableFactory    = tableFactory;
		this.compiler        = compiler;
	}

	@Override
	public void cancel() {
		isCanceled = true;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);

		CSVReader reader = new CSVReader(new InputStreamReader(stream));
		taskMonitor.setProgress(0.2);

		TableInfo info = readHeader(reader);
		table = createTable(reader, info);
		taskMonitor.setProgress(1.0);
	}

	CyTable createTable(CSVReader reader, TableInfo info) throws IOException, SecurityException {
		final ColumnInfo[] columns = info.getColumns();
		final CyTable table = tableFactory.createTable(info.getTitle(), columns[0].getName(),
		                                               columns[0].getType(), info.isPublic(),
		                                               true);

		final Map<String, Class<?>> variableNameToTypeMap = new HashMap<String, Class<?>>();
		for (final ColumnInfo colInfo : columns)
			variableNameToTypeMap.put(colInfo.getName(), colInfo.getType());

		for (int i = 1; i < columns.length; i++) {
			ColumnInfo column = columns[i];
			Class<?> type = column.getType();
			if (type.equals(List.class)) {
				table.createListColumn(column.getName(), column.getListElementType(), !column.isMutable());
			} else {
				table.createColumn(column.getName(), type, !column.isMutable());
			}
		}
		String[] values = reader.readNext();
		while (values != null) {
			if (isCanceled)
				return null;

			Object key = parseValue(columns[0].getType(), null, values[0]);
			CyRow row = table.getRow(key);
			for (int i = 1; i < values.length; i++) {
				ColumnInfo column = columns[i];
				String name = column.getName();
				if (handleEquations && values[i].startsWith("=")) {
					final Class<?> type = variableNameToTypeMap.remove(name);
					try {
						if (!compiler.compile(values[i],
								      variableNameToTypeMap))
							throw new IOException("Error while reading \""
									      + info.getTitle()
									      + "\" cant compile equation because: "
									      + compiler.getLastErrorMsg());
						final Equation equation = compiler.getEquation();
						row.set(name, equation);
					} catch (final Exception e) {
						throw new IOException(e.getMessage(), e.getCause());
					}
					variableNameToTypeMap.put(name, type);
				} else {
					Object value = parseValue(column.getType(), column.getListElementType(), values[i]);
					if (value != null)
						row.set(name, value);
				}
			}
			values = reader.readNext();
		}
		return table;
	}

	Object parseValue(Class<?> type, Class<?> listElementType, String value) {
		if (type.equals(List.class)) {
			List<Object> list = new ArrayList<Object>();
			String[] values = value.split("\n");
			for (String item : values) {
				list.add(parseValue(listElementType, null, item));
			}
			return list;
		} else if (type.equals(String.class))
			return value;
		else {
			try {
				Method method = type.getMethod("valueOf", String.class);
				return method.invoke(null, value);
			} catch (Exception e) {
				return null;
			}
		}
	}

	TableInfo readHeader(CSVReader reader) throws IOException, ClassNotFoundException {
		String[] values = reader.readNext();
		TableInfo table = new TableInfo();
		ColumnInfo[] columns = new ColumnInfo[values.length];
		for (int i = 0; i < values.length; i++) {
			ColumnInfo column = new ColumnInfo();
			column.setName(values[i]);
			columns[i] = column;
		}
		table.setColumns(columns);
		if (!readSchema) {
			return table;
		}
		values = reader.readNext();
		for (int i = 0; i < values.length; i++) {
			Matcher matcher = classPattern.matcher(values[i]);
			matcher.matches();
			String typeName = matcher.group(1);
			Class<?> type = Class.forName(typeName);
			if (type.equals(List.class)) {
				String elementName = matcher.group(3);
				Class<?> elementType = Class.forName(elementName);
				columns[i].setListElementType(elementType);
			} else {
				columns[i].setType(type);
			}
		}
		values = reader.readNext();
		table.setTitle(values[0]);
		for (String option : values[1].split(",")) {
			if ("public".equals(option)) {
				table.setPublic(true);
			} else if ("mutable".equals(option)) {
				table.setMutable(true);
			}
		}
		return table;
	}

	@Override
	public CyTable[] getTables() {
		if (table == null) {
			return null;
		}
		return new CyTable[] { table };
	}

}
