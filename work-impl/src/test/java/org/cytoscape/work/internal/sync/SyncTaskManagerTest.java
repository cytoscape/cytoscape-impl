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

		Map<String,Object> map = new HashMap<>();
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
