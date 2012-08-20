package org.cytoscape.app.internal.net.server;

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
