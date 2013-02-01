package org.cytoscape.group;

/*
 * #%L
 * Cytoscape Groups Impl (group-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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


import org.junit.After;
import org.junit.Before;

import org.cytoscape.group.AbstractCyGroupTest;
import org.cytoscape.model.NetworkTestSupport;


public class CyGroupTest extends AbstractCyGroupTest {
	@Before
	public void setUp() {
		NetworkTestSupport support = new NetworkTestSupport();
		net = support.getNetwork();
		groupFactory = TestCyGroupFactory.getFactory();
		defaultSetUp();
	}

	@After
	public void tearDown() {
		net = null;
		groupFactory = null;
	}
}
