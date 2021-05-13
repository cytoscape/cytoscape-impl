package org.cytoscape.ding.impl;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.beans.PropertyChangeListener;

import javax.swing.UIManager;
import javax.swing.event.SwingPropertyChangeSupport;


public class LabelSelectionManager {
	
	public static final String PROP_SELECTION = "selection";
	
	private final DRenderingEngine re;
	private final SwingPropertyChangeSupport propChangeSupport = new SwingPropertyChangeSupport(this);
	
//	private Set<LabelSelection> selectedNodeLabels = new HashSet<>();
	private LabelSelection selectedLabel;
	private Point currentDragPoint;
	
	
	public LabelSelectionManager(DRenderingEngine re) {
		this.re = re;
	}
	
	public void set(LabelSelection selectedLabel) {
		this.selectedLabel = selectedLabel;
		fireSelectionChanged();
	}
	
	public void clear() {
		this.selectedLabel = null;
		fireSelectionChanged();
	}
	
	public boolean isEmpty() {
		return selectedLabel == null;
	}
	
	public LabelSelection getSelectedLabel() {
		return selectedLabel;
	}
	
//	public void set(LabelSelection sel) {
//		selectedNodeLabels.add(sel);
//		fireSelectionChanged();
//	}
//	
//	public void remove(LabelSelection sel) {
//		selectedNodeLabels.remove(sel);
//		fireSelectionChanged();
//	}
//	
//	public void addAll(Collection<LabelSelection> nodeLabels) {
//		selectedNodeLabels.addAll(nodeLabels);
//		fireSelectionChanged();
//	}
//	
//	public void clear() {
//		selectedNodeLabels.clear();
//		fireSelectionChanged();
//	}
//	
//	public boolean isEmpty() {
//		return selectedNodeLabels.isEmpty();
//	}
//	
//	public boolean contains(LabelSelection sel) {
//		return selectedNodeLabels.contains(sel);
//	}
	
	
	public void setCurrentDragPoint(Point offset) {
		this.currentDragPoint = offset;
	}
	
	public void move(Point p) {
		var transform = re.getTransform();
		var nodePt = transform.getNodeCoordinates(p);
		var offsetPt = transform.getNodeCoordinates(currentDragPoint);

		double dx = nodePt.getX() - offsetPt.getX();
		double dy = nodePt.getY() - offsetPt.getY();
		
		selectedLabel.translate(dx, dy);
		currentDragPoint = p;
	}
	
	
	public void rotate(Point p) {
		double anchorX = selectedLabel.getAnchorX();
		double anchorY = selectedLabel.getAnchorY();
		
		var transform = re.getTransform();
		var pt1 = transform.getNodeCoordinates(currentDragPoint);
		var pt2 = transform.getNodeCoordinates(p);
		
		// radians
		double angle1 = Math.atan2(pt1.getY() - anchorY, pt1.getX() - anchorX);
		double angle2 = Math.atan2(pt2.getY() - anchorY, pt2.getX() - anchorX);
		
		double angle = angle2 - angle1;
		
		selectedLabel.rotate(angle);
	}
	
	
	public void paint(Graphics2D graphics) {
		Graphics2D g = (Graphics2D) graphics.create();
		g.setColor(UIManager.getColor("Focus.color"));
		
		var transform = re.getTransform().getWindowAffineTransform();
		var dpiScale  = re.getTransform().getDpiScaleFactor();
		g.scale(dpiScale, dpiScale);
		
//		// Draw selection rectangle
//		for(LabelSelection sel : selectedNodeLabels) {
//			Shape shape = sel.getShape();
//			var transformedShape = transform.createTransformedShape(shape);
//			g.draw(transformedShape);
//		}
		
		Shape shape = selectedLabel.getShape();
		var transformedShape = transform.createTransformedShape(shape);
		g.draw(transformedShape);
	}

	
	private void fireSelectionChanged() {
		re.setContentChanged();
		propChangeSupport.firePropertyChange(PROP_SELECTION, null, null);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propChangeSupport.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propChangeSupport.removePropertyChangeListener(propertyName, listener);
	}

}
