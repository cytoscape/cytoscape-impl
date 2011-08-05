package org.cytoscape.command.internal.tunables;


import java.util.Map;

import org.cytoscape.work.spring.SpringTunableInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandTunableInterceptorImpl extends SpringTunableInterceptor<StringTunableHandler> {
	private static final Logger logger = LoggerFactory.getLogger(CommandTunableInterceptorImpl.class);
	private String args;

    public boolean execUI(Object... objs) {
		return validateAndWriteBackTunables(objs);
	}

	public void setArgString(String args) {
		this.args = args;
	}
   
    public boolean validateAndWriteBackTunables(Object... objs) {
		try {

			// The objects here are task objects.  Generally there is only one. 
			for ( Object o : objs ) {
				
				// Get the handlers for the tunables in the Task.  The
				// key is the name of the tunable and the value is the handler
				// for that tunable.
				Map<String,StringTunableHandler> handlers = getHandlers(o);
				for ( StringTunableHandler h : handlers.values() ) {
					
					// Give the handler the arg string and let it do its thing, 
					// which will hopefully be: set the tunable value based on
					// information parsed from the arg string.
					h.processArgString(args);
				}
			}

		} catch (Exception e) {
			logger.warn("Exception processing tunables", e);
		}
		return true;
	}
}
