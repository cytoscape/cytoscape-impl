package org.cytoscape.ding.impl.cyannotator.api;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.Set;
import java.util.Map;
import java.util.UUID;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation;


public interface Annotation {
	public static final String BACKGROUND =  "background";
	public static final String FOREGROUND =  "foreground";

	public String getCanvasName();
	public void setCanvas(String canvas);
	public void changeCanvas(String canvas);
	public JComponent getCanvas();

	public JComponent getComponent();
	public void addComponent(JComponent canvas);

	public UUID getUUID();

	public CyAnnotator getCyAnnotator();

	public void moveAnnotation(Point2D location);
	public void removeAnnotation();

	public double getZoom();
	public void setZoom(double zoom);

	public double getSpecificZoom();
	public void setSpecificZoom(double zoom);

	public boolean isSelected();
	public void setSelected(boolean selected);

	public void addArrow(ArrowAnnotation arrow);
	public void removeArrow(ArrowAnnotation arrow);
	public Set<ArrowAnnotation> getArrows();

	public Map<String,String> getArgMap();

	public void drawAnnotation(Graphics g, double x, 
	                           double y, double scaleFactor);

	public boolean usedForPreviews();
	public void setUsedForPreviews(boolean v);

	public void update();

	public JFrame getModifyDialog();

	// Overrides of Component
	public void paint(Graphics g);
}
