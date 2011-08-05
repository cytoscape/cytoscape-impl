// $Id: HTTPEvent.java,v 1.2 2007/04/20 15:50:40 grossb Exp $
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
package org.cytoscape.cpath2.internal.http;

// imports

import java.util.EventObject;

/**
 * A proxy event object.
 */
public class HTTPEvent extends EventObject {

    /**
     * the request
     */
    private String request;

    /**
     * the response
     */
    private String response;

    /**
     * Constructor.
     *
     * @param source   Object
     * @param request  String
     * @param response String
     */
    public HTTPEvent(Object source, String request, String response) {
        super(source);

        // init members
        this.request = request;
        this.response = response;
    }

    /**
     * Method to get the request.
     *
     * @return String
     */
    public String getRequest() {
        return request;
    }

    /**
     * Method to get the response.
     *
     * @return String
     */
    public String getResponse() {
        return response;
	}
}
