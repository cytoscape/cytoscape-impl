// $Id: Driver.java,v 1.3 2007/04/26 21:56:42 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2007 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami, Benjamin Gross
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander, Benjamin Gross
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.cytoscape.cpath2.internal.driver;

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
