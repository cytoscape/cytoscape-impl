// $Id$
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
