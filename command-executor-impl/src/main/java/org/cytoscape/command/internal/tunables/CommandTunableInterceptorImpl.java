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

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.command.StringTunableHandler;
import org.cytoscape.command.StringTunableHandlerFactory;
import org.cytoscape.work.AbstractTunableInterceptor;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.TunableValidator.ValidationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandTunableInterceptorImpl extends AbstractTunableInterceptor<StringTunableHandler> {

	private static final Logger logger = LoggerFactory.getLogger(CommandTunableInterceptorImpl.class);
	private String args = null;
	private Map<String, Object> mapArgs = null;

	public void setConfigurationContext(String args) {
		this.args = args;
		if (args == null) {
			titleProviderMap.clear();
			handlerMap.clear();
		}
	}

	public void setConfigurationContext(Map<String, Object> args) {
		this.mapArgs = args;
		if (args == null) {
			titleProviderMap.clear();
			handlerMap.clear();
		}
	}

	public boolean validateAndWriteBackTunables(Object o) {
		try {
			// Get the handlers for the tunables in the Task.  The
			// key is the name of the tunable and the value is the handler
			// for that tunable.
			for ( StringTunableHandler h : getHandlers(o) ) {

				if (args != null) {
					// Give the handler the arg string and let it do its thing,
					// which will hopefully be: set the tunable value based on
					// information parsed from the arg string.
					h.processArgString(args);
				} else {
					for ( String string: mapArgs.keySet() ) {
						if (h.getName().equals(string)) {
							Object v = mapArgs.get(string);
							try {
								if (v instanceof String)
									v = h.processArg((String)v);
								h.setValue(v);
							} catch (Exception e) {
								logger.warn("Couldn't parse value from: " + v + " for setting: "+h.getName(), e);
								throw new IllegalArgumentException("Couldn't parse value from: " + v + " for setting: "+h.getName());
								// return false;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.warn("Exception processing tunables", e);
			throw new RuntimeException("Error processing arguments: "+e.getMessage());
		}
		return true;
	}
	
	/**
	 * Check if the conditions set in validate method from <code>TunableValidator</code> are met
	 *
	 * If an exception is thrown, or something's wrong, it will be displayed to the user
	 *
	 * @return success(true) or failure(false) for the validation
	 */
	public boolean validateTunableInput(final Object objectWithTunables,TaskMonitor tm) {
		if (!(objectWithTunables instanceof TunableValidator))
			return true;

		final Appendable errMsg = new StringBuilder();
		try {
			final ValidationState validationState = ((TunableValidator)objectWithTunables).getValidationState(errMsg);
			if (validationState == ValidationState.INVALID) {
				tm.showMessage(TaskMonitor.Level.ERROR, "[ERROR] " + errMsg.toString());
				return false;
			} else if (validationState == ValidationState.REQUEST_CONFIRMATION) {
				if (JOptionPane.showConfirmDialog(new JFrame(), errMsg.toString(),
								  "Confirmation",
								  JOptionPane.YES_NO_OPTION)
				    == JOptionPane.NO_OPTION)
				{
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public void addTunableHandlerFactory(StringTunableHandlerFactory<StringTunableHandler> f, Map<String,String> p) {
		super.addTunableHandlerFactory(f,p);
	}
	public void removeTunableHandlerFactory(StringTunableHandlerFactory<StringTunableHandler> f, Map<String,String> p) {
		super.removeTunableHandlerFactory(f,p);
	}
}
