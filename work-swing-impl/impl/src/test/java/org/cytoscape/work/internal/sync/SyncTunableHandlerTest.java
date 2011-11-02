package org.cytoscape.work.internal.sync;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.cytoscape.work.Tunable;

import java.lang.reflect.Field;
import java.util.*;

public class SyncTunableHandlerTest {


	@Test
	public void testSyncTunableHandler() throws Exception {
		TunableHolder th = new TunableHolder();

        final Field stringField = th.getClass().getField("tstring");
        final Tunable tun = stringField.getAnnotation(Tunable.class);
        SyncTunableHandler syncHandler = new SyncTunableHandler(stringField, th, tun);

		Map<String,Object> map = new HashMap<String,Object>();
		map.put("tstring","hello");

		syncHandler.setValueMap(map);

		assertEquals("goodbye",th.tstring);

		syncHandler.handle();

		assertEquals("hello",th.tstring);
	}

}
