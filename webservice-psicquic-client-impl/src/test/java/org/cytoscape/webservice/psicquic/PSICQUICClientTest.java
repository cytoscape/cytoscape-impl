package org.cytoscape.webservice.psicquic;

/*
 * #%L
 * Cytoscape PSIQUIC Web Service Impl (webservice-psicquic-client-impl)
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

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.swing.DialogTaskManager;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PSICQUICClientTest {

	@Mock
	private OpenBrowser openBrowser;
	@Mock
	private DialogTaskManager tm;
	@Mock
	CyNetworkFactory cyNetworkFactoryServiceRef;
	@Mock
	CyNetworkManager cyNetworkManagerServiceRef;
	@Mock
	CreateNetworkViewTaskFactory createViewTaskFactoryServiceRef;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

//		final String uriString = "http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry";
//		this.locationUri = new URI(uriString);
//		this.description = "PSICUIC Web Service Client";
//		this.displayName = "PSICQUIC";
//		this.queryObject = "brca1 AND brca2";
		
		// Enable this test only when you need to actual connection to the registry.
//		client = new PSICQUICWebServiceClient(uriString, displayName, description, cyNetworkFactoryServiceRef,
//				cyNetworkManagerServiceRef, tm, createViewTaskFactoryServiceRef, openBrowser);
	}
}
