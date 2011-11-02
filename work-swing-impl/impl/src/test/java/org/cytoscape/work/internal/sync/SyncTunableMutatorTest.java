package org.cytoscape.work.internal.sync;


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
