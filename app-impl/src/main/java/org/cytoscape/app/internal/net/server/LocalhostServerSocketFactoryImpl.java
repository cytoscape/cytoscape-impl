package org.cytoscape.app.internal.net.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.InetAddress;

/**
 * An implementation of {@link ServerSocketFactory} that only accepts connections
 * from localhost.
 */
public class LocalhostServerSocketFactoryImpl implements ServerSocketFactory
{
    final int port;

    public LocalhostServerSocketFactoryImpl(int port)
    {
        if (port <= 0)
            throw new IllegalArgumentException("port <= 0");
        this.port = port;
    }

    /**
     * Create a server socket with the given port and default backlog.
     */
    public ServerSocket createServerSocket() throws IOException
    {
        return new ServerSocket(port, 0, InetAddress.getByName(null));
    }
}
