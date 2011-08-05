// $Id: HTTPReader.java,v 1.5 2007/04/25 15:28:34 grossb Exp $
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This class reads incoming http requests.
 *
 * @author Benjamin Gross.
 */
public class HTTPReader {

    // some string constants
    private static final String SPACE = " ";
    private static final String HTTP_PREFIX = "http://";

    /**
     * Called to handle incoming request.
     *
     * @param sock Socket
     */
    public static String processRequest(Socket sock) throws IOException {

        // create buffered reader from socket
        BufferedReader in =
                new BufferedReader(new InputStreamReader(sock.getInputStream()));

        // init some vars
        String s;
        ArrayList<String> request = new ArrayList<String>();

        // grab the entire request
        while ((s = in.readLine()) != null && s.length() > 0) {
            request.add(s);
        }

        // outta here - at this point we only care about the request line
        return parseRequestLine(request.get(0));
    }

    /**
     * Parse request line from client browser.
     *
     * @param requestLine String
     */
    private static String parseRequestLine(String requestLine) {

        // per rfc2616, request line in following format:
        // request-line: Method SP Request-URI SP HTTP-Version CRLF

        // replace w/regex if we need to parse arguments
        String uri =
                requestLine.substring(requestLine.indexOf(SPACE) + 2, requestLine.lastIndexOf(SPACE));
        return HTTP_PREFIX + uri;
    }
}
