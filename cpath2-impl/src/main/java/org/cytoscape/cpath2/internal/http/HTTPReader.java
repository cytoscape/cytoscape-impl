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
