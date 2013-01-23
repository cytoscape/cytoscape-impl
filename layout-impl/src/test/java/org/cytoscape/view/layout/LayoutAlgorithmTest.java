package org.cytoscape.view.layout;

/*
 * #%L
 * Cytoscape Layout Impl (layout-impl)
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

import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.junit.Before;

public class LayoutAlgorithmTest extends AbstractLayoutAlgorithmTest {

	@Before
	public void setUp() throws Exception {
		computerName = "dummy";
		humanName = "Dummy Layout";
		this.layout = new DummyLayout(computerName, humanName);
	}

	private static final class DummyLayout extends AbstractLayoutAlgorithm {

		public DummyLayout(String computerName, String humanName) {
			super(computerName, humanName, null);
		}

		@Override
		public TaskIterator createTaskIterator(CyNetworkView networkView, Object layoutContext,
				Set<View<CyNode>> nodesToLayOut, String attrName) {
			return null;
		}

	}
}
