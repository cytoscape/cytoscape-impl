package org.cytoscape.filter.internal;

import static org.junit.Assert.*;

import org.cytoscape.filter.internal.work.DiscreteProgressMonitor;
import org.cytoscape.filter.internal.work.ProgressMonitor;
import org.cytoscape.filter.internal.work.SubProgressMonitor;
import org.junit.Test;


public class PerformanceMonitorTest {

	@Test
	public void testSubMonitors() {
		TestProgressMonitor pm = new TestProgressMonitor();
		
		SubProgressMonitor spm = new SubProgressMonitor(pm, .25, .75);
		
		spm.setProgress(.0);
		assertEquals(.25, pm.progress, .001);
		
		spm.setProgress(.5);
		assertEquals(.5,  pm.progress, .001);
		
		spm.setProgress(1.0);
		assertEquals(.75, pm.progress, .001);
		
		spm.setStatusMessage("Blah");
		spm.cancel();
		assertEquals("Blah", pm.message);
		assertTrue(pm.cancelled);
	}
	
	
	@Test
	public void testDiscreteMonitors() {
		TestProgressMonitor pm = new TestProgressMonitor();
		
		DiscreteProgressMonitor dpm = new DiscreteProgressMonitor(pm, .2, .4);
		
		dpm.setProgress(.0);
		assertEquals(.2, pm.progress, .001);
		
		dpm.setProgress(.5);
		assertEquals(.3, pm.progress, .001);
		
		dpm.setTotalWork(20);
		
		dpm.setWork(0);
		assertEquals(.2, pm.progress, .001);
		
		dpm.setWork(10);
		assertEquals(.3, pm.progress, .001);
		
		dpm.addWork(10);
		assertEquals(.4, pm.progress, .001);
	}
	
	@Test
	public void testJustStart() {
		TestProgressMonitor pm = new TestProgressMonitor();
		DiscreteProgressMonitor dpm1 = new DiscreteProgressMonitor(pm, .0, .25);
		DiscreteProgressMonitor dpm2 = new DiscreteProgressMonitor(pm, .25, .5);
		DiscreteProgressMonitor dpm3 = new DiscreteProgressMonitor(pm, .5, .75);
		DiscreteProgressMonitor dpm4 = new DiscreteProgressMonitor(pm, .75, 1.0);
		
		dpm1.start();
		assertEquals(.0,  pm.progress, 0.001);
		dpm2.start();
		assertEquals(.25, pm.progress, 0.001);
		dpm3.start();
		assertEquals(.5,  pm.progress, 0.001);
		dpm4.start();
		assertEquals(.75, pm.progress, 0.001);
		dpm4.done();
		assertEquals(1.0, pm.progress, 0.001);
	}
	
	private static class TestProgressMonitor implements ProgressMonitor {
		boolean cancelled;
		double progress;
		String message;
			
		@Override
		public void cancel() {
			cancelled = true;
		}

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public void setProgress(double progress) {
			this.progress = progress;
		}

		@Override
		public void setStatusMessage(String message) {
			this.message = message;
		}
	}
	
}
