/*
  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.task.internal.select;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;

public class AbstractSelectTaskTester {

    CyEventHelper eventHelper;
    CyNetworkViewManager networkViewManager;
    TaskMonitor tm;
    CyTable table;
    CyNetwork net;
    CyRow r1;
    CyEdge e1;
    CyRow r2;
    CyEdge e2;
    CyRow r3;
    CyNode e3;
    CyRow r4;
    CyNode e4;

    public void setUp() throws Exception {
	eventHelper = mock(CyEventHelper.class);
	table = mock(CyTable.class);
	net = mock(CyNetwork.class);
	when(net.getDefaultNodeTable()).thenReturn(table);

	CyNetworkView view = mock(CyNetworkView.class);
	when(view.getModel()).thenReturn(net);

	networkViewManager = mock(CyNetworkViewManager.class);
	Collection<CyNetworkView> views = new HashSet<CyNetworkView>();
	views.add(view);
	when(networkViewManager.getNetworkViews(any(CyNetwork.class))).thenReturn(views);

	tm = mock(TaskMonitor.class);

	r1 = mock(CyRow.class);
	when(r1.getTable()).thenReturn(table);
	e1 = mock(CyEdge.class);
	when(net.getRow(e1)).thenReturn(r1);

	r2 = mock(CyRow.class);
	when(r2.getTable()).thenReturn(table);
	e2 = mock(CyEdge.class);
	when(net.getRow(e2)).thenReturn(r2);

	List<CyEdge> el = new ArrayList<CyEdge>();
	el.add(e1);
	el.add(e2);
	when(net.getEdgeList()).thenReturn(el);

	r3 = mock(CyRow.class);
	when(r3.getTable()).thenReturn(table);
	e3 = mock(CyNode.class);
	when(net.getRow(e3)).thenReturn(r3);

	r4 = mock(CyRow.class);
	when(r4.getTable()).thenReturn(table);
	e4 = mock(CyNode.class);
	when(net.getRow(e4)).thenReturn(r4);

	List<CyNode> nl = new ArrayList<CyNode>();
	nl.add(e3);
	nl.add(e4);
	when(net.getNodeList()).thenReturn(nl);
    }
}
