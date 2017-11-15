package org.cytoscape.internal.tunable;

import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class CyPropertyConfirmation {

	private final String propertyName;
	
	private boolean confirmed;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public CyPropertyConfirmation(String propertyName, CyServiceRegistrar serviceRegistrar) {
		this.propertyName = propertyName;
		this.serviceRegistrar = serviceRegistrar;
	}

	public String getPropertyValue() {
		return getCyProperty().getProperties().getProperty(propertyName, "");
	}

	public void setPropertyValue(String propertyValue) {
		getCyProperty().getProperties().setProperty(propertyName, propertyValue);
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public String getPropertyName() {
		return propertyName;
	}
	
	public CyServiceRegistrar getServiceRegistrar() {
		return serviceRegistrar;
	}
	
	@SuppressWarnings("unchecked")
	private CyProperty<Properties> getCyProperty() {
		return serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
	}
}
