package org.cytoscape.internal.view;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;

import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


public abstract class AbstractNetworkPanelModel<T extends CyNetwork> {

	private T network;
	private boolean current;
	
	protected final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	protected final CyServiceRegistrar serviceRegistrar;

	protected AbstractNetworkPanelModel(final T network, final CyServiceRegistrar serviceRegistrar) {
		if (network == null)
			throw new IllegalArgumentException("'subNetwork' must not be null.");
		if (serviceRegistrar == null)
			throw new IllegalArgumentException("'serviceRegistrar' must not be null.");
		
		this.network = network;
		this.serviceRegistrar = serviceRegistrar;
	}

	public T getNetwork() {
		return network;
	}
	
	public Collection<CyNetworkView> getNetworkViews() {
		return serviceRegistrar.getService(CyNetworkViewManager.class).getNetworkViews(getNetwork());
	}
	
	public int getViewCount() {
		return getNetworkViews().size();
	}
	
	public int getNodeCount() {
		return getNetwork().getNodeCount();
	}
	
	public int getEdgeCount() {
		return getNetwork().getEdgeCount();
	}
	
	public String getNetworkName() {
		return ViewUtil.getName(getNetwork());
	}

	public boolean isCurrent() {
		return current;
	}
	
	public void setCurrent(final boolean newValue) {
		if (current != newValue) {
			final boolean oldValue = current;
			current = newValue;
			changeSupport.firePropertyChange("current", oldValue, newValue);
		}
	}
	
	public abstract int getSubNetworkCount();
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}
}
