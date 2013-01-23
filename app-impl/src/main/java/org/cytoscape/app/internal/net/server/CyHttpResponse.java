package org.cytoscape.app.internal.net.server;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

/**
 * The http response the server sends back to the client.
 * This interface assumes that all responses are in text.
 * This makes it easier to work with responses. However,
 * sending binary data back to the client is not possible
 * unless the data is encoded into a string that the client
 * understands.
 */
public interface CyHttpResponse 
{
    /**
     * The http protocol status code of the response.
     * Use values from {@link org.apache.http.HttpStatus} to fill this in.
     */
    public int getStatusCode();

    /**
     * The response's content.
     */
    public String getContent();

    /**
     * Get the MIME type of the content.
     */
    public String getContentType();

    /**
     * Get the headers of the response.
     * This should return a mutable map so that {@link CyHttpAfterResponse}
     * can add or modify headers before a response is sent back to the client.
     */
    public Map<String,String> getHeaders();
}
