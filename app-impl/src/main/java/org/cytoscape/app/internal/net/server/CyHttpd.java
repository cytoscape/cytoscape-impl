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

import java.util.Collection;

/**
 * The http server.
 * 
 * <h4>The Response Chain</h4>
 * <p>When the server receives a request,
 * it follows the <i>response chain</i>:</p>
 * <ol>
 *  <li>
 *   It goes through its list of {@link CyHttpBeforeResponse}s.
 *   If any of them return a {@link CyHttpResponse}, the server 
 *   immediately responds to the client with the
 *   {@code CyHttpBeforeResponse}'s {@code CyHttpResponse}.
 *   When this happens, no {@code CyHttpResponder}s are invoked.
 *  </li>
 *  <li>
 *   If all of the {@code CyHttpBeforeResponse}s return null,
 *   the server goes through each of its {@link CyHttpResponder}s.
 *   The first {@code CyHttpResponder} whose regular expression
 *   matches the request's URI gets invoked. If none of the
 *   {@code CyHttpResponder}s match the URI, a 404 error is returned
 *   to the client.
 *  </li>
 *  <li>
 *   After a {@code CyHttpResponder} returns with a response,
 *   all of the {@link CyHttpAfterResponse}s are invoked to further
 *   process the response.
 *  </li>
 *  <li>
 *   The {@code CyHttpResponse} is sent to the client.
 *  </li>
 * </ol>
 *
 * <h4>Implications of the Response Chain</h4>
 * <ul>
 *  <li>
 *   The {@code OPTIONS} method can be in a
 *   {@code CyHttpBeforeResponse}.
 *  </li>
 *  <li>
 *   Clients can be denied if its {@code Origin} is not allowed
 *   through a {@code CyHttpBeforeResponse}.
 *  </li>
 *  <li>
 *   Headers can be appended to responses in an 
 *   {@code CyHttpAfterResponse}.
 *  </li>
 * </ul>
 */
public interface CyHttpd
{
    /**
     * Starts the server to service requests from the client.
     */
    void start();

    /**
     * Stops the server from servicing new requests.
     */
    void stop();

    boolean isRunning();

    void addResponder(CyHttpResponder responder);
    void removeResponder(CyHttpResponder responder);
    Collection<CyHttpResponder> getResponders();

    void addBeforeResponse(CyHttpBeforeResponse beforeResponse);
    void removeBeforeResponse(CyHttpBeforeResponse beforeResponse);
    Collection<CyHttpBeforeResponse> getBeforeResponses();

    void addAfterResponse(CyHttpAfterResponse afterResponse);
    void removeAfterResponse(CyHttpAfterResponse afterResponse);
    Collection<CyHttpAfterResponse> getAfterResponses();
}
