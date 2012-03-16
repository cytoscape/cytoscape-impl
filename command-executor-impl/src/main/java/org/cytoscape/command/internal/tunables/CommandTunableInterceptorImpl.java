package org.cytoscape.command.internal.tunables;

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
