package org.cytoscape.task.internal.proxysettings;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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


import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;


public class ProxySettingsTaskFactoryImpl extends AbstractTaskFactory {
	
	
	private CyProperty<Properties> proxyProperties;
	private final StreamUtil streamUtil;
	private final CyEventHelper eventHelper;
	
	public ProxySettingsTaskFactoryImpl(CyProperty<Properties> proxyProperties, StreamUtil streamUtil, CyEventHelper eventHelper) {
		this.proxyProperties = proxyProperties;
		this.streamUtil = streamUtil;
		this.eventHelper = eventHelper;

        // Force reading of proxy properties to assign System properties
        (new ProxySettingsTask2(proxyProperties, streamUtil, eventHelper)).assignSystemProperties();
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(2,new ProxySettingsTask(proxyProperties, streamUtil, eventHelper));
	}
}
