package org.cytoscape.cpath2.internal;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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

import org.cytoscape.cpath2.internal.task.ExecuteGetRecordByCPathIdTaskFactory;
import org.cytoscape.cpath2.internal.web_service.CPathProperties;
import org.cytoscape.cpath2.internal.web_service.CPathResponseFormat;
import org.cytoscape.cpath2.internal.web_service.CPathWebService;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class CPathNetworkImportTask implements Task {

	private final String query;
	private final CPathWebService client;
	private final CPathResponseFormat format;
	private final CPath2Factory factory;

	public CPathNetworkImportTask(String query, CPathWebService client, CPathResponseFormat format, CPath2Factory factory) {
		this.query = query;
		this.client = client;
		this.format = format;
		this.factory = factory;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
        String idStrs[] = query.split(" ");
        long ids[] = new long[idStrs.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = Long.parseLong(idStrs[i]);
        }

//        ModuleProperties properties = this.getProps();
//        Tunable tunable = properties.get(RESPONSE_FORMAT);
//        CPathResponseFormat format = CPathResponseFormat.BINARY_SIF;
//        if (tunable != null) {
//            format = CPathResponseFormat.getResponseFormat((String) tunable.getValue());
//        }

        //  Create the task
        ExecuteGetRecordByCPathIdTaskFactory taskFactory = factory.createExecuteGetRecordByCPathIdTaskFactory(client, ids, format, CPathProperties.getInstance().getCPathServerName());
        TaskIterator iterator = taskFactory.createTaskIterator();
        while (iterator.hasNext()) {
        	Task task = iterator.next();
            task.run(taskMonitor);
        }
	}

	@Override
	public void cancel() {
	}
}
