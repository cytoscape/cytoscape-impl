package org.cytoscape.model;

import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyTable.SavePolicy;
import org.cytoscape.model.internal.CyTableImpl;
import org.junit.Before;

public class CyColumnTest extends AbstractCyColumnTest{
	protected DummyCyEventHelper eventHelper; 
	protected final Interpreter interpreter;
	
	public CyColumnTest(){
		eventHelper = new DummyCyEventHelper();
		interpreter = new InterpreterImpl();
		
	}
	@Before
	public void setUp (){
		this.table = new CyTableImpl("homer", CyTableEntry.SUID, Long.class, false, true, SavePolicy.SESSION_FILE,
				eventHelper, interpreter, 1000);
		table.createColumn("test1", String.class, false);
	}
	
}