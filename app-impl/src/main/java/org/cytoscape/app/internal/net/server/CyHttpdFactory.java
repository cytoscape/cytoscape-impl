package org.cytoscape.app.internal.net.server;

public interface CyHttpdFactory
{
    CyHttpd createHttpd(ServerSocketFactory serverSocketFactory);
}
