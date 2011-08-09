

package org.cytoscape.model;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;

import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.equations.Interpreter;

import static org.mockito.Mockito.*;

public class TableTestSupport {

	protected CyTableFactory tableFactory;
	protected DummyCyEventHelper eventHelper;

	public TableTestSupport() {
		eventHelper = new DummyCyEventHelper();
		tableFactory = new CyTableFactoryImpl( eventHelper, mock(CyTableManagerImpl.class), mock(Interpreter.class) );
	}

	public CyTableFactory getTableFactory() {
		return tableFactory;	
	}

	public DummyCyEventHelper getDummyCyEventHelper() {
		return eventHelper;
	}
}


