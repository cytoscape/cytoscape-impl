package org.cytoscape.biopax.internal.action;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
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

import java.net.URL;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

//import org.cytoscape.biopax.internal.util.CytoscapeWrapper;
import org.cytoscape.util.swing.OpenBrowser;


/**
 * Launches the User's External Web Browser.
 *
 * @author Ethan Cerami.
 */
public class LaunchExternalBrowser implements HyperlinkListener {

	private OpenBrowser browser;

	public LaunchExternalBrowser(OpenBrowser browser) {
		this.browser = browser;
	}
	
	/**
	 * User has clicked on a HyperLink.
	 *
	 * @param evt HyperLink Event Object.
	 */
	public void hyperlinkUpdate(HyperlinkEvent evt) {
		URL url = evt.getURL();

		if (url != null) {
			if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
				//CytoscapeWrapper.setStatusBarMsg(url.toString());
			} else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
				//CytoscapeWrapper.clearStatusBar();
			} else if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				browser.openURL(url.toString());
			}
		}
	}
}
