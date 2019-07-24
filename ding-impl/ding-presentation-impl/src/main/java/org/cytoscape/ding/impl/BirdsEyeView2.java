package org.cytoscape.ding.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.print.Printable;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;

public class BirdsEyeView2 extends JComponent implements RenderingEngine<CyNetwork> {

	private static final Dimension MIN_SIZE = new Dimension(180, 180);
	private final Color VIEW_WINDOW_COLOR;
	private final Color VIEW_WINDOW_BORDER_COLOR;
	
	
	// Ratio of the graph image to panel size 
	private static final double SCALE_FACTOR = 0.97;
		
	private final DRenderingEngine re;
	private final CompositeCanvas compositeCanvas;
	
	
	public BirdsEyeView2(DRenderingEngine re, CyServiceRegistrar registrar) {
		this.re = re;
		
		Color c = UIManager.getColor("Table.focusCellBackground");
		VIEW_WINDOW_COLOR = new Color(c.getRed(), c.getGreen(), c.getBlue(), 60);
		c = UIManager.getColor("Table.background");
		VIEW_WINDOW_BORDER_COLOR = new Color(c.getRed(), c.getGreen(), c.getBlue(), 90);
		
		setPreferredSize(MIN_SIZE);
		setMinimumSize(MIN_SIZE);
		
		BirdsEyeViewLOD lod = getLOD();
		compositeCanvas = new CompositeCanvas(registrar, re, re.dingLock, lod);
	}	
	
	private BirdsEyeViewLOD getLOD() {
		// Now draw the network
		// System.out.println("Drawing snapshot");
		BirdsEyeViewLOD bevLOD;
		if (re.getGraphLOD() instanceof DingGraphLOD)
			bevLOD = new BirdsEyeViewLOD(new DingGraphLOD((DingGraphLOD)re.getGraphLOD()));
		else
			bevLOD = new BirdsEyeViewLOD(re.getGraphLOD());

		bevLOD.setDrawEdges(true);
		return bevLOD;
	}
	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		compositeCanvas.setViewport(width, height);
	}
	
	@Override
	public void paint(Graphics g) {
	}
	
	
	@Override
	public Dimension getMinimumSize() {
		return MIN_SIZE;
	}

	@Override
	public View<CyNetwork> getViewModel() {
		return re.getViewModel();
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return re.getVisualLexicon();
	}

	@Override
	public Properties getProperties() {
		return re.getProperties();
	}
	
	@Override
	public Printable createPrintable() {
		return re.createPrintable();
	}

	@Override
	public <V> Icon createIcon(VisualProperty<V> vp, V value, int width, int height) {
		return re.createIcon(vp, value, width, height);
	}

	@Override
	public void printCanvas(Graphics printCanvas) {
		throw new UnsupportedOperationException("Printing is not supported for Bird's eye view.");
	}


	@Override
	public void dispose() {
	}

	@Override
	public String getRendererId() {
		return DingRenderer.ID;
	}
	
}
