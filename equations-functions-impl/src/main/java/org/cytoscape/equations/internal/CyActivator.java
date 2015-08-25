package org.cytoscape.equations.internal;

import java.util.Properties;

/*
 * #%L
 * Cytoscape Equation Functions Impl (equations-functions-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.Function;
import org.cytoscape.equations.internal.functions.Degree;
import org.cytoscape.equations.internal.functions.InDegree;
import org.cytoscape.equations.internal.functions.OutDegree;
import org.cytoscape.equations.internal.functions.SourceID;
import org.cytoscape.equations.internal.functions.TargetID;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		final CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);

		final Function degree = new Degree(applicationManager);
		final Function inDegree = new InDegree(applicationManager);
		final Function outDegree = new OutDegree(applicationManager);
		final Function sourceId = new SourceID(applicationManager);
		final Function targetId = new TargetID(applicationManager);

		registerAllServices(bc, degree, new Properties());
		registerAllServices(bc, inDegree, new Properties());
		registerAllServices(bc, outDegree, new Properties());
		registerAllServices(bc, sourceId, new Properties());
		registerAllServices(bc, targetId, new Properties());
	}
}
