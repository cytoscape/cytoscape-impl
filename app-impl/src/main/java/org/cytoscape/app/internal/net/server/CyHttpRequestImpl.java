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

/**
 * This is used internally to translate HttpCore's {@link HttpRequest}
 * into a {@link CyHttpRequest}.
 */
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
            throw new IllegalArgumentException("Unable to parse URI: " + uriEncoded, e);
        }

        method = request.getRequestLine().getMethod().toUpperCase();

        headers = new HashMap<>();
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
