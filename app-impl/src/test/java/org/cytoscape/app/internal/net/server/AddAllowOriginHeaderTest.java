package org.cytoscape.app.internal.net.server;

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

public class AddAllowOriginHeaderTest {
	@Test
	public void testAddAccessControlAllowOriginHeader() throws Exception
    {
        final CyHttpd httpd = (new CyHttpdFactoryImpl()).createHttpd(new LocalhostServerSocketFactory(2611));
        final CyHttpResponseFactory responseFactory = new CyHttpResponseFactoryImpl();
        httpd.addResponder(new CyHttpResponder()
        {
            public Pattern getURIPattern()
            {
                return Pattern.compile("^/test$");
            }

            public CyHttpResponse respond(CyHttpRequest request, Matcher matchedURI)
            {
                return responseFactory.createHttpResponse(HttpStatus.SC_OK, "test response ok", "text/html");
            }
        });
        httpd.addAfterResponse(new AddAllowOriginHeader());
        httpd.start();

        HttpURLConnection connection = null;
        final String url = "http://localhost:2611/test";

        connection = connectToURL(url, "GET");
        assertTrue(connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        assertEquals(connection.getHeaderField("Access-Control-Allow-Origin"), "*");
        assertEquals(readConnection(connection), "test response ok");

        httpd.stop();
	}

    private static HttpURLConnection connectToURL(String urlString, String method) throws Exception
    {
        final URL url = new URL(urlString);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        connection.setConnectTimeout(1000);
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
