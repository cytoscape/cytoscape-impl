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

/**
 * Creates response objects. Implementations of {@link CyHttpResponder},
 * {@link CyHttpBeforeResponse}, and {@link CyHttpAfterResponse}
 * would use this to create response objects
 * in its return values.
 */
public interface CyHttpResponseFactory
{
    /**
     * Create a response with no content.
     * @param statusCode the http response status code; use a
     * static field in {@link java.net.HttpURLConnection} to fill this in.
     */
    CyHttpResponse createHttpResponse(int statusCode);

    /**
     * Create a response with content.
     * @param statusCode the http response status code; use a
     * static field in {@link java.net.HttpURLConnection} to fill this in.
     * @param content the text content to send back to the client
     * @param contentType the MIME type of {@code content}; examples of valid
     * MIME types: {@code text/html}, {@code application/json}, {@code application/xml}.
     */
    CyHttpResponse createHttpResponse(int statusCode, String content, String contentType);

    /**
     * Create a response with content and 200 OK status code.
     * @param content the text content to send back to the client
     * @param contentType the MIME type of {@code content}; examples of valid
     * MIME types: {@code text/html}, {@code application/json}, {@code application/xml}.
     */
    CyHttpResponse createHttpResponse(String content, String contentType);
}
