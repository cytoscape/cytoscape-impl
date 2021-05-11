package org.cytoscape.ding.impl;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.UIManager;
import javax.swing.event.SwingPropertyChangeSupport;

public class LabelSelectionManager {
	
	public static final String PROP_SELECTION = "selection";
	
	private final DRenderingEngine re;
	private Set<LabelSelection> selectedNodeLabels = new HashSet<>();
	
	private final SwingPropertyChangeSupport propChangeSupport = new SwingPropertyChangeSupport(this);
	
	
	public LabelSelectionManager(DRenderingEngine re) {
		this.re = re;
	}
	
	
	public void add(LabelSelection sel) {
		selectedNodeLabels.add(sel);
		fireSelectionChanged();
	}
	
	public void addAll(Collection<LabelSelection> nodeLabels) {
		selectedNodeLabels.addAll(nodeLabels);
		fireSelectionChanged();
	}
	
	public void clear() {
		selectedNodeLabels.clear();
		fireSelectionChanged();
	}
	
	
	public boolean isEmpty() {
		return selectedNodeLabels.isEmpty();
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
