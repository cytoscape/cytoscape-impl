package org.cytoscape.cpath2.internal.driver;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2007 - 2013
 *   Memorial Sloan-Kettering Cancer Center
 *   The Cytoscape Consortium
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

// imports
import org.cytoscape.cpath2.internal.http.HTTPEvent;
import org.cytoscape.cpath2.internal.http.HTTPServer;
import org.cytoscape.cpath2.internal.http.HTTPServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver class for cPath Plugin components.
 *
 * @author Benjamin Gross
 */
public class Driver implements HTTPServerListener {
		Logger logger = LoggerFactory.getLogger(HTTPServerListener.class);

    /**
     * Our implementation of HTTPServerListener.
     *
     * @param event HTTPEvent
     */
    public void httpEvent(HTTPEvent event) {

        logger.debug("request received: " + event.getRequest());
    }

    public static void main(String[] args) {

        // create instance of driver
        Driver driver = new Driver();

        String debugProperty = System.getProperty("DEBUG");
        Boolean debug = (debugProperty != null && debugProperty.length() > 0) &&
                new Boolean(debugProperty.toLowerCase());

        // create server
        new HTTPServer(HTTPServer.DEFAULT_PORT, driver, debug).start();
    }
}
