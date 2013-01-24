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

import org.apache.http.HttpStatus;

/**
 * Reject requests if the client does not send an {@code Origin} header
 * and if its {@code Origin} is not allowed.
 */
public class ScreenOriginsBeforeResponse implements CyHttpBeforeResponse
{
    static final CyHttpResponseFactory responseFactory = new CyHttpResponseFactoryImpl();
    final String[] allowedOrigins;

    /**
     * @param allowedOrigins A list of strings containing the full URLs of allowed origins.
     */
    public ScreenOriginsBeforeResponse(final String... allowedOrigins)
    {
        this.allowedOrigins = new String[allowedOrigins.length];
        
        // Trim any trailing slashes
        for (int i = 0; i < allowedOrigins.length; i++) {
        	String origin = allowedOrigins[i];
        	if (origin.endsWith("/")) {
        		origin = origin.substring(0, origin.length() - 1);
        	}
        	this.allowedOrigins[i] = origin;
        }
    }

    public CyHttpResponse intercept(CyHttpRequest request)
    {
        final String origin = request.getHeaders().get("Origin");
        if (origin == null)
            return responseFactory.createHttpResponse(HttpStatus.SC_FORBIDDEN, "<html><body><h1>403 Forbidden</h1>No <tt>Origin</tt> header specified.</body></html>", "text/html");

        for (final String allowedOrigin : allowedOrigins)
        {
            if (allowedOrigin.equals(origin))
                return null;
        }

        return responseFactory.createHttpResponse(HttpStatus.SC_FORBIDDEN, String.format("<html><body><h1>403 Forbidden</h1><tt>Origin: %s</tt> not allowed.</body></html>", origin), "text/html");
    }
}
