package org.cytoscape.model;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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


import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.junit.Before;
import org.junit.After;

import java.util.Random;


/**
 * This will verify that the network created by NetworkTestSupport
 * is a good network.
 */
public class TableTestSupportTest extends AbstractCyTableTest {
	TableTestSupport support; 
	CyTableFactory factory;
	Random rand;

	public TableTestSupportTest() {
		support = new TableTestSupport();
		factory = support.getTableFactory();
		rand = new Random(15);
	}

	@Before
	public void setUp() {
		eventHelper = support.getDummyCyEventHelper(); 
		table = factory.createTable(Integer.toString( rand.nextInt(10000) ), CyIdentifiable.SUID, Long.class, false, true);
		table2 = factory.createTable(Integer.toString( rand.nextInt(10000) ), CyIdentifiable.SUID, Long.class, false, true);
		attrs = table.getRow(1l);
		CyTableManagerImpl tblMgr = new CyTableManagerImpl(eventHelper,new CyNetworkTableManagerImpl(), new CyNetworkManagerImpl(eventHelper));
		tblMgr.addTable(table);
		((CyTableImpl)table).handleEvent(new TableAddedEvent(tblMgr, table));
		tblMgr.addTable(table2);
		((CyTableImpl)table2).handleEvent(new TableAddedEvent(tblMgr, table2));

	}

	@After
	public void tearDown() {
		table = null;
		attrs = null;
	}
}
