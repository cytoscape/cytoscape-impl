package org.cytoscape.work.internal.sync;

/*
 * #%L
 * org.cytoscape.work-impl
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.cytoscape.work.Tunable;

import java.lang.reflect.Field;
import java.util.*;

public class SyncTunableMutatorTest {

	@Test
	public void testTunableMutator() {
		SyncTunableMutator stm = new SyncTunableMutator();
		stm.addTunableHandlerFactory( new SyncTunableHandlerFactory(), new Properties() );

		Map<String,Object> map = new HashMap<String,Object>();
		map.put("tstring","hello");

		stm.setConfigurationContext(map);

		TunableHolder th = new TunableHolder();

		assertEquals("goodbye",th.tstring);

		stm.validateAndWriteBack(th);

		assertEquals("hello",th.tstring);
	}
}
