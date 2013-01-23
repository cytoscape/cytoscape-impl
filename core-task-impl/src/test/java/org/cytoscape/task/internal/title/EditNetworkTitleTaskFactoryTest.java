package org.cytoscape.task.internal.title;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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



import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;
import org.mockito.Mock;


public class EditNetworkTitleTaskFactoryTest {
	
	@Mock
	TunableSetter ts;
	
	@Test
	public void testGetTaskIterator() {
		CyNetwork net = mock(CyNetwork.class);
		CyNetworkManager netMgr = mock(CyNetworkManager.class);
		CyNetworkNaming cyNetworkNaming = mock(CyNetworkNaming.class);
		
		CyRow r1 =  mock(CyRow.class);
		when(net.getRow(net)).thenReturn(r1);
		when(r1.get("name", String.class)).thenReturn("title");

		UndoSupport undoSupport = mock(UndoSupport.class);

		EditNetworkTitleTaskFactoryImpl factory = new EditNetworkTitleTaskFactoryImpl(undoSupport,netMgr,cyNetworkNaming, ts);
		
		TaskIterator ti = factory.createTaskIterator(net);
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );		
	}
}
