package org.cytoscape.ding.impl;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.UIManager;
import javax.swing.event.SwingPropertyChangeSupport;

import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;


public class LabelSelectionManager {
	
	public static final String PROP_SELECTION = "selection";
	
	private final DRenderingEngine re;
	private final SwingPropertyChangeSupport propChangeSupport = new SwingPropertyChangeSupport(this);
	
	private Set<LabelSelection> selectedNodeLabels = new HashSet<>();
	private Set<LabelSelection> selectedEdgeLabels = new HashSet<>();
	private LabelSelection primarySelection;
	private Point currentDragPoint;
	
	
	public LabelSelectionManager(DRenderingEngine re) {
		this.re = re;
	}
	
	public void add(LabelSelection sel) {
		boolean changed = false;
    if (sel.getNode() != null)
      changed = selectedNodeLabels.add(sel);
    else
      changed = selectedEdgeLabels.add(sel);

		if (changed)
			fireSelectionChanged();
	}
	
	public void remove(LabelSelection sel) {
		boolean changed = false;
    if (sel.getNode() != null)
      changed = selectedNodeLabels.remove(sel);
    else
      changed = selectedEdgeLabels.remove(sel);

		if (changed)
			fireSelectionChanged();
	}
	
	public void clear() {
    boolean changed = false;
		if(!selectedNodeLabels.isEmpty()) {
			selectedNodeLabels.clear();
      changed = true;
		}
		if(!selectedEdgeLabels.isEmpty()) {
			selectedEdgeLabels.clear();
      changed = true;
    }

    if (changed)
			fireSelectionChanged();
	}
	
	// The last clicked on label, used to make rotating consistent for all selected labels.
	public void setPrimary(LabelSelection sel) {
		this.primarySelection = sel;
	}
		
	public Collection<LabelSelection> getSelectedNodeLabels() {
		return selectedNodeLabels;
	}
		
	public Collection<LabelSelection> getSelectedEdgeLabels() {
		return selectedEdgeLabels;
	}
	
	public boolean isEmpty() {
		return selectedNodeLabels.isEmpty() && selectedEdgeLabels.isEmpty();
	}
	
	public boolean contains(LabelSelection sel) {
    if (sel.getNode() != null)
      return selectedNodeLabels.contains(sel);
    else
      return selectedEdgeLabels.contains(sel);
	}
	
	public void setCurrentDragPoint(Point offset) {
		this.currentDragPoint = offset;
	}
	

	public void move(Point p) {
		var transform = re.getTransform();
		var nodePt = transform.getNodeCoordinates(p); 
		var offsetPt = transform.getNodeCoordinates(currentDragPoint);

		double dx = nodePt.getX() - offsetPt.getX();
		double dy = nodePt.getY() - offsetPt.getY();

    selectedNodeLabels.forEach(sl -> sl.translate(dx, dy));
    selectedEdgeLabels.forEach(sl -> sl.translate(dx, dy));
		currentDragPoint = p;
	}
	
	
	public void rotate(Point p) {
		if(primarySelection == null)
			return ;
		
		double anchorX = primarySelection.getAnchorX();
		double anchorY = primarySelection.getAnchorY();
		
		var transform = re.getTransform();
		var pt1 = transform.getNodeCoordinates(currentDragPoint);
		var pt2 = transform.getNodeCoordinates(p);
		
		// radians
		double angle1 = Math.atan2(pt1.getY() - anchorY, pt1.getX() - anchorX);
		double angle2 = Math.atan2(pt2.getY() - anchorY, pt2.getX() - anchorX);
		
		double angle = angle2 - angle1;
		
		selectedNodeLabels.forEach(sl -> sl.rotate(angle));
		selectedEdgeLabels.forEach(sl -> sl.rotate(angle));
	}
	
	
	public void paint(Graphics2D graphics) {
		Graphics2D g = (Graphics2D) graphics.create();
		g.setColor(UIManager.getColor("Focus.color"));
		
		var transform = re.getTransform().getWindowAffineTransform();
		var dpiScale  = re.getTransform().getDpiScaleFactor();
		g.scale(dpiScale, dpiScale);
		
		// Draw selection rectangle
		for(LabelSelection sel : selectedNodeLabels) {
			Shape shape = sel.getShape();
			var transformedShape = transform.createTransformedShape(shape);
			g.draw(transformedShape);
		}

		for(LabelSelection sel : selectedEdgeLabels) {
			Shape shape = sel.getShape();
			var transformedShape = transform.createTransformedShape(shape);
			g.draw(transformedShape);
    }
	}

	
	private void fireSelectionChanged() {
		re.updateView(UpdateType.ALL_FULL, true);
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
