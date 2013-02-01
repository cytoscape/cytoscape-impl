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

import org.cytoscape.io.internal.util.cytables.model.CyTables;
import org.cytoscape.io.internal.util.cytables.model.VirtualColumn;
import org.cytoscape.io.internal.util.cytables.model.VirtualColumns;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CyTablesXMLWriter extends AbstractTask implements CyWriter {

	private final Set<CyTableMetadata> tables;
	private final OutputStream outputStream;
	private Map<Long, String> tableFileNamesBySUID;
	
	public CyTablesXMLWriter(Set<CyTableMetadata> tables, Map<Long, String> tableFileNamesBySUID, OutputStream outputStream) {
		this.tables = tables;
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
		VirtualColumns virtualColumns = new VirtualColumns();
		model.setVirtualColumns(virtualColumns);
		List<VirtualColumn> columns = virtualColumns.getVirtualColumn();
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
				columns.add(column);
			}
		}
		return model;
	}

}
