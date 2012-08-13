package org.cytoscape.app.internal.net.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Used by the http server to create {@link ServerSocket}s.
 * You can control the creation of a {@code ServerSocket} that the
 * http server will use to serve clients.
 * By creating {@code ServerSocket}s yourself, you can control the
 * port the http server listens to and the IP addresses the http server accepts.
 * This is not to be confused with {@link javax.net.ServerSocketFactory}, which
 * is just a list of convenience methods for creating {@code ServerSocket}s.
 */
public interface ServerSocketFactory
{
    /**
     * Create a {@code ServerSocket}.
     * @throws IOException if this method is unable to create a working server socket
     */
    public ServerSocket createServerSocket() throws IOException;
}
