package org.cytoscape.app.internal.net.server;

import org.apache.http.HttpStatus;

/**
 * Supports responses to the {@code OPTIONS} http method requests.
 * This is not a completely accurate implementation of {@code OPTIONS}.
 * If the client requests {@code OPTIONS} on a non-existant URL, this will not
 * return 404. But that's okay. If the client later requests {@code GET} on the
 * same, non-existant URL, it will get the 404.
 */
public class OriginOptionsBeforeResponse implements CyHttpBeforeResponse
{
    static final CyHttpResponseFactory responseFactory = new CyHttpResponseFactoryImpl();
    final String allowedHeadersFmt;

    public OriginOptionsBeforeResponse(final String... allowedHeaders)
    {
        final StringBuffer allowedHeadersBuffer = new StringBuffer("origin, accept");
        for (final String allowedHeader : allowedHeaders)
        {
            allowedHeadersBuffer.append(", ");
            allowedHeadersBuffer.append(allowedHeader);
        }
        this.allowedHeadersFmt = allowedHeadersBuffer.toString();
    }

    public CyHttpResponse intercept(CyHttpRequest request)
    {
        if (!"OPTIONS".equals(request.getMethod()))
            return null;

        final String origin = request.getHeaders().get("Origin");

        final CyHttpResponse response = responseFactory.createHttpResponse(HttpStatus.SC_OK);
        response.getHeaders().put("Access-Control-Allow-Origin", (origin == null ? "*" : origin));
        response.getHeaders().put("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS");
        response.getHeaders().put("Access-Control-Max-Age", "1");
        response.getHeaders().put("Access-Control-Allow-Headers", allowedHeadersFmt);
        return response;
    }
}
