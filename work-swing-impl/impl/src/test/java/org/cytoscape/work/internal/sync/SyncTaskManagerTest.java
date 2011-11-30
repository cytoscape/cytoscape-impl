package org.cytoscape.work.internal.sync;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.cytoscape.work.*;

import java.lang.reflect.Field;
import java.util.*;

public class SyncTaskManagerTest {

	@Test
	public void testTaskManager() {
		SyncTunableMutator stm = new SyncTunableMutator();
		stm.addTunableHandlerFactory( new SyncTunableHandlerFactory(), new Properties() );
		SyncTaskManager taskManager = new SyncTaskManager(stm);

		Map<String,Object> map = new HashMap<String,Object>();
		map.put("tstring","hello");

		taskManager.setExecutionContext(map);

		TestTaskFactory tf = new TestTaskFactory();
		
		taskManager.execute(tf);
	}

	public class TestTaskFactory implements TaskFactory {
		public TaskIterator createTaskIterator() { return new TaskIterator( new TestTask() ); }
	}

	public class TestTask extends AbstractTask {
		@Tunable(description="asdf")
		public String tstring = "goodbye";

		public void run(TaskMonitor tm) {
			assertEquals("hello",tstring);
		}
	}
}
