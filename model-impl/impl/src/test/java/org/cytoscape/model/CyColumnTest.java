package org.cytoscape.model;

import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.internal.CyTableImpl;
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

public class CyColumnTest extends AbstractCyColumnTest{
	
	protected DummyCyEventHelper eventHelper; 
	protected final Interpreter interpreter;
	
	public CyColumnTest(){
		eventHelper = new DummyCyEventHelper();
		interpreter = new InterpreterImpl();
	}
	
	@Before
	public void setUp (){
		table = new CyTableImpl("homer", CyIdentifiable.SUID, Long.class, false, true, SavePolicy.SESSION_FILE,
				eventHelper, interpreter, 1000);
	}
}
