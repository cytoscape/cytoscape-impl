package org.cytoscape.app.internal.net.server;

public interface CyHttpResponseFactory
{
    /**
     * Create a response with no content.
     */
    CyHttpResponse createHttpResponse(int statusCode);

    /**
     * Create a response with content.
     */
    CyHttpResponse createHttpResponse(int statusCode, String content, String contentType);
}
