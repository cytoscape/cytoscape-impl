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
import org.cytoscape.equations.EquationUtil;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;

import au.com.bytecode.opencsv.CSVReader;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class CSVCyReader implements CyTableReader {
	
	private final static Pattern classPattern = Pattern.compile("([^<>]+)(<(.*?)>)?");

	private final InputStream stream;
	private final boolean readSchema;
	private final boolean handleEquations;
	private final String encoding;

	private boolean isCanceled;
	private CyTable table;
	
	private final CyServiceRegistrar serviceRegistrar;

	public CSVCyReader(
			final InputStream stream,
			final boolean readSchema,
			final boolean handleEquations, 
			final String encoding,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.stream = stream;
		this.readSchema = readSchema;
		this.handleEquations = handleEquations;
		this.encoding = encoding;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void cancel() {
		isCanceled = true;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);

		CSVReader reader = new CSVReader(new InputStreamReader(stream, encoding), ',', '"', '\0');
		taskMonitor.setProgress(0.2);

		TableInfo info = readHeader(reader);
		table = createTable(reader, info);
		taskMonitor.setProgress(1.0);
	}

	CyTable createTable(CSVReader reader, TableInfo info) throws IOException, SecurityException {
		final ColumnInfo[] columns = info.getColumns();
		final CyTableFactory tableFactory = serviceRegistrar.getService(CyTableFactory.class);
		
		final CyTable table = tableFactory.createTable(info.getTitle(), columns[0].getName(),
		                                               columns[0].getType(), info.isPublic(),
		                                               true);

		final Map<String, Class<?>> variableNameToTypeMap = new HashMap<>();
		
		for (final ColumnInfo colInfo : columns)
			variableNameToTypeMap.put(colInfo.getName(), colInfo.getType() == Integer.class ? Long.class : colInfo.getType());

		for (int i = 1; i < columns.length; i++) {
			ColumnInfo column = columns[i];
			Class<?> type = column.getType();
			
			if (type.equals(List.class)) {
				table.createListColumn(column.getName(), column.getListElementType(), !column.isMutable());
			} else {
				table.createColumn(column.getName(), type, !column.isMutable());
			}
		}
		
		final EquationCompiler compiler = serviceRegistrar.getService(EquationCompiler.class);
		String[] values = reader.readNext();
		
		while (values != null) {
			if (isCanceled)
				return null;

			Object key = parseValue(columns[0].getType(), null, values[0]);
			CyRow row = table.getRow(key);
			
			for (int i = 1; i < values.length; i++) {
				ColumnInfo column = columns[i];
				String name = column.getName();
				final Class<?> columnType = column.getType();
				final Class<?> columnListElementType = column.getListElementType();
				
				if (handleEquations && values[i].startsWith("=")) {
					final Class<?> expectedType = variableNameToTypeMap.remove(name);
					
					try {
						final Equation equation;
						final Class<?> eqnType;
						
						if (compiler.compile(values[i], variableNameToTypeMap)) {
							eqnType = compiler.getEquation().getType();
							if(EquationUtil.eqnTypeIsCompatible(columnType, columnListElementType, eqnType))
								equation = compiler.getEquation();
							else {
								final String errorMsg = "Equation result type is "
									+ EquationUtil.getUnqualifiedName(eqnType) + ", column type is "
									+ EquationUtil.getUnqualifiedName(columnType) + ".";
								equation = compiler.getErrorEquation(values[i], expectedType, errorMsg);
							}
						} else {
							equation = compiler.getErrorEquation(values[i], expectedType, compiler.getLastErrorMsg());
						}
						
						row.set(name, equation);
					} catch (final Exception e) {
						throw new IOException(e.getMessage(), e.getCause());
					}
					
					variableNameToTypeMap.put(name, expectedType);
				} else {
					Object value = parseValue(columnType, columnListElementType, values[i]);
					
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
			List<Object> list = new ArrayList<>();
			String[] values = value.split("\n");
			
			for (String item : values) {
				list.add(parseValue(listElementType, null, item));
			}

			if (list.size() == 1 && list.get(0) == null) 
				return null;

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
		int schemaVersion;
		if (values.length == 2 && "CyCSV-Version".equals(values[0])) {
			schemaVersion = Integer.parseInt(values[1]);
			values = reader.readNext();
		} else {
			schemaVersion = 0;
		}
		
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
		
		SchemaDelegate delegate = getSchemaDelegate(schemaVersion);
		delegate.readSchema(reader, table);
		
		return table;
	}

	private SchemaDelegate getSchemaDelegate(int schemaVersion) {
		switch (schemaVersion) {
		case 0:
			return new SchemaDelegate0();
		case 1:
			return new SchemaDelegate1();
		default:
			throw new IllegalArgumentException("Unsupported CyCSV version: " + schemaVersion);
		}
	}

	@Override
	public CyTable[] getTables() {
		if (table == null) {
			return null;
		}
		return new CyTable[] { table };
	}

	static interface SchemaDelegate {
		void readSchema(CSVReader reader, TableInfo table) throws IOException, ClassNotFoundException;
	}
	
	static class AbstractSchemaDelegate implements SchemaDelegate {
		@Override
		public void readSchema(CSVReader reader, TableInfo table) throws IOException, ClassNotFoundException {
			handleColumnTypes(reader, table);
			handleColumnOptions(reader, table);
			handleTableOptions(reader, table);
		}

		protected void handleTableOptions(CSVReader reader, TableInfo table) throws IOException {
			String[] values = reader.readNext();
			table.setTitle(values[0]);
			for (String option : values[1].split(",")) {
				if ("public".equals(option)) {
					table.setPublic(true);
				} else if ("mutable".equals(option)) {
					table.setMutable(true);
				}
			}
		}

		protected void handleColumnOptions(CSVReader reader, TableInfo table) throws IOException, ClassNotFoundException {
			ColumnInfo[] columns = table.getColumns();
			String[] values = reader.readNext();
			for (int i = 0; i < values.length; i++) {
				for (String option : values[i].split(",")) {
					if ("mutable".equals(option)) {
						columns[i].setMutable(true);
					}
				}
			}
		}

		protected void handleColumnTypes(CSVReader reader, TableInfo table) throws IOException, ClassNotFoundException {
			ColumnInfo[] columns = table.getColumns();
			String[] values = reader.readNext();
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
		}
	}
	
	static class SchemaDelegate0 extends AbstractSchemaDelegate {
		@Override
		protected void handleColumnOptions(CSVReader reader, TableInfo table) throws IOException, ClassNotFoundException {
			// Make columns except primary key mutable by default so users
			// won't run into issue #1526.
			ColumnInfo[] columns = table.getColumns();
			for (int i = 1; i < columns.length; i++) {
				columns[i].setMutable(true);
			}
		}
	}
	
	static class SchemaDelegate1 extends AbstractSchemaDelegate {
	}
}
