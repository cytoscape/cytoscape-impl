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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.InetAddress;

/**
 * Creates {@link ServerSocket}s that only accepts connections
 * from localhost.
 */
public class LocalhostServerSocketFactory implements ServerSocketFactory
{
    final int port;

    public LocalhostServerSocketFactory(int port)
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
