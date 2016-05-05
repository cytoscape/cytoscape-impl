package org.cytoscape.work.internal.sync;

/*
 * #%L
 * org.cytoscape.work-impl
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
		
		Map<String, Object> settings = new HashMap<>();
		settings.put("score", 10D);
		
		setter.applyTunables(object, settings);
		
		Assert.assertEquals(10D, object.score);		
	}
	
	@Test
	public void testCreateTaskIterator() {
		TunableTask[] tasks = new TunableTask[] { new FirstTask(), new LastTask() };
		TaskIterator taskIterator = new TaskIterator(tasks);
		
		Map<String, Object> settings = new HashMap<>();
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
