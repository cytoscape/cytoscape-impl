package org.cytoscape.app.internal.net.server;

import java.util.Map;

/**
 * The http response the server sends back to the client.
 * This interface assumes that all responses are in text.
 * This makes it easier to work with responses. However,
 * sending binary data back to the client is not possible
 * unless the data is encoded into a string that the client
 * understands.
 */
public interface CyHttpResponse 
{
    /**
     * The http protocol status code of the response.
     * Use values from {@link org.apache.http.HttpStatus} to fill this in.
     */
    public int getStatusCode();

    /**
     * The response's content.
     */
    public String getContent();

    /**
     * Get the MIME type of the content.
     */
    public String getContentType();

    /**
     * Get the headers of the response.
     * This should return a mutable map so that {@link CyHttpAfterResponse}
     * can add or modify headers before a response is sent back to the server.
     */
    public Map<String,String> getHeaders();
}
