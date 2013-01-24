package org.cytoscape.cpath2.internal.http;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2007 - 2013
 *   Memorial Sloan-Kettering Cancer Center
 *   The Cytoscape Consortium
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

// import

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.LoggerFactory;

/**
 * HTTPServer class provides a bare-bones
 * server to listen for requests from cPath instance.
 * web pages.
 * <p/>
 * First version will be single-thread since
 * we assume only one browser w/pc is running.
 *
 * @author Benjamin Gross.
 */
public class HTTPServer extends Thread {

    /**
     * default port
     */
    public static final int DEFAULT_PORT = 27182;

    /**
     * ref to port
     */
    private int port;

    /**
     * debug flag
     */
    private boolean debug;

    /**
     * ref to server listener
     */
    private HTTPServerListener listener;

    /**
     * Constructor.
     *
     * @param port     int
     * @param listener HTTServerListener
     * @param debug    boolean
     */
    public HTTPServer(int port, HTTPServerListener listener, boolean debug) {

        // init members
        this.port = port;
        this.listener = listener;
        this.debug = debug;
    }

    /**
     * Our implementation of run.
     */
    public void run() {

        // create new server socket
        ServerSocket ssocket;
        try {
            if (debug) System.out.println("HTTPServer, creating server socket...");
            ssocket = new ServerSocket(port);
        } catch (Exception e) {
			LoggerFactory.getLogger(HTTPServer.class).warn("HTTPServer couldn't create socket.",e);
            return;
        }

        // run indefinitely
        while (true) {

            try {

                // block until connection is made
                if (debug) System.out.println("HTTPServer, waiting for connection...");
                Socket sock = ssocket.accept();

                // connection made, create an new connection handler
                if (debug)
                    System.out.println("HTTPServer, instantiating new HTTPConnectionHandler");
                new HTTPConnectionHandler(sock, listener, debug).start();

            } catch (IOException e) {
				LoggerFactory.getLogger(HTTPServer.class).warn("HTTPServer couldn't create connection handler.",e);
                break;
			}
		}
	}
}
