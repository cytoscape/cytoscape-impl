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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Provides a response for a given request.
 * When {@link CyHttpd} receives a request,
 * it goes through each of its {@code CyHttpResponder}s.
 * When a {@code CyHttpResponder}'s URI pattern matches the
 * request's URI, that {@code CyHttpResponder} will be invoked
 * to handle the request.
 */
public interface CyHttpResponder
{
    /**
     * Get the regular expression that matches the responder to the request's URI.
     * <p>
     * The request URI does not include the protocol, server name, or port. If the client issues
     * a request to the URL {@code http://localhost:2607/a/b/c}, the server receives the
     * request URI to be {@code /a/b/c}.
     * </p>
     * <p>
     * Groups can be included in the regular expression, as the {@link Matcher}
     * object is passed to the {@code respond} method.
     * </p>
     */
    public Pattern getURIPattern();

    /**
     * Invoked by {@link CyHttpd} to respond to a request.
     * @param matchedURI The {@link Matcher} object that matches against the request's URI;
     * useful for retrieving groups in the regular expression.
     */
    public CyHttpResponse respond(CyHttpRequest request, Matcher matchedURI);
}
