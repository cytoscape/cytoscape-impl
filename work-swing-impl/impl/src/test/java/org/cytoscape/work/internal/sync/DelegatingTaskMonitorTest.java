
package org.cytoscape.work.internal.sync;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.junit.Test;

public class DelegatingTaskMonitorTest {

	TestTaskMonitor ttm;
	DelegatingTaskMonitor dtm;

	private void setup(int numTasks) {
		ttm = new TestTaskMonitor();
		dtm = new DelegatingTaskMonitor(ttm, numTasks);
	}

	@Test
	public void testSetTitle() {
		setup(1);
		dtm.setTitle("hello");
		assertEquals( "hello", ttm.getTitle());
	}

	@Test
	public void testSetMessage() {
		setup(1);
		dtm.setStatusMessage("goodbye");
		assertEquals( "goodbye", ttm.getStatusMessage());
	}

	@Test
	public void testProgressTwo() {
		setup(2);
		dtm.setTask(null);
		dtm.setProgress(0.5);
		dequals(0.25,ttm.getProgress());
		dtm.setProgress(1.0);
		dequals(0.5,ttm.getProgress());
		dtm.setTask(null);
		dtm.setProgress(0.5);
		dequals(0.75,ttm.getProgress());
		dtm.setProgress(1.0);
		dequals(1.0,ttm.getProgress());
	}

	@Test
	public void testProgressOne() {
		setup(1);
		dtm.setTask(null);
		dtm.setProgress(0.5);
		dequals(0.5,ttm.getProgress());
		dtm.setProgress(1.0);
		dequals(1.0,ttm.getProgress());
	}

	@Test
	public void testProgressFour() {
		setup(4);
		dtm.setTask(null);
		dtm.setProgress(1.0);
		dequals(0.25,ttm.getProgress());

		dtm.setTask(null);
		dtm.setProgress(1.0);
		dequals(0.5,ttm.getProgress());

		dtm.setTask(null);
		dtm.setProgress(1.0);
		dequals(0.75,ttm.getProgress());

		dtm.setTask(null);
		dtm.setProgress(1.0);
		dequals(1.0,ttm.getProgress());
	}

	private void dequals(double a, double b) {
		assertTrue((Math.abs(a-b) < 0.0000001));
	}

	private class TestTaskMonitor implements TaskMonitor {
		private String title;
		private String msg;
		private double progress;
		public void setTitle(String t) { title = t;	}
		public void setStatusMessage(String m) { msg = m; }
		public void setProgress(double p) { progress = p; }
		public String getTitle() { return title; }
		public String getStatusMessage() { return msg; }
		public double getProgress() { return progress; }
	}
}
