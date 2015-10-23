package org.cytoscape.scripting.internal;

/*
 * #%L
 * Cytoscape Scripting Impl (scripting-impl)
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

import static org.cytoscape.work.ServiceProperties.*;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		final CommandExecutorTaskFactory commandExecutorTaskFactoryServiceRef = getService(bc, CommandExecutorTaskFactory.class);
		
		// This object should be injected to all scripts to access manager objects from scripts.
		final CyAppAdapter appAdapter = getService(bc, CyAppAdapter.class);
		
		final ExecuteScriptTaskFactory executeScriptTaskFactory = new ExecuteScriptTaskFactory(appAdapter, commandExecutorTaskFactoryServiceRef);

		final Properties executeScriptTaskFactoryProps = new Properties();
		executeScriptTaskFactoryProps.setProperty(ID, "executeScriptTaskFactory");
		executeScriptTaskFactoryProps.setProperty(PREFERRED_MENU, "File");
		executeScriptTaskFactoryProps.setProperty(MENU_GRAVITY, "6.1f");
		executeScriptTaskFactoryProps.setProperty(TITLE, "Run Script File...");
		executeScriptTaskFactoryProps.setProperty("inToolBar", "false");

		registerAllServices(bc, executeScriptTaskFactory, executeScriptTaskFactoryProps);
	}
}
