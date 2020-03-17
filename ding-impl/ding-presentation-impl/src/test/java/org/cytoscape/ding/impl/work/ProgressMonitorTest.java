package org.cytoscape.ding.impl.work;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class ProgressMonitorTest {

	@Test
	public void testProgressMonitorSplit() {
		NoOutputProgressMonitor pm = new NoOutputProgressMonitor();
		List<ProgressMonitor> split = pm.split(1,1);
		ProgressMonitor pm1 = split.get(0);
		ProgressMonitor pm2 = split.get(1);
		
		pm1.addProgress(0.5);
		pm2.addProgress(0.5);
		assertEquals(0.5, pm.getProgress(), 0.0);
		
		pm1.done();
		assertEquals(0.75, pm.getProgress(), 0.0);
		
		pm2.done();
		assertEquals(1.0, pm.getProgress(), 0.0);
	}
	
	
	@Test
	public void testProgressMonitorZero() {
		NoOutputProgressMonitor pm = new NoOutputProgressMonitor();
		List<ProgressMonitor> split = pm.split(1,0);
		ProgressMonitor pm1 = split.get(0);
		ProgressMonitor pm2 = split.get(1);
		
		pm1.addProgress(0.5);
		pm2.addProgress(0.5);
		assertEquals(0.5, pm.getProgress(), 0.0);
		
		pm1.done();
		assertEquals(1.0, pm.getProgress(), 0.0);
		
		pm2.done();
		assertEquals(1.0, pm.getProgress(), 0.0);
	}
	
	@Test
	public void testProgressMonitorSplitDiscrete() {
		NoOutputProgressMonitor pm = new NoOutputProgressMonitor();
		List<ProgressMonitor> split = pm.split(1,1);
		DiscreteProgressMonitor pm1 = split.get(0).toDiscrete(100);
		DiscreteProgressMonitor pm2 = split.get(1).toDiscrete(100);
		
		pm1.addWork(50);
		pm2.addWork(50);
		assertEquals(0.5, pm.getProgress(), 0.0);
		
		pm1.workFinished();
		assertEquals(0.75, pm.getProgress(), 0.0);
		
		pm2.workFinished();
		assertEquals(1.0, pm.getProgress(), 0.0);
	}
}
