package org.cytoscape.tableimport.internal.util;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
