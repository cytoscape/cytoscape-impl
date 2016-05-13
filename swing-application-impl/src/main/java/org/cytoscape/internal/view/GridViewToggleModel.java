package org.cytoscape.internal.view;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

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

public class GridViewToggleModel {

	public enum Mode { GRID, VIEW };
	
	private Mode mode;
	
	private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	
	public GridViewToggleModel(final Mode mode) {
		this.mode = mode;
	}
	
	public void setMode(final Mode newValue) {
		if (mode != newValue) {
			final Mode oldValue = mode;
			mode = newValue;
			changeSupport.firePropertyChange("mode", oldValue, newValue);
		}
	}
	
	public Mode getMode() {
		return mode;
	}
	
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
