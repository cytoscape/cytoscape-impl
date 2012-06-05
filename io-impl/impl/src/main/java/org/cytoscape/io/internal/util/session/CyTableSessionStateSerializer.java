package org.cytoscape.io.internal.util.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.cytoscape.io.internal.model.CyTableSessionState;
import org.cytoscape.io.internal.model.CyTableSessionState.VirtualColumn;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.session.CySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyuproject.protostuff.JsonIOUtil;

public class CyTableSessionStateSerializer {
	private static final Logger logger = LoggerFactory.getLogger(CyTableSessionStateSerializer.class);

	public void serialize(OutputStream stream, Map<Long, String> tableFilenamesBySUID, CySession session) throws IOException {
		CyTableSessionState model = buildModel(stream, tableFilenamesBySUID, session);
		JsonGenerator generator = JsonIOUtil.newJsonGenerator(stream, new byte[1024*100]);
		generator.useDefaultPrettyPrinter();
		
		try {
			JsonIOUtil.writeTo(generator, model, CyTableSessionState.getSchema(), false);
		} finally {
			generator.flush();
		}
	}

	public CyTableSessionState parse(InputStream stream) throws IOException {
		CyTableSessionState model = new CyTableSessionState();
		JsonIOUtil.mergeFrom(stream, model, CyTableSessionState.getSchema(), false);
		return model;
	}
	
	CyTableSessionState buildModel(OutputStream stream, Map<Long, String> tableFilenamesBySUID, CySession session) {
		List<VirtualColumn> virtualColumns = new ArrayList<VirtualColumn>();
		for (CyTableMetadata metadata : session.getTables()) {
			CyTable table = metadata.getTable();
			String targetTable = tableFilenamesBySUID.get(table.getSUID());
			
			if (targetTable == null) {
				continue;
			}
			
			for (CyColumn column : table.getColumns()) {
				VirtualColumnInfo info = column.getVirtualColumnInfo();
				
				if (!info.isVirtual()) {
					continue;
				}
				
				String sourceTable = tableFilenamesBySUID.get(info.getSourceTable().getSUID());
				
				if (sourceTable == null) {
					logger.warn("Cannot serialize virtual column \"" + column.getName() + "\" of \"" + targetTable
							+ "\" because the source table is null.");
					continue;
				}
				
				virtualColumns.add(new VirtualColumn(column.getName(),
								   info.getSourceColumn(),
								   sourceTable,
								   info.getSourceJoinKey(),
								   targetTable,
								   info.getTargetJoinKey(),
								   info.isImmutable()));
			}
		}
		
		CyTableSessionState state = new CyTableSessionState();
		state.setVirtualColumnsList(virtualColumns);
		return state;
	}
}
