package org.cytoscape.io.webservice.biomart;

/*
 * #%L
 * Cytoscape Biomart Webservice Impl (webservice-biomart-client-impl)
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



import javax.naming.ConfigurationException;

import org.cytoscape.io.webservice.TableImportWebServiceClient;
import org.cytoscape.io.webservice.biomart.rest.BiomartRestClient;
import org.cytoscape.io.webservice.biomart.task.ImportTableTask;
import org.cytoscape.io.webservice.biomart.ui.BiomartAttrMappingPanel;
import org.cytoscape.io.webservice.swing.AbstractWebServiceGUIClient;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.edit.ImportDataTableTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.osgi.framework.ServiceException;


/**
 * BioMart Web Service Client.
 * 
 */
public class BiomartClient extends AbstractWebServiceGUIClient implements TableImportWebServiceClient {
	
	private final CyTableFactory tableFactory;
	private final BiomartRestClient restClient;
	private ImportTableTask importTask;
	private final CyTableManager tableManager;
	private final ImportDataTableTaskFactory importNetworkAttrTF;

	/**
	 * Creates a new Biomart Client object.
	 * 
	 * @throws ServiceException
	 * @throws ConfigurationException
	 */
	public BiomartClient(final String displayName, final String description,
	                     final BiomartRestClient restClient, final CyTableFactory tableFactory,
	                     final CyTableManager tableManager,
						 final BiomartAttrMappingPanel gui,
						 final ImportDataTableTaskFactory importNetworkAttrTF)
	{
		super(restClient.getBaseURL(), displayName, description);

		this.tableFactory         = tableFactory;
		this.restClient           = restClient;
		this.tableManager         = tableManager;
		this.importNetworkAttrTF     = importNetworkAttrTF;
		
		this.gui = gui;

		// TODO: set optional parameters (Tunables?)
	}

	public BiomartRestClient getRestClient() {
		return this.restClient;
	}


	@Override
	public TaskIterator createTaskIterator(Object query) {
		if (gui == null)
			throw new IllegalStateException(
					"Could not build query because Query Builder GUI is null.");

		importTask = new ImportTableTask(restClient, (BiomartQuery) query, tableFactory,
		                                  tableManager,importNetworkAttrTF);

		return new TaskIterator(importTask);
	}
}
