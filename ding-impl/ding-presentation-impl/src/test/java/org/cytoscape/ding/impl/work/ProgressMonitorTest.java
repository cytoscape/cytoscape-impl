package org.cytoscape.ding.impl.work;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.cytoscape.ding.debug.DebugFrameType;
import org.cytoscape.ding.debug.DebugRootProgressMonitor;
import org.cytoscape.ding.debug.DebugSubProgressMonitor;
import org.junit.Test;

public class ProgressMonitorTest {

	@Test
	public void testProgressMonitorSplit() {
		NoOutputProgressMonitor pm = new NoOutputProgressMonitor();
		ProgressMonitor[] split = pm.split(1,1);
		ProgressMonitor pm1 = split[0];
		ProgressMonitor pm2 = split[1];
		
		pm1.addProgress(0.5);
		pm2.addProgress(0.5);
		assertEquals(0.5, pm.getProgress(), 0.0);
		
		pm1.addProgress(0.5);
		assertEquals(0.75, pm.getProgress(), 0.0);
		
		pm2.addProgress(0.5);
		assertEquals(1.0, pm.getProgress(), 0.0);
	}
	
	
	@Test
	public void testProgressMonitorZero() {
		NoOutputProgressMonitor pm = new NoOutputProgressMonitor();
		ProgressMonitor[] split = pm.split(1,0);
		ProgressMonitor pm1 = split[0];
		ProgressMonitor pm2 = split[1];
		
		pm1.addProgress(0.5);
		pm2.addProgress(0.5);
		assertEquals(0.5, pm.getProgress(), 0.0);
		
		pm1.addProgress(0.5);
		assertEquals(1.0, pm.getProgress(), 0.0);
	}
	
	@Test
	public void testProgressMonitorSplitDiscrete() {
		NoOutputProgressMonitor pm = new NoOutputProgressMonitor();
		ProgressMonitor[] split = pm.split(1,1);
		DiscreteProgressMonitor pm1 = split[0].toDiscrete(100);
		DiscreteProgressMonitor pm2 = split[1].toDiscrete(100);
		
		pm1.addWork(50);
		pm2.addWork(50);
		assertEquals(0.5, pm.getProgress(), 0.0);
		
		pm1.workFinished();
		assertEquals(0.75, pm.getProgress(), 0.0);
		
		pm2.workFinished();
		assertEquals(1.0, pm.getProgress(), 0.0);
	}
	
	@Test
	public void testDebugProgressMonitorSplitInterleaved() throws Exception {
		NoOutputProgressMonitor nopm = new NoOutputProgressMonitor();
		DebugRootProgressMonitor pm = new DebugRootProgressMonitor(DebugFrameType.MAIN_FAST, nopm, null);
		ProgressMonitor[] split = pm.split(1,1);
		DebugSubProgressMonitor pm1 = (DebugSubProgressMonitor) split[0];
		DebugSubProgressMonitor pm2 = (DebugSubProgressMonitor) split[1];
		
		pm1.start("A");
		Thread.sleep(100);
		pm1.addProgress(0.25);
		pm1.done();
		long pm1t1 = pm1.getTime();
		assertTrue(pm1t1 > 0);
		
		pm2.start("B");
		Thread.sleep(100);
		pm2.addProgress(0.25);
		pm2.done();
		long pm2t1 = pm2.getTime();
		assertTrue(pm2t1 > 0);
		
		pm1.start("A");
		Thread.sleep(100);
		pm1.addProgress(0.25);
		pm1.done();
		long pm1t2 = pm1.getTime();
		assertTrue(pm1t2 > pm1t1);
		
		pm2.start("B");
		Thread.sleep(100);
		pm2.addProgress(0.25);
		pm2.done();
		long pm2t2 = pm2.getTime();
		assertTrue(pm2t2 > pm2t1);
		
		assertEquals(0.5, nopm.getProgress(), 0.0);
	}
}
