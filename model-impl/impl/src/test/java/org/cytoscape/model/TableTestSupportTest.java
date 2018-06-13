package org.cytoscape.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.internal.EquationCompilerImpl;
import org.cytoscape.equations.internal.EquationParserImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.junit.After;
import org.junit.Before;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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

/**
 * This will verify that the network created by NetworkTestSupport
 * is a good network.
 */
public class TableTestSupportTest extends AbstractCyTableTest {
	
	private CyNetworkNaming namingUtil = mock(CyNetworkNaming.class);
	private CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
	
	TableTestSupport support; 
	CyTableFactory factory;

	public TableTestSupportTest() {
		support = new TableTestSupport();
		factory = support.getTableFactory();
	}

	@Before
	public void setUp() {
		eventHelper = support.getDummyCyEventHelper();
		EquationCompiler compiler = new EquationCompilerImpl(new EquationParserImpl(serviceRegistrar));
		
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(namingUtil);
		when(serviceRegistrar.getService(EquationCompiler.class)).thenReturn(compiler);
		
		table = createTable("homer");
		table2 = createTable("marge");
		attrs = table.getRow(1l);
		CyTableManagerImpl tblMgr = new CyTableManagerImpl(new CyNetworkTableManagerImpl(), 
				new CyNetworkManagerImpl(serviceRegistrar), serviceRegistrar);
		tblMgr.addTable(table);
		((CyTableImpl)table).handleEvent(new TableAddedEvent(tblMgr, table));
		tblMgr.addTable(table2);
		((CyTableImpl)table2).handleEvent(new TableAddedEvent(tblMgr, table2));
	}

	@Override
	protected CyTable createTable(String title) {
		return factory.createTable(title, CyIdentifiable.SUID, Long.class, false, true);
	}
	
	@After
	public void tearDown() {
		table = null;
		attrs = null;
	}
}
