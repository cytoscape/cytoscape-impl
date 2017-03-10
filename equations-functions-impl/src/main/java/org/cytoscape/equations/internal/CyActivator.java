package org.cytoscape.equations.internal;

import java.util.Properties;

import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.Function;
import org.cytoscape.equations.internal.functions.Degree;
import org.cytoscape.equations.internal.functions.InDegree;
import org.cytoscape.equations.internal.functions.OutDegree;
import org.cytoscape.equations.internal.functions.SourceID;
import org.cytoscape.equations.internal.functions.TargetID;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Equation Functions Impl (equations-functions-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2016 The Cytoscape Consortium
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

public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);

		final Function degree = new Degree(serviceRegistrar);
		final Function inDegree = new InDegree(serviceRegistrar);
		final Function outDegree = new OutDegree(serviceRegistrar);
		final Function sourceId = new SourceID(serviceRegistrar);
		final Function targetId = new TargetID(serviceRegistrar);
		
		final CyEventHelper eventHelper = getService(bc, CyEventHelper.class);
		final EquationParser parser = getService(bc, EquationParser.class);
		eventHelper.silenceEventSource(parser);
		
		registerAllServices(bc, degree, new Properties());
		registerAllServices(bc, inDegree, new Properties());
		registerAllServices(bc, outDegree, new Properties());
		registerAllServices(bc, sourceId, new Properties());
		registerAllServices(bc, targetId, new Properties());
		
		eventHelper.unsilenceEventSource(parser);
	}
}
