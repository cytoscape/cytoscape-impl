package org.cytoscape.app.internal.net.server;

import java.util.Map;

/**
 * An http request issued by the client.
 * This interface assumes that the client is
 * sending text. This assumption greatly simplifies
 * this interface. In reality, the client could
 * be sending binary data, which is not properly
 * handled by this interface.
 */
public interface CyHttpRequest
{
    /**
     * The URI the client requested.
     * This will not include the protocol or server address.
     * For example, if the client requested {@code http://myserver:8000/my/resource},
     * the URI will be {@code /my/resource}.
     */
    String getURI();

    /**
     * The http method.
     * Typically the method is {@code GET} or {@code POST}.
     * @return the http method in all uppercase characters.
     */
    String getMethod();
    
    /**
     * The http request's headers.
     * This will join headers that appear more than once.
     * If the http request's headers are:
     * <pre>
     * {@code
     *   Header-1: a
     *   Header-2: b
     *   Header-1: c
     *   Header-3: d
     * }
     * </pre>
     * This will return the following map:
     * <pre>
     * {@code
     *  {
     *   "Header-1": "a\nc"
     *   "Header-2": "b",
     *   "Header-3": "d"
     *  }
     * }
     * </pre>
     */
    Map<String,String> getHeaders();

    /**
     * Get the content of the request.
     * For {@code GET} methods, this will typically return an empty string.
     * Parameters of the {@code GET} method are provided in the URI.
     * For {@code POST} methods, this will return the body of the request.
     */
    String getContent();

    /**
     * Get the MIME type of the content.
     */
    String getContentType();
}
