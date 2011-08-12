/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.internal.actions;



import javax.swing.*;
import javax.swing.event.MenuEvent;
import java.awt.event.ActionEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.AbstractCyAction;


public class CytoPanelAction extends AbstractCyAction {
	private final static long serialVersionUID = 1202339869395571L;

	protected static String SHOW = "Show";
	protected static String HIDE = "Hide";

	protected String title;
	protected CytoPanelName position;
	private CySwingApplication desktop;

	public CytoPanelAction(final CytoPanelName position, final boolean show, final CySwingApplication desktop, final CyApplicationManager applicationManager, float menuGravity)
	{
		super(show ? HIDE + " " + position.getTitle() : SHOW + " " + position.getTitle(),
		      applicationManager);

		this.title = position.getTitle();
		this.position = position;
		setPreferredMenu("View");
		setMenuGravity(menuGravity);
		this.desktop = desktop;
	}

	/**
	 * Toggles the cytopanel state.  
	 *
	 * @param ev Triggering event - not used. 
	 */
	public void actionPerformed(ActionEvent ev) {
		CytoPanelState curState = desktop.getCytoPanel(position).getState();

		if (curState == CytoPanelState.HIDE)
			desktop.getCytoPanel(position).setState(CytoPanelState.DOCK);
		else
			desktop.getCytoPanel(position).setState(CytoPanelState.HIDE);
	} 

	/**
	 * This dynamically sets the title of the menu based on the state of the CytoPanel.
	 */
	public void menuSelected(MenuEvent me) {
		CytoPanelState curState = desktop.getCytoPanel(position).getState();
		if (curState == CytoPanelState.HIDE) {
			putValue(Action.NAME, SHOW + " " + title);
		} else {
			putValue(Action.NAME, HIDE + " " + title);
		}
	}
}
