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

/**
 * Used by the http server to create {@link ServerSocket}s.
 * By implementing this interface, you can control the creation
 * of a {@code ServerSocket} that the http server will use to serve clients.
 * This means that you can control the
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
