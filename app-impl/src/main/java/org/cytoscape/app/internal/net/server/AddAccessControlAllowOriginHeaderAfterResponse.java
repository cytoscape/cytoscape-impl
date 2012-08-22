package org.cytoscape.app.internal.net.server;

/**
 * Adds the {@code Access-Control-Allow-Origin} http header to responses.
 */
public class AddAccessControlAllowOriginHeaderAfterResponse implements CyHttpAfterResponse
{
    static final CyHttpResponseFactory responseFactory = new CyHttpResponseFactoryImpl();

    public CyHttpResponse intercept(CyHttpRequest request, CyHttpResponse response)
    {
        final String origin = request.getHeaders().get("Origin");
        response.getHeaders().put("Access-Control-Allow-Origin", (origin == null ? "*" : origin));
        return response;
    }
}
