// $Id: HTTPWriter.java,v 1.4 2007/04/20 15:49:36 grossb Exp $
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

// imports

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class writes to the http client.
 *
 * @author Benjamin Gross.
 */
public class HTTPWriter {

    // some string constants
    private static final String CRNL = "\r\n";
    private static final String CONTENT_TYPE = "Content-Type: ";
    private static final String HTTP_RESPONSE = "HTTP/1.1 200 OK";
    private static final String CONTENT_LENGTH = "Content-Length: ";
    private static final String CONTENT_TYPE_TEXT_HTML = "text/html";

    /**
     * Called to write client respones
     *
     * @param sock  Socket
     * @param event HTTPEvent
     */
    public static void processResponse(Socket sock, HTTPEvent event) throws IOException {

        // setup print writer
        PrintWriter printWriter = new PrintWriter(sock.getOutputStream());

        String response = event.getResponse();

        // write out the contents
        printWriter.print(HTTP_RESPONSE + CRNL);
        printWriter.print(CONTENT_TYPE + CONTENT_TYPE_TEXT_HTML + CRNL);
        printWriter.print(CONTENT_LENGTH + response.length() + CRNL);
        printWriter.print(CRNL);
        printWriter.print(response);
        printWriter.flush();
	}
}