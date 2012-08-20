package org.cytoscape.app.internal.net.server;

import java.util.Map;
import java.util.HashMap;

public class CyHttpResponseFactoryImpl implements CyHttpResponseFactory
{
    public CyHttpResponse createHttpResponse(final int statusCode)
    {
        return createHttpResponse(statusCode, null, null);
    }

    public CyHttpResponse createHttpResponse(final int statusCode, final String content, final String contentType)
    {
        return new CyHttpResponse()
        {
            final Map<String,String> headers = new HashMap<String,String>();

            public int getStatusCode()
            {
                return statusCode;
            }

            public String getContent()
            {
                return content;
            }

            public String getContentType()
            {
                return contentType;
            }

            public Map<String,String> getHeaders()
            {
                return headers;
            }
        };
    }
}
