package org.cytoscape.app.internal.net.server;

/**
 * Intercepts a request before it is handled by a {@link CyHttpResponder}.
 * This is used typically to respond to the {@code OPTIONS} method and
 * to deny requests without sufficient credentials.
 */
public interface CyHttpBeforeResponse
{
    /**
     * @return null to allow the server to normally use its {@link CyHttpResponder}s,
     * or a {@link CyHttpResponse} to break the response chain and intercept the request and respond directly.
     */
    public CyHttpResponse intercept(CyHttpRequest request);
}
