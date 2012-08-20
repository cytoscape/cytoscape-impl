package org.cytoscape.app.internal.net.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpRequest;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.util.EntityUtils;
import org.apache.http.Header;
import java.net.URLDecoder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class CyHttpRequestImpl implements CyHttpRequest
{
    final String uri;
    final String method;
    final Map<String,String> headers;
    final String content;
    final String contentType;

    public CyHttpRequestImpl(final HttpRequest request)
    {
        final String uriEncoded = request.getRequestLine().getUri();
        try
        {
            uri = URLDecoder.decode(uriEncoded, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalArgumentException("Unable to parse uri: " + uriEncoded, e);
        }

        method = request.getRequestLine().getMethod().toUpperCase();

        headers = new HashMap<String,String>();
        for (final Header header : request.getAllHeaders())
        {
            final String name = header.getName();
            String value = header.getValue();
            if (headers.containsKey(name))
                value = headers.get(name) + '\n' + value;
            headers.put(name, value);
        }

        if (request instanceof HttpEntityEnclosingRequest)
        {
            final HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            contentType = entity.getContentType().getValue();
            try
            {
                content = EntityUtils.toString(entity);
            }
            catch (IOException e)
            {
                throw new IllegalArgumentException("Unable to read entity as string", e);
            }
        }
        else
        {
            contentType = null;
            content = null;
        }
    }

    public String getURI()
    {
        return uri;
    }

    public String getMethod()
    {
        return method;
    }
    
    public Map<String,String> getHeaders()
    {
        return Collections.unmodifiableMap(headers);
    }

    public String getContent()
    {
        return content;
    }

    public String getContentType()
    {
        return contentType;
    }
}
