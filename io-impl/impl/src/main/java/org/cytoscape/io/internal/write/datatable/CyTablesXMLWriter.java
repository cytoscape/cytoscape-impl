package org.cytoscape.io.internal.write.datatable;

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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.cytoscape.io.internal.util.cytables.model.BypassValue;
import org.cytoscape.io.internal.util.cytables.model.ColumnView;
import org.cytoscape.io.internal.util.cytables.model.CyTables;
import org.cytoscape.io.internal.util.cytables.model.RowView;
import org.cytoscape.io.internal.util.cytables.model.TableView;
import org.cytoscape.io.internal.util.cytables.model.TableViews;
import org.cytoscape.io.internal.util.cytables.model.VirtualColumn;
import org.cytoscape.io.internal.util.cytables.model.VirtualColumns;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.table.CyColumnViewMetadata;
import org.cytoscape.view.model.table.CyRowViewMetadata;
import org.cytoscape.view.model.table.CyTableViewMetadata;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CyTablesXMLWriter extends AbstractTask implements CyWriter {

	private final CyServiceRegistrar registrar;
	
	private final Set<CyTableMetadata> tables;
	private final Set<CyTableViewMetadata> tableViews;
	private final OutputStream outputStream;
	private Map<Long, String> tableFileNamesBySUID;
	
	public CyTablesXMLWriter(
			CyServiceRegistrar registrar,
			Set<CyTableMetadata> tables, 
			Set<CyTableViewMetadata> tableViews, 
			Map<Long, String> tableFileNamesBySUID, 
			OutputStream outputStream) {
		
		this.registrar = registrar;
		this.tables = tables;
		this.tableViews = tableViews;
		this.outputStream = outputStream;
		this.tableFileNamesBySUID = tableFileNamesBySUID;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final JAXBContext jc = JAXBContext.newInstance(CyTables.class.getPackage().getName(), this.getClass().getClassLoader());
		Marshaller m = jc.createMarshaller();
		taskMonitor.setProgress(0.25);
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		taskMonitor.setProgress(0.5);
		
		CyTables model = buildModel();
		taskMonitor.setProgress(0.75);
		
		m.marshal(model, outputStream);
		taskMonitor.setProgress(1.0);
	}

	private CyTables buildModel() {
		CyTables model = new CyTables();
		
		// Virtual columns
		VirtualColumns virtualColumns = new VirtualColumns();
		model.setVirtualColumns(virtualColumns);
		for (CyTableMetadata metadata : tables) {
			CyTable table = metadata.getTable();
			
			String targetTable = tableFileNamesBySUID.get(table.getSUID());
			if (targetTable == null) {
				continue;
			}
			
			for (CyColumn cyColumn : table.getColumns()) {
				VirtualColumnInfo info = cyColumn.getVirtualColumnInfo();
				if (!info.isVirtual()) {
					continue;
				}
				
				String sourceTable = tableFileNamesBySUID.get(info.getSourceTable().getSUID());
				if (sourceTable == null) {
					// log this
					continue;
				}
				
				VirtualColumn column = new VirtualColumn();
				column.setName(cyColumn.getName());
				column.setSourceColumn(info.getSourceColumn());
				column.setSourceTable(sourceTable);
				column.setSourceJoinKey(info.getSourceJoinKey());
				column.setTargetTable(targetTable);
				column.setTargetJoinKey(info.getTargetJoinKey());
				virtualColumns.getVirtualColumn().add(column);
			}
		}
		
		// Table view styles
		TableViews xmlTableViews = new TableViews();
		model.setTableViews(xmlTableViews);
		
		for(CyTableViewMetadata tableViewMetadata : tableViews) {
			Long actualSuid = tableViewMetadata.getSavedTableSUID();
			String tableFileName = tableFileNamesBySUID.get(actualSuid);
			if(tableFileName == null)
				continue;
			
			TableView xmlTableView = new TableView();
			xmlTableView.setRendererId(tableViewMetadata.getRendererID());
			xmlTableView.setTableNamespace(tableViewMetadata.getNamespace());
			xmlTableView.setTable(tableFileName);
			xmlTableViews.getTableView().add(xmlTableView);
			
			for(var entry : tableViewMetadata.getBypassValues().entrySet()) {
				var vpName = entry.getKey();
				var value = entry.getValue();
				if(value != null) {
					BypassValue bypassValue = new BypassValue();
					bypassValue.setName(vpName);
					bypassValue.setValue(value);
					xmlTableView.getBypassValue().add(bypassValue);
				}
			}
			
			for(CyColumnViewMetadata colViewMetadata : tableViewMetadata.getColumnViews()) {
				ColumnView xmlColumnView = new ColumnView();
				xmlColumnView.setColumnName(colViewMetadata.getName());
				xmlColumnView.setStyleTitle(colViewMetadata.getStyleName());
				
				for(var entry : colViewMetadata.getBypassValues().entrySet()) {
					var vpName = entry.getKey();
					var value = entry.getValue();
					if(value != null) {
						BypassValue bypassValue = new BypassValue();
						bypassValue.setName(vpName);
						bypassValue.setValue(value);
						xmlColumnView.getBypassValue().add(bypassValue);
					}
				}
				
				if(xmlColumnView.getStyleTitle() != null || !xmlColumnView.getBypassValue().isEmpty()) {
					xmlTableView.getColumnView().add(xmlColumnView);
				}
			}
			
			for(CyRowViewMetadata rowViewMetadata : tableViewMetadata.getRowViews()) {
				RowView xmlRowView = null;
				
				for(var entry : rowViewMetadata.getBypassValues().entrySet()) {
					var vpName = entry.getKey();
					var value = entry.getValue();
					if(value != null) {
						if(xmlRowView == null) {
							xmlRowView = new RowView();
							String serializedKey = serializeKey(rowViewMetadata.getKeyValue(), tableViewMetadata.getPrimaryKeyType());
							xmlRowView.setKey(serializedKey);
							xmlTableView.getRowView().add(xmlRowView);
						}
						BypassValue bypassValue = new BypassValue();
						bypassValue.setName(vpName);
						bypassValue.setValue(value);
						xmlRowView.getBypassValue().add(bypassValue);
					}
				}
			}
		}

		return model;
	}
	
	
//	private static boolean primaryKeyIsSUID(CyTable table) {
//		CyColumn pk = table.getPrimaryKey();
//		return pk.getName().equals(CyIdentifiable.SUID) && pk.getType().equals(Long.class);
//	}
	
	private static String serializeKey(Object key, Class<?> keyType) {
		if (keyType.equals(List.class)) {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			List<?> list = (List<?>) key;
			for (Object value : list) {
				if (!first)
					sb.append("|");
				if (value != null)
					sb.append(value);
				first = false;
			}
			return sb.toString();
		} else {
			return key.toString();
		}
	}
	
//	private static Object deserializeKey(String key, CyColumn primaryKeyColumn) {
//		Class<?> type = primaryKeyColumn.getType();
//		
//		if (type.equals(List.class)) {
//			Class<?> listElementType = primaryKeyColumn.getListElementType();
//			List<Object> list = new ArrayList<>();
//			String[] values = key.split("|");
//			for (String item : values) {
//				list.add(deserializeNonListValue(item, listElementType));
//			}
//			if (list.size() == 1 && list.get(0) == null) 
//				return null;
//			return list;
//		} else {
//			return deserializeNonListValue(key, type);
//		}
//	}
//	
//	private static Object deserializeNonListValue(String value, Class<?> type) {
//		if(type.equals(String.class)) {
//			return value;
//		} else if(value.isEmpty()) {
//			return null;
//		} else {
//			try {
//				if (type.equals(Long.class)) {
//					return Long.valueOf(value);
//				} else if (type.equals(Boolean.class)) {
//					return Boolean.valueOf(value);
//				} else if (type.equals(Double.class)) {
//					return Double.valueOf(value);
//				} else if (type.equals(Integer.class)) {
//					return Integer.valueOf(value); 
//				}
//			} catch (Exception e) { }
//		}
//		return null;
//	}

}
