package org.cytoscape.io.internal.read.gml;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;

import org.cytoscape.io.internal.read.AbstractNetworkReaderTest;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GMLNetworkReaderTest extends AbstractNetworkReaderTest {
	@Mock private RenderingEngineManager renderingEngineManager;
	@Mock private VisualLexicon lexicon;
	private UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		MockitoAnnotations.initMocks(this);
		
		when(renderingEngineManager.getDefaultVisualLexicon()).thenReturn(lexicon);
		
		// FIXME
		//when(lexicon.getVisualProperties(any(String.class))).thenReturn(new LinkedList<VisualProperty<?>>());
		
		TableTestSupport tblTestSupport = new TableTestSupport();
		CyTableFactory tableFactory = tblTestSupport.getTableFactory();
		CyTableManager tableMgr= mock(CyTableManager.class);
		
		unrecognizedVisualPropertyMgr = new UnrecognizedVisualPropertyManager(tableFactory, tableMgr);
	}
	
	@Test
	public void testLoadGml() throws Exception {
		File file = new File("src/test/resources/testData/gml/example1.gml");
		GMLNetworkReader reader = new GMLNetworkReader(new FileInputStream(file), netFactory, viewFactory,
													   renderingEngineManager, unrecognizedVisualPropertyMgr, this.networkManager, this.rootNetworkManager, this.cyApplicationManager);
		reader.run(taskMonitor);
		
		final CyNetwork[] networks = reader.getNetworks();
		final CyNetworkView[] networkViews = new CyNetworkView[networks.length];
		int i = 0;
		for(CyNetwork network: networks) {
			networkViews[i] = reader.buildCyNetworkView(network);
			i++;
		}
		
		assertNotNull(networkViews);
		assertEquals(1, networkViews.length);
		
		CyNetworkView view = networkViews[0];
		assertNotNull(view);
		
		CyNetwork model = view.getModel();
		assertNotNull(model);
		
		assertEquals(3, model.getNodeCount());
		assertEquals(3, model.getEdgeCount());
	}
}
