package org.cytoscape.view.vizmap.gui.internal.view;

import java.beans.PropertyChangeListener;

import javax.swing.event.SwingPropertyChangeSupport;

public abstract class AbstractVizMapperModel {

	final protected SwingPropertyChangeSupport propChangeSupport;

	public AbstractVizMapperModel() {
		propChangeSupport = new SwingPropertyChangeSupport(this, true);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propChangeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return propChangeSupport.getPropertyChangeListeners();
	}

	public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
		return propChangeSupport.getPropertyChangeListeners(propertyName);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propChangeSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propChangeSupport.removePropertyChangeListener(propertyName, listener);
	}
	
	public boolean hasListeners(String propertyName) {
		return propChangeSupport.hasListeners(propertyName);
	}
	
//	public void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
//		propChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
//	}
//
//	public void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
//		propChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
//	}
//
//	public void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
//		propChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
//	}
//
//	public void firePropertyChange(PropertyChangeEvent evt) {
//		propChangeSupport.firePropertyChange(evt);
//	}
//
//	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
//		propChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
//	}
//
//	public void firePropertyChange(String propertyName, int oldValue, int newValue) {
//		propChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
//	}
//
//	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
//		propChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
//	}
}
