/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.tableimport.internal.util;

//import cytoscape.Cytoscape;
//import cytoscape.CytoscapeInit;

import java.beans.*;

import java.net.InetSocketAddress;
import java.net.Proxy;


/**
 *
 */
public class ProxyHandler implements PropertyChangeListener {
    public static final String PROXY_HOST_PROPERTY_NAME = "proxy.server";
    public static final String PROXY_TYPE_PROPERTY_NAME = "proxy.server.type";
    public static final String PROXY_PORT_PROPERTY_NAME = "proxy.server.port";
    private static Proxy       proxyServer = null;

    static {
        new ProxyHandler();
    }

    private ProxyHandler() {
       // Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(this);
    }

    /**
     * Return the Proxy representing the proxy server to use
     * when reading and writing URLs. If no proxy server is being
     * used, return null.
     */
    // TODO: Change this to *always* return a Proxy (no null value).
    //       For a null proxy, return Proxy.NO_PROXY instead.
    public static Proxy getProxyServer() {
        if (proxyServer == null) {
            loadProxyServer();
        }
        return proxyServer;
    }

    // TODO: Change to always setup proxyServer to a non-null value
    // using Proxy.NO_PROXY for direct connections. Also, change to
    // not produce a new Proxy if the proxy is the same as previous
    // Proxy.
    private static void loadProxyServer() {
        /*
    	String proxyName = CytoscapeInit.getProperties()
                                        .getProperty(PROXY_HOST_PROPERTY_NAME);

        if ((proxyName == null) || proxyName.equals("")) {
            proxyServer = null;

            return;
        }

        String proxyT = CytoscapeInit.getProperties()
                                     .getProperty(PROXY_TYPE_PROPERTY_NAME);

        if ((proxyT == null) || proxyT.equals("")) {
            proxyServer = null;

            return;
        }

        Proxy.Type proxyType = Proxy.Type.valueOf(proxyT);
        String     proxyP = CytoscapeInit.getProperties()
                                         .getProperty(PROXY_PORT_PROPERTY_NAME);

        if ((proxyP == null) || proxyP.equals("")) {
            proxyServer = null;

            return;
        }

        int proxyPort = Integer.parseInt(proxyP);

        proxyServer = new Proxy(proxyType,
                                new InetSocketAddress(proxyName, proxyPort));
    
        */
    
    }

    /**
     *  DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void propertyChange(PropertyChangeEvent e) {
    	/*
        if (Cytoscape.PREFERENCES_UPDATED.equals(e.getPropertyName())) {
            Proxy savedProxy = proxyServer;
            loadProxyServer();

	    // Only fire event if the proxy changed:
            if (((proxyServer == null) && (savedProxy != null)) ||
                ((proxyServer != null) && (!proxyServer.equals(savedProxy)))) {
                Cytoscape.firePropertyChange(Cytoscape.PROXY_MODIFIED,
                                             savedProxy, proxyServer);
            }
        }
        */
    }
}
