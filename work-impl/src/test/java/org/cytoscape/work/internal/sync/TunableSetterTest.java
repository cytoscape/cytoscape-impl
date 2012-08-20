package org.cytoscape.work.internal.sync;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableSetter;
import org.junit.Before;
import org.junit.Test;

public class TunableSetterTest {
	private TunableSetter setter;
	private SyncTunableMutatorFactory mutatorFactory;

	@Before
	public void setUp() {
		mutatorFactory = new SyncTunableMutatorFactory(new SyncTunableHandlerFactory());
		
		TunableRecorderManager recorderManager = new TunableRecorderManager();
		setter = new TunableSetterImpl(mutatorFactory, recorderManager);
	}
	
	@Test
	public void testApplyTunables() {
		TestObject object = new TestObject();
		Assert.assertEquals(5D, object.score);
		
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("score", 10D);
		
		setter.applyTunables(object, settings);
		
		Assert.assertEquals(10D, object.score);		
	}
	
	@Test
	public void testCreateTaskIterator() {
		TunableTask[] tasks = new TunableTask[] { new FirstTask(), new LastTask() };
		TaskIterator taskIterator = new TaskIterator(tasks);
		
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("firstTask", "new value1");
		settings.put("lastTask", "new value2");
		TaskIterator taskIterator2 = setter.createTaskIterator(taskIterator, settings);
		
		SyncTunableMutator<?> mutator = mutatorFactory.createMutator();
		SyncTaskManager taskManager = new SyncTaskManager(mutator);
		taskManager.execute(taskIterator2);
		
		
		// Ensure tasks were actually run
		for (TunableTask task : tasks) {
			Assert.assertTrue(task.result);
		}
	}
	
	public static class TestObject {
		@Tunable
		public Double score = 5D;
	}
	
	public static abstract class TunableTask extends AbstractTask {
		boolean result;
	}
	
	public static class FirstTask extends TunableTask {
		@Tunable
		public String firstTask = "old value";
		
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			Assert.assertEquals("new value1", firstTask);
			result = true;
		}
	}

	public static class LastTask extends TunableTask {
		@Tunable
		public String lastTask = "old value";
		
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			Assert.assertEquals("new value2", lastTask);
			result = true;
		}
	}
}
