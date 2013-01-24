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