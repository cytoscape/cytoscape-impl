// $Id: HTTPServer.java,v 1.9 2007/04/26 21:56:24 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2007 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami, Benjamin Gross
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander, Benjamin Gross
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.cytoscape.cpath2.internal.http;

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
