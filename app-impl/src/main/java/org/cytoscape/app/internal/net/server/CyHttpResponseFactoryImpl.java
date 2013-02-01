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
import org.apache.http.HttpStatus;

import java.util.Map;
import java.util.HashMap;

public class CyHttpResponseFactoryImpl implements CyHttpResponseFactory
{
    public CyHttpResponse createHttpResponse(final int statusCode)
    {
        return createHttpResponse(statusCode, null, null);
    }

    public CyHttpResponse createHttpResponse(final String content, final String contentType)
    {
        return createHttpResponse(HttpStatus.SC_OK, content, contentType);
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
