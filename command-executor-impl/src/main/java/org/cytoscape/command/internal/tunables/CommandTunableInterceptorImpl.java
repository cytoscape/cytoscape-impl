package org.cytoscape.command.internal.tunables;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
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

import java.util.Map;

import org.cytoscape.work.AbstractTunableInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandTunableInterceptorImpl extends AbstractTunableInterceptor<StringTunableHandler> {

	private static final Logger logger = LoggerFactory.getLogger(CommandTunableInterceptorImpl.class);
	private String args;

	public void setConfigurationContext(Object args) {
		this.args = (String)args;
	}

	public boolean validateAndWriteBackTunables(Object o) {
		try {
			// Get the handlers for the tunables in the Task.  The
			// key is the name of the tunable and the value is the handler
			// for that tunable.
			for ( StringTunableHandler h : getHandlers(o) ) {

				// Give the handler the arg string and let it do its thing,
				// which will hopefully be: set the tunable value based on
				// information parsed from the arg string.
				h.processArgString(args);
			}
		} catch (Exception e) {
			logger.warn("Exception processing tunables", e);
		}
		return true;
	}

	public void addTunableHandlerFactory(StringTunableHandlerFactory f, Map p) {
		super.addTunableHandlerFactory(f,p);
	}
	public void removeTunableHandlerFactory(StringTunableHandlerFactory f, Map p) {
		super.removeTunableHandlerFactory(f,p);
	}
}
