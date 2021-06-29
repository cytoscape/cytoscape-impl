package org.cytoscape.ding.impl.canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.UIManager;

import org.cytoscape.ding.debug.DebugFrameType;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;
import org.cytoscape.ding.impl.work.NoOutputProgressMonitor;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.GraphLOD;

@SuppressWarnings("serial")
public class BirdsEyeViewRenderComponent extends RenderComponent {

	private static final Dimension MIN_SIZE = new Dimension(180, 180);
	
	private final Color VIEW_WINDOW_COLOR;
	private final Color VIEW_WINDOW_BORDER_COLOR;
	
	public BirdsEyeViewRenderComponent(DRenderingEngine re, GraphLOD lod) {
		super(re, lod);
		
		setPreferredSize(MIN_SIZE);
		setMinimumSize(MIN_SIZE);
		
		Color c = UIManager.getColor("Table.focusCellBackground");
		VIEW_WINDOW_COLOR = new Color(c.getRed(), c.getGreen(), c.getBlue(), 60);
		c = UIManager.getColor("Table.background");
		VIEW_WINDOW_BORDER_COLOR = new Color(c.getRed(), c.getGreen(), c.getBlue(), 90);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawRectangle(g);
	}
	
	private void drawRectangle(Graphics g) {
		// extents of network viewable in birds-eye-view (node coords)
		double myXCenter = getTransform().getCenterX();
		double myYCenter = getTransform().getCenterY();
		double myScaleFactor = getTransform().getScaleFactor();
		
		// get extents of the main network view (node coords)
		double viewXCenter = re.getTransform().getCenterX();
		double viewYCenter = re.getTransform().getCenterY();
		double viewScaleFactor = re.getTransform().getScaleFactor();
		
		// Swing width/height of main view (image coords)
		int viewWidth  = re.getTransform().getWidth();
		int viewHeight = re.getTransform().getHeight();
		
		// Compute view area
		final int rectWidth  = (int) (myScaleFactor * (((double) viewWidth)  / viewScaleFactor));
		final int rectHeight = (int) (myScaleFactor * (((double) viewHeight) / viewScaleFactor));
		
		final double rectXCenter = (((double) getWidth())  / 2.0d) + (myScaleFactor * (viewXCenter - myXCenter));
		final double rectYCenter = (((double) getHeight()) / 2.0d) + (myScaleFactor * (viewYCenter - myYCenter));
		
		final int x = (int) (rectXCenter - (rectWidth  / 2));
		final int y = (int) (rectYCenter - (rectHeight / 2));
		
		// Draw the view area window		
		g.setColor(VIEW_WINDOW_COLOR);
		g.fillRect(x, y, rectWidth, rectHeight);
		g.setColor(VIEW_WINDOW_BORDER_COLOR);
		g.drawRect(x, y, rectWidth, rectHeight);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return MIN_SIZE;
	}

	@Override
	ProgressMonitor getSlowProgressMonitor() {
		return new NoOutputProgressMonitor();
	}
	
	@Override
	DebugFrameType getDebugFrameType(UpdateType type) {
		switch(type) {
			case JUST_ANNOTATIONS: return DebugFrameType.BEV_ANNOTAITONS;
			case ALL_FAST: return DebugFrameType.BEV_FAST;
			case ALL_FULL: return DebugFrameType.BEV_SLOW;
			default: return null;
		}
	}
	
}
