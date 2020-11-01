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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.cytoscape.io.internal.util.cytables.model.BypassValue;
import org.cytoscape.io.internal.util.cytables.model.ColumnView;
import org.cytoscape.io.internal.util.cytables.model.CyTables;
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
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CyTablesXMLWriter extends AbstractTask implements CyWriter {

	private final CyServiceRegistrar registrar;
	
	private final Set<CyTableMetadata> tables;
	private final Set<CyTableView> tableViews;
	private final Map<View<CyColumn>,String> tableStyleMap;
	private final OutputStream outputStream;
	private Map<Long, String> tableFileNamesBySUID;
	
	public CyTablesXMLWriter(
			CyServiceRegistrar registrar,
			Set<CyTableMetadata> tables, 
			Set<CyTableView> tableViews, 
			Map<View<CyColumn>,String> tableStyleMap,
			Map<Long, String> tableFileNamesBySUID, 
			OutputStream outputStream) {
		
		this.registrar = registrar;
		this.tables = tables;
		this.tableViews = tableViews;
		this.outputStream = outputStream;
		this.tableFileNamesBySUID = tableFileNamesBySUID;
		this.tableStyleMap = tableStyleMap;
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
		
		for(CyTableView tableView : tableViews) {
			// MKTODO This is a temporary hack to handle facade tables. This is fragile, if someone changes 
			// how AbstractTableFacade.getPrimaryKey() works it could break this code.
			
			// The table browser is passed facade tables not the actual tables, so we need to dig down and get the real SUID.
			// Should probably add an interfce CyTableFacade and use instanceof to test for it.
			Long actualSuid = tableView.getModel().getPrimaryKey().getTable().getSUID();
			String tableName = tableFileNamesBySUID.get(actualSuid);
			if(tableName == null)
				continue;
			
			TableView xmlTabelView = new TableView();
			xmlTabelView.setRendererId(tableView.getRendererId());
			xmlTabelView.setTable(tableName);
			xmlTableViews.getTableView().add(xmlTabelView);
			
			for(View<CyColumn> colView : tableView.getColumnViews()) {
				ColumnView xmlColumnView = new ColumnView();
				xmlColumnView.setColumnName(colView.getModel().getName());
				xmlTabelView.getColumnView().add(xmlColumnView);
				
				String styleName = tableStyleMap.get(colView);
				if(styleName != null)
					xmlColumnView.setStyleTitle(styleName);
				
				VisualLexicon lexicon = getVisualLexicon(tableView);
				for(VisualProperty vp : lexicon.getAllVisualProperties()) {
					if(colView.isDirectlyLocked(vp)) {
						Object value = colView.getVisualProperty(vp);
						if(value != null) {
							String valueString = null;
							try {
								valueString = vp.toSerializableString(value);
							} catch(ClassCastException e) { }
							
							if(valueString != null) {
								BypassValue bypassValue = new BypassValue();
								bypassValue.setName(vp.getIdString());
								bypassValue.setValue(valueString);
								xmlColumnView.getBypassValue().add(bypassValue);
							}
						}
					}
				}
			}
		}

		return model;
	}

	
	private VisualLexicon getVisualLexicon(CyTableView tableView) {
		var renderingEngineManager = registrar.getService(RenderingEngineManager.class);
		var renderingEngines = renderingEngineManager.getRenderingEngines(tableView);
		for(var re : renderingEngines) {
			if(Objects.equals(tableView.getRendererId(), re.getRendererId())) {
				return re.getVisualLexicon();
			}
		}
		return null;
	}
}
