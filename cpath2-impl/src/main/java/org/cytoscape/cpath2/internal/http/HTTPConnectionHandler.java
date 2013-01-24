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
import java.net.Socket;

/**
 * Class which handles connections.
 *
 * @author Benjamin Gross.
 */
public class HTTPConnectionHandler extends Thread {

    /**
     * web service url string constant
     */
    public static final String WEB_SERVICE_URL = "webservice.do";

    /**
     * ref to socket
     */
    private Socket sock;

    /**
     * ref to server listener
     */
    private HTTPServerListener listener;

    /**
     * debug flag
     */
    private boolean debug;

    /**
     * Constructor.
     *
     * @param sock     Socket
     * @param listener HTTServerListener
     * @param debug    boolean
     */
    public HTTPConnectionHandler(Socket sock, HTTPServerListener listener, boolean debug) {

        // init our members
        this.sock = sock;
        this.listener = listener;
        this.debug = debug;
    }

    /**
     * Our implementation of run
     */
    public void run() {

        try {

            // get the event object to pass on
            String uri = HTTPReader.processRequest(sock);
            HTTPEvent request = new HTTPEvent(this, uri, null);
            if (debug)
                System.out.println("HTTPConnectionHandler, request received: " + request.getRequest());

            // only interested in cpath web service urls
            if (request.getRequest().indexOf(WEB_SERVICE_URL) != -1) {

                // send response back to client - before we process here
                // note: callBack() is a javascript routine in webstart.js
                // which will be execute immediately after web browser receives this request
                HTTPEvent response = new HTTPEvent(this, null, "callBack();");
                if (debug)
                    System.out.println("HTTPConnectionHandler, sending response to web browser: " + response.getResponse());
                HTTPWriter.processResponse(sock, response);
                if (debug) System.out.println("HTTPConnectionHandler, closing socket...");
                sock.close();

                // pass on the event to our listener
                if (debug)
                    System.out.println("HTTPConnectionHandler, sending request to listener.");
                listener.httpEvent(request);
                if (debug) System.out.println("HTTPConnectionHandler, listener complete");
            }
            // nothing we are interested in, just close socket
            else {
                // outta here
                if (debug)
                    System.out.println("HTTPConnectionHandler, no work to be done, closing socket...");
                sock.close();
            }

        }
        catch (IOException e) {
            e.printStackTrace();
		}
	}
}
