package org.cytoscape.io.internal.read.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.io.internal.read.datatable.CSVCyReaderFactory;
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.SUIDUpdater;
import org.cytoscape.io.internal.util.cytables.model.VirtualColumn;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class Cy3SessionReaderImplTest {
	
	private Cy3SessionReaderImpl reader;
	private TableTestSupport tblTestSupport;
	
	@Before
	public void setUp() {
		InputStream is = mock(InputStream.class);
		GroupUtil groupUtil = mock(GroupUtil.class);
		SUIDUpdater suidUpdater = mock(SUIDUpdater.class);
		CyNetworkReaderManager netReaderMgr = mock(CyNetworkReaderManager.class);
		CyPropertyReaderManager propReaderMgr = mock(CyPropertyReaderManager.class);
		VizmapReaderManager vizmapReaderMgr = mock(VizmapReaderManager.class);
		CSVCyReaderFactory csvCyReaderFactory = mock(CSVCyReaderFactory.class);
		
		CyNetworkTableManager netTblMgr = mock(CyNetworkTableManager.class);
		CyRootNetworkManager rootNetMgr = mock(CyRootNetworkManager.class);
		EquationCompiler compiler = mock(EquationCompiler.class);
		
		CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyNetworkTableManager.class)).thenReturn(netTblMgr);
		when(serviceRegistrar.getService(CyRootNetworkManager.class)).thenReturn(rootNetMgr);
		when(serviceRegistrar.getService(EquationCompiler.class)).thenReturn(compiler);
		
		ReadCache cache = new ReadCache(serviceRegistrar);
		
		reader = new Cy3SessionReaderImpl(is, cache, groupUtil, suidUpdater, netReaderMgr, propReaderMgr,
				vizmapReaderMgr, csvCyReaderFactory, serviceRegistrar);
		tblTestSupport = new TableTestSupport();
	}
	
	@Test
	public void testEscaping() throws Exception {
		NetworkViewTestSupport support = new NetworkViewTestSupport();
		CyNetworkView view = support.getNetworkView();
		CyNetwork network = view.getModel();
		String title = "Network_With-Special Characters<>?:\"{}|=+()*&^%$#@![]|;',./\\";
		network.getRow(network).set(CyNetwork.NAME, title);
		
		{
			Matcher matcher = Cy3SessionReaderImpl.NETWORK_NAME_PATTERN.matcher(SessionUtil.getXGMMLFilename(network));
			Assert.assertTrue(matcher.matches());
			Assert.assertEquals(String.valueOf(network.getSUID()), matcher.group(1));
			Assert.assertEquals(title + ".xgmml", decode(matcher.group(3)));
		}
		{
			Matcher matcher = Cy3SessionReaderImpl.NETWORK_VIEW_NAME_PATTERN.matcher(SessionUtil.getXGMMLFilename(view));
			Assert.assertTrue(matcher.matches());
			Assert.assertEquals(String.valueOf(network.getSUID()), matcher.group(1));
			Assert.assertEquals(String.valueOf(view.getSUID()), matcher.group(2));
			Assert.assertEquals(title + ".xgmml", decode(matcher.group(4)));
		}
	}

	@Test
	public void testRestoreVirtualColumns() throws Exception {
		VirtualColumn vcA1 = new VirtualColumn();
		vcA1.setName("vcA1");
		vcA1.setSourceColumn("cA");
		vcA1.setSourceJoinKey("id");
		vcA1.setTargetJoinKey("id");
		vcA1.setSourceTable("Tbl1");
		vcA1.setTargetTable("Tbl2");
		vcA1.setImmutable(false);
		
		VirtualColumn vcA2 = new VirtualColumn();
		vcA2.setName("vcA2");
		vcA2.setSourceColumn("vcA1");
		vcA2.setSourceJoinKey("id");
		vcA2.setTargetJoinKey("id");
		vcA2.setSourceTable("Tbl2");
		vcA2.setTargetTable("Tbl3");
		vcA2.setImmutable(false);
		
		VirtualColumn vcA3 = new VirtualColumn();
		vcA3.setName("vcA3");
		vcA3.setSourceColumn("vcA2");
		vcA3.setSourceJoinKey("id");
		vcA3.setTargetJoinKey("id");
		vcA3.setSourceTable("Tbl3");
		vcA3.setTargetTable("Tbl4");
		vcA3.setImmutable(false);
		
		VirtualColumn vcB = new VirtualColumn();
		vcB.setName("cB");
		vcB.setSourceColumn("cB");
		vcB.setSourceJoinKey("cA");
		vcB.setTargetJoinKey("vcA3");
		vcB.setSourceTable("Tbl5");
		vcB.setTargetTable("Tbl4");
		vcB.setImmutable(true);
		
		CyTableFactory tblFactory = tblTestSupport.getTableFactory();
		
		CyTable tbl1 = tblFactory.createTable("Tbl1", "id", Integer.class, false, true);
		tbl1.createColumn("cA", String.class, true);
		CyTable tbl2 = tblFactory.createTable("Tbl2", "id", Integer.class, true, true);
		CyTable tbl3 = tblFactory.createTable("Tbl3", "id", Integer.class, true, false);
		CyTable tbl4 = tblFactory.createTable("Tbl4", "id", Integer.class, true, false);
		CyTable tbl5 = tblFactory.createTable("Tbl5", "cA", String.class, true, false);
		tbl5.createColumn("cB", Boolean.class, true);
		
		reader.filenameTableMap.put(tbl1.getTitle(), tbl1);
		reader.filenameTableMap.put(tbl2.getTitle(), tbl2);
		reader.filenameTableMap.put(tbl3.getTitle(), tbl3);
		reader.filenameTableMap.put(tbl4.getTitle(), tbl4);
		reader.filenameTableMap.put(tbl5.getTitle(), tbl5);
		
		// Set this order to see if it works when virtual columns depend on other virtual columns
		reader.virtualColumns.add(vcB); // the virtual column "vcA3" (used as target join key) hasn't been created yet
		reader.virtualColumns.add(vcA3); // the virtual column "vcA2" hasn't been created yet
		reader.virtualColumns.add(vcA2); // the virtual column "vcA1" hasn't been created yet
		reader.virtualColumns.add(vcA1); 
		
		reader.restoreVirtualColumns();
		
		// --- Test ---
		
		{
			CyColumn c = tbl2.getColumn("vcA1");
			assertNotNull(c);
			assertTrue(c.getVirtualColumnInfo().isVirtual());
			assertEquals("cA", c.getVirtualColumnInfo().getSourceColumn());
			assertEquals(tbl1, c.getVirtualColumnInfo().getSourceTable());
			assertFalse(c.isImmutable());
		}
		{
			CyColumn c = tbl3.getColumn("vcA2");
			assertNotNull(c);
			assertTrue(c.getVirtualColumnInfo().isVirtual());
			assertEquals("vcA1", c.getVirtualColumnInfo().getSourceColumn());
			assertEquals(tbl2, c.getVirtualColumnInfo().getSourceTable());
			assertFalse(c.isImmutable());
		}
		{
			CyColumn c = tbl4.getColumn("vcA3");
			assertNotNull(c);
			assertTrue(c.getVirtualColumnInfo().isVirtual());
			assertEquals("vcA2", c.getVirtualColumnInfo().getSourceColumn());
			assertEquals(tbl3, c.getVirtualColumnInfo().getSourceTable());
			assertFalse(c.isImmutable());
		}
		{
			CyColumn c = tbl4.getColumn("cB");
			assertNotNull(c);
			assertTrue(c.getVirtualColumnInfo().isVirtual());
			assertEquals("cB", c.getVirtualColumnInfo().getSourceColumn());
			assertEquals(tbl5, c.getVirtualColumnInfo().getSourceTable());
			assertTrue(c.isImmutable());
		}
	}
	
	@Test(expected=Exception.class)
	public void testRestoreVirtualColumnsWithCircularDependencies() throws Exception {
		// This should never happen, but let's make sure it doesn't get into an infinite loop
		{
			VirtualColumn vc = new VirtualColumn();
			vc.setName("a");
			vc.setSourceColumn("a");
			vc.setSourceJoinKey("id");
			vc.setTargetJoinKey("id");
			vc.setSourceTable("t2");
			vc.setTargetTable("t3");
			reader.virtualColumns.add(vc);
		}
		{
			VirtualColumn vc = new VirtualColumn();
			vc.setName("a");
			vc.setSourceColumn("a");
			vc.setSourceJoinKey("id");
			vc.setTargetJoinKey("id");
			vc.setSourceTable("t1");
			vc.setTargetTable("t2");
			reader.virtualColumns.add(vc);
		}
		{
			VirtualColumn vc = new VirtualColumn();
			vc.setName("a");
			vc.setSourceColumn("a");
			vc.setSourceJoinKey("id");
			vc.setTargetJoinKey("id");
			vc.setSourceTable("t2");
			vc.setTargetTable("t3");
			reader.virtualColumns.add(vc);
		}
		
		CyTableFactory tblFactory = tblTestSupport.getTableFactory();
		CyTable tbl1 = tblFactory.createTable("t1", "id", Integer.class, true, true);
		CyTable tbl2 = tblFactory.createTable("t2", "id", Integer.class, true, true);
		CyTable tbl3 = tblFactory.createTable("t3", "id", Integer.class, true, true);
		
		reader.filenameTableMap.put(tbl1.getTitle(), tbl1);
		reader.filenameTableMap.put(tbl2.getTitle(), tbl2);
		reader.filenameTableMap.put(tbl3.getTitle(), tbl3);
		
		reader.restoreVirtualColumns();
	}
	
	private String decode(String encodedText) throws UnsupportedEncodingException {
		return URLDecoder.decode(encodedText, "UTF-8");
	}
}
