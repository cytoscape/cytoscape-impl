package org.cytoscape.app.internal.net.server;

/**
 * The http server.
 * 
 * <h4>The Response Chain</h4>
 * <p>When the server receives a request,
 * it follows the <i>response chain</i>:</p>
 * <ol>
 *  <li>
 *   It goes through its list of {@link CyHttpBeforeResponse}s.
 *   If any of them return a {@link CyHttpResponse}, the server 
 *   immediately responds to the client with the
 *   {@code CyHttpBeforeResponse}'s {@code CyHttpResponse}.
 *   In this case, no {@code CyHttpResponder}s are invoked.
 *  </li>
 *  <li>
 *   If all of the {@code CyHttpBeforeResponse}s return null,
 *   the server goes through each of its {@link CyHttpResponder}s.
 *   The first {@code CyHttpResponder} whose regular expression
 *   matches the request's URI gets invoked. If none of the
 *   {@code CyHttpResponder}s match the URI, a 404 error is returned
 *   to the client.
 *  </li>
 *  <li>
 *   After a {@code CyHttpResponder} returns with a response,
 *   all of the {@link CyHttpAfterResponse}s are invoked to further
 *   process the response.
 *  </li>
 *  <li>
 *   The {@code CyHttpResponder} is sent to the client.
 *  </li>
 * </ol>
 *
 * <h4>Implications of the Response Chain</h4>
 * <ul>
 *  <li>
 *   The {@code OPTIONS} method can be in a
 *   {@code CyHttpBeforeResponse}.
 *  </li>
 *  <li>
 *   Clients can be denied if its {@code Origin} is not allowed
 *   through a {@code CyHttpBeforeResponse}.
 *  </li>
 *  <li>
 *   Headers can be appended to responses in an 
 *   {@code CyHttpAfterResponse}.
 *  </li>
 * </ul>
 */
public interface CyHttpd
{
    /**
     * Starts the server to service requests from the client.
     */
    void start();

    /**
     * Stops the server from servicing new requests.
     */
    void stop();

    boolean isRunning();

    void addResponder(CyHttpResponder responder);
    void removeResponder(CyHttpResponder responder);
    Iterable<CyHttpResponder> getResponders();

    void addBeforeResponse(CyHttpBeforeResponse beforeResponse);
    void removeBeforeResponse(CyHttpBeforeResponse beforeResponse);
    Iterable<CyHttpBeforeResponse> getBeforeResponses();

    void addAfterResponse(CyHttpAfterResponse afterResponse);
    void removeAfterResponse(CyHttpAfterResponse afterResponse);
    Iterable<CyHttpAfterResponse> getAfterResponses();
}
