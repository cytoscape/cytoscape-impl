package org.cytoscape.model;


import static org.mockito.Mockito.mock;

import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.service.util.CyServiceRegistrar;


public class TableTestSupport {
	protected CyTableFactory tableFactory;
	protected DummyCyEventHelper eventHelper;

	public TableTestSupport() {
		eventHelper = new DummyCyEventHelper();
		tableFactory = new CyTableFactoryImpl(eventHelper, new InterpreterImpl(),
		                                      mock(CyServiceRegistrar.class));
	}

	public CyTableFactory getTableFactory() {
		return tableFactory;	
	}

	public DummyCyEventHelper getDummyCyEventHelper() {
		return eventHelper;
	}
}


