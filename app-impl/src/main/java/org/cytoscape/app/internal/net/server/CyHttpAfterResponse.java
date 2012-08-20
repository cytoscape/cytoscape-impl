package org.cytoscape.app.internal.net.server;

/**
 * Intercepts a {@link CyHttpResponse} before it is sent out to the client.
 * This is typically used to add headers to the response.
 */
public interface CyHttpAfterResponse
{
    public CyHttpResponse intercept(CyHttpRequest request, CyHttpResponse pendingResponse);
}
