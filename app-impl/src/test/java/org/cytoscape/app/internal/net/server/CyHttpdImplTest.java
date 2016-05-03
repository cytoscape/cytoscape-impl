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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.http.HttpStatus;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CyHttpdImplTest {

	@Test
	public void testHttpd() throws Exception
    {
        final CyHttpd httpd = (new CyHttpdFactoryImpl()).createHttpd(new LocalhostServerSocketFactory(2608));
        final CyHttpResponseFactory responseFactory = new CyHttpResponseFactoryImpl();
        httpd.addResponder(new CyHttpResponder()
        {
            public Pattern getURIPattern()
            {
                return Pattern.compile("^/testA$");
            }

            public CyHttpResponse respond(CyHttpRequest request, Matcher matchedURI)
            {
                return responseFactory.createHttpResponse(HttpStatus.SC_OK, "testA response ok", "text/html");
            }
        });

        httpd.addResponder(new CyHttpResponder()
        {
            public Pattern getURIPattern()
            {
                return Pattern.compile("^/testB$");
            }

            public CyHttpResponse respond(CyHttpRequest request, Matcher matchedURI)
            {
                return responseFactory.createHttpResponse(HttpStatus.SC_OK, "testB response ok", "text/html");
            }
        });

        httpd.addBeforeResponse(request -> {
            if (request.getMethod().equals("OPTIONS"))
                return responseFactory.createHttpResponse(HttpStatus.SC_OK, "options intercepted", "text/html");
            else
                return null;
        });

        httpd.addAfterResponse((request, response) -> {
            response.getHeaders().put("SomeRandomHeader", "WowInterceptWorks");
            return response;
        });


        assertFalse(httpd.isRunning());
        httpd.start();
        assertTrue(httpd.isRunning());

        final String url = "http://localhost:2608/";

        // test if normal response works
        HttpURLConnection connection = connectToURL(url + "testA", "GET");
        assertTrue(connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        assertEquals(readConnection(connection), "testA response ok");

        connection = connectToURL(url + "testB", "GET");
        assertTrue(connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        assertEquals(readConnection(connection), "testB response ok");

        // test if 404 response works
        connection = connectToURL(url + "testX", "GET");
        assertTrue(connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND);

        // test if before intercept works
        connection = connectToURL(url + "testA", "OPTIONS");
        assertTrue(connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        assertEquals(readConnection(connection), "options intercepted");

        // test if after intercept works
        connection = connectToURL(url + "testA", "GET");
        assertTrue(connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        assertEquals(connection.getHeaderField("SomeRandomHeader"), "WowInterceptWorks");

        httpd.stop();
        assertFalse(httpd.isRunning());
	}

    private static HttpURLConnection connectToURL(String urlString, String method) throws Exception
    {
        final URL url = new URL(urlString);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        connection.connect();
        return connection;
    }

    private static String readConnection(HttpURLConnection connection) throws Exception
    {
        final StringBuffer buffer = new StringBuffer();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while (true)
        {
            final String line = reader.readLine();
            if (line == null)
                break;
            buffer.append(line);
        }

        return buffer.toString();
    }
}
