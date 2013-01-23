package org.cytoscape.cpath2.internal.http;

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
