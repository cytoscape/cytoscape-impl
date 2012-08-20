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

public class ScreenOriginsBeforeResponseTest {
	@Test
	public void testScreenOrigins() throws Exception
    {
        final CyHttpd httpd = (new CyHttpdFactoryImpl()).createHttpd(new LocalhostServerSocketFactory(2609));
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
        httpd.addBeforeResponse(new ScreenOriginsBeforeResponse("http://x", "http://y"));
        httpd.start();

        HttpURLConnection connection = null;
        final String url = "http://localhost:2609/test";

        connection = connectToURL(url , "GET", null);
        assertTrue(connection.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN);

        connection = connectToURL(url, "GET", "http://x");
        assertTrue(connection.getResponseCode() == HttpURLConnection.HTTP_OK);

        connection = connectToURL(url, "GET", "http://y");
        assertTrue(connection.getResponseCode() == HttpURLConnection.HTTP_OK);

        connection = connectToURL(url, "GET", "http://z");
        assertTrue(connection.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN);

        httpd.stop();
	}

    private static HttpURLConnection connectToURL(String urlString, String method, String origin) throws Exception
    {
        final URL url = new URL(urlString);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        if (origin != null)
            connection.setRequestProperty("Origin", origin);
        connection.setConnectTimeout(1000);
        connection.connect();
        return connection;
    }
}
