/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.webservice.biomart;

import java.util.HashSet;
import java.util.Set;

import javax.naming.ConfigurationException;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.webservice.TableImportWebServiceClient;
import org.cytoscape.io.webservice.biomart.rest.BiomartRestClient;
import org.cytoscape.io.webservice.biomart.task.ImportTableTask;
import org.cytoscape.io.webservice.biomart.ui.BiomartAttrMappingPanel;
import org.cytoscape.io.webservice.client.AbstractWebServiceClient;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Biomart Web Service Client.
 * 
 */
public class BiomartClient extends AbstractWebServiceClient implements
		TableImportWebServiceClient {

	private final CyTableFactory tableFactory;

	private final BiomartRestClient restClient;

	private BiomartAttrMappingPanel gui;

	private ImportTableTask importTask;

	private final CyNetworkManager networkManager;
	private final CyApplicationManager applicationManager;
	
	private final CySwingApplication app;

	/**
	 * Creates a new Biomart Client object.
	 * 
	 * @throws ServiceException
	 * @throws ConfigurationException
	 */
	public BiomartClient(final String displayName, final String description,
			final BiomartRestClient restClient,
			final CyTableFactory tableFactory,
			final CyNetworkManager networkManager,
			final CyApplicationManager applicationManager, final CySwingApplication app) {
		super(restClient.getBaseURL(), displayName, description);

		this.tableFactory = tableFactory;
		this.restClient = restClient;

		this.networkManager = networkManager;
		this.applicationManager = applicationManager;
		this.app = app;

		// TODO: set optional parameters (Tunables?)
	}

	public void setGUI(final BiomartAttrMappingPanel gui) {
		this.gui = gui;
	}

	public BiomartRestClient getRestClient() {
		return this.restClient;
	}

	@Override
	public Set<CyTable> getTables() {
		// Return empty task if not executed yet.
		if (importTask == null)
			return new HashSet<CyTable>();
		else
			return importTask.getCyTables();
	}

	@Override
	public TaskIterator getTaskIterator() {
		if (gui == null)
			throw new IllegalStateException(
					"Could not build query because Query Builder GUI is null.");

		final BiomartQuery query = this.gui.getTableImportQuery();

		importTask = new ImportTableTask(restClient, query, tableFactory, networkManager, applicationManager, app.getJFrame());

		return new TaskIterator(importTask);
	}
}
