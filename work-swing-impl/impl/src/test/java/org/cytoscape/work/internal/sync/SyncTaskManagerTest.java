package org.cytoscape.work.internal.sync;


import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.junit.Test;

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
		
		taskManager.execute(tf.createTaskIterator());
	}

	public class TestTaskFactory extends AbstractTaskFactory {
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
