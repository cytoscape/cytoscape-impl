package org.cytoscape.io.internal.read.nnf;

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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import org.cytoscape.io.internal.read.AbstractNetworkReaderTest;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import java.io.IOException;
import org.cytoscape.model.CyNetwork;
import java.io.FileInputStream;
import org.junit.Test;
import java.util.Iterator;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;


/**
 * Test code for Nested Network Format file reader.
 * 
 * @author kono, ruschein
 * @since Cytoscape 2.7.0
 */
public class NNFReaderTest extends AbstractNetworkReaderTest {
	
	// All test data files are in this directory.
	private static final String FILE_LOCATION = "src/test/resources/testData/NNFData/";

	
//	@Test
//	public void testGood1() throws Exception {
//		final NNFNetworkReader reader = new NNFNetworkReader(new FileInputStream(FILE_LOCATION + "good1.nnf"),  layouts,
//				viewFactory,  netFactory, mock(CyNetworkManager.class), mock(CyRootNetworkManager.class));
//			
//		reader.run(taskMonitor);
//		
//		assertNotNull(reader.getNetworks()[0]);
//		assertEquals("root", reader.getNetworks()[0].getRow(reader.getNetworks()[0]).get(CyNetwork.NAME, String.class));
//	}
	
//	@Test
//	public void testGood2() throws Exception {
//		final NNFNetworkReader reader = new NNFNetworkReader(new FileInputStream(FILE_LOCATION + "good2.nnf"),  layouts,
//				viewFactory,  netFactory, mock(CyNetworkManager.class), mock(CyRootNetworkManager.class));
//				
//		reader.run(taskMonitor);
//		
//		assertNotNull(reader.getNetworks()[0]);
//		assertEquals("root", reader.getNetworks()[0].getRow(reader.getNetworks()[0]).get(CyNetwork.NAME, String.class));
//	}
//	
//	
//	@Test
//	public void testGood3() throws Exception {
//		final NNFNetworkReader reader = new NNFNetworkReader(new FileInputStream(FILE_LOCATION + "good3.nnf"),  layouts,
//				viewFactory,  netFactory, mock(CyNetworkManager.class), mock(CyRootNetworkManager.class));
//
//		reader.run(taskMonitor);
//		
//		assertNotNull(reader.getNetworks()[0]);
//		assertEquals("Module_Overview", reader.getNetworks()[0].getRow(reader.getNetworks()[0]).get(CyNetwork.NAME, String.class));
//		
//		CyNetwork targetNetwork = reader.getNetworks()[0];
//
//		assertEquals("Module_Overview", targetNetwork.getRow(targetNetwork).get(CyNetwork.NAME, String.class));
//		assertEquals(4, targetNetwork.getNodeCount());
//		assertEquals(5, targetNetwork.getEdgeCount());
//	}
//	
//	
//	public void testGood4() throws Exception {
//		final NNFNetworkReader reader = new NNFNetworkReader(new FileInputStream(FILE_LOCATION + "good4.nnf"),  layouts,
//				viewFactory,  netFactory, mock(CyNetworkManager.class), mock(CyRootNetworkManager.class));
//
//		reader.run(taskMonitor);
//		
//		assertNotNull(reader.getNetworks()[0]);
//		assertEquals("Top_Level_Network", reader.getNetworks()[0].getRow(reader.getNetworks()[0]).get(CyNetwork.NAME, String.class));
//		
//		final CyNetwork[] networks = reader.getNetworks() ;//this.networkManager.getNetworkSet();
//		CyNetwork targetNetwork = null;
//		for (CyNetwork net : networks) {
//			if (net.getRow(net).get(CyNetwork.NAME, String.class).equals("M3")) {
//				targetNetwork = net;
//			}
//		}
//
//		assertNotNull(targetNetwork);
//		assertEquals("M3", targetNetwork.getRow(targetNetwork).get(CyNetwork.NAME, String.class));
//		assertEquals(4, targetNetwork.getNodeCount());
//		assertEquals(3, targetNetwork.getEdgeCount());
//		
//		// Find node M2
//		CyNode m2 = null; // = Cytoscape.getCyNode("M2");
//		for (CyNetwork net : networks) {
//			Iterator<CyRow> rowsIt = net.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, "M2").iterator();
//			while(rowsIt.hasNext()){
//				CyRow row = rowsIt.next();
//				Long suid = row.get("suid", Long.class);
//				m2 = net.getNode(suid);
//			}
//		}
//		
//		assertNotNull(m2);
//		CyNetwork nestedNetwork = m2.getNetworkPointer();
//		assertNotNull(nestedNetwork);
//		assertTrue(((CyNetwork)nestedNetwork).getRow(((CyNetwork)nestedNetwork)).get(CyNetwork.NAME, String.class).equals("M2"));
//		assertEquals(1, nestedNetwork.getNodeCount());
//		assertEquals(0, nestedNetwork.getEdgeCount());
//	}
//
//	
//	@Test
//	public void testGood5() throws Exception {
//		final NNFNetworkReader reader = new NNFNetworkReader(new FileInputStream(FILE_LOCATION + "good5.nnf"),  layouts,
//				viewFactory,  netFactory, mock(CyNetworkManager.class), mock(CyRootNetworkManager.class));
//
//		reader.run(taskMonitor);
//		
//		assertNotNull(reader.getNetworks()[0]);
//		assertEquals("TopLevelNetwork", reader.getNetworks()[0].getRow(reader.getNetworks()[0]).get(CyNetwork.NAME, String.class));
//	}
//	
//	@Test
//	public void testGood6() throws Exception {
//		final NNFNetworkReader reader = new NNFNetworkReader(new FileInputStream(FILE_LOCATION + "good6.nnf"),  layouts,
//				viewFactory,  netFactory, mock(CyNetworkManager.class), mock(CyRootNetworkManager.class));
//		reader.run(taskMonitor);
//		
//		assertNotNull(reader.getNetworks()[0]);
//		assertNotNull(reader.getNetworks()[1]);
//	}
//	
//	
//	public void testBad1() throws Exception {
//		final NNFNetworkReader reader = new NNFNetworkReader(new FileInputStream(FILE_LOCATION + "bad1.nnf"),  layouts,
//				viewFactory,  netFactory, mock(CyNetworkManager.class), mock(CyRootNetworkManager.class));
//		try {
//			reader.run(taskMonitor);
//		} catch (IOException e) {
//			e.printStackTrace();
//			assertNotNull(reader.getNetworks()[0]);
//			return;
//		}
//		
//		//If not caught by the above, something is wrong!
//		fail();
//	}
//	
//	@Test
//	public void testBad2() throws Exception {
//		final NNFNetworkReader reader = new NNFNetworkReader(new FileInputStream(FILE_LOCATION + "bad2.nnf"),  layouts,
//				viewFactory,  netFactory, mock(CyNetworkManager.class), mock(CyRootNetworkManager.class));
//
//		try {
//			reader.run(taskMonitor);
//		} catch (IOException e) {
//			e.printStackTrace();
//			assertNull(reader.getNetworks());
//			return;
//		}
//		
//		//If not caught by the above, something is wrong!
//		fail();
//	}

//	@Test
//	public void testMultipleFiles() throws Exception {
//		
//		final NNFNetworkReader reader1 = new NNFNetworkReader(new FileInputStream(FILE_LOCATION + "good3.nnf"),  layouts,
//				viewFactory,  netFactory, mock(CyNetworkManager.class));
//
//		reader1.run(taskMonitor);
//
//		final NNFNetworkReader reader2 = new NNFNetworkReader(new FileInputStream(FILE_LOCATION + "good4.nnf"),  layouts,
//				viewFactory,  netFactory, mock(CyNetworkManager.class));
//		
//		reader2.run(taskMonitor);
//		
//		// Find node m3 and the network M3 belongs to
//		final Set<CyNetwork> networks = this.networkManager.getNetworkSet();
//		CyNetwork targetNetwork = null;
//		for (CyNetwork net : networks) {
//			if (net.getRow(net).get(CyNetwork.NAME, String.class).equals("M3")) {
//				targetNetwork = net;
//			}
//		}
//		
//		assertNotNull(targetNetwork);
//		assertEquals("M3", targetNetwork.getRow(targetNetwork).get(CyNetwork.NAME, String.class));
//		assertEquals(6, targetNetwork.getNodeCount());
//		assertEquals(4, targetNetwork.getEdgeCount());
//	}
}
