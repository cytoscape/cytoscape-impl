package org.cytoscape.ding.impl.canvas;

import java.awt.FontMetrics;
import java.awt.Graphics;

import org.cytoscape.ding.debug.DebugFrameType;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

@SuppressWarnings("serial")
public class MainRenderComponent extends RenderComponent {

	private FontMetrics fontMetrics;
	
	public MainRenderComponent(DRenderingEngine re, DingGraphLOD lod) {
		super(re, lod);
	}
	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		if(width == getWidth() && height == getHeight())
			return;
		
		super.setBounds(x, y, width, height);
		
		re.setTransformChanged();
		
		re.getViewModel().batch(netView -> {
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH,  (double) width);
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT, (double) height);
		}, false); // don't set the dirty flag
	}

	@Override
	public void update(Graphics g) {
		if(fontMetrics == null) {
			fontMetrics = g.getFontMetrics(); // needed to compute label widths
		}
		super.update(g);
	}
	
	public FontMetrics getFontMetrics() {
		return fontMetrics;
	}
	
	@Override
	protected void updateThumbnail(ImageFuture future) {
//		re.fireThumbnailChanged(future.join());
	}
	
	@Override
	protected ProgressMonitor getSlowProgressMonitor() {
		return re.getInputHandlerGlassPane().createProgressMonitor();
	}
	
	@Override
	protected void setRenderDetailFlags(RenderDetailFlags flags) {
		re.getPicker().setRenderDetailFlags(flags);
	}

	@Override
	DebugFrameType getDebugFrameType(UpdateType type) {
		switch(type) {
			case ALL_FAST: return DebugFrameType.MAIN_FAST;
			case ALL_FULL: return DebugFrameType.MAIN_SLOW;
			case JUST_ANNOTATIONS: return DebugFrameType.MAIN_ANNOTAITONS;
			default: return null;
		}
	}
}