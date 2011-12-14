/*
  File: ExitAction.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

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

// $Revision: 12968 $
// $Date: 2008-02-06 15:34:25 -0800 (Wed, 06 Feb 2008) $
// $Author: mes $
package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import javax.swing.KeyStroke;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.swing.AbstractCyAction;

/**
 *
 */
public class ExitAction extends AbstractCyAction {
    
    private final static long serialVersionUID = 1202339869460858L;
    
    protected int returnVal;
    private final CyShutdown shutdown;

    /**
     * Creates a new ExitAction object.
     */
    public ExitAction(CyShutdown shutdown) {
	super("Quit");
	this.shutdown = shutdown;
	setPreferredMenu("File");

	String osName = System.getProperty("os.name").toLowerCase();
	//boolean isMacOs = osName.startsWith("mac os x");
	
	if (osName.indexOf("mac") != -1) {    
		// TODO: need to create special case for Mac to use apple key.
		// do mac-specific things here
		//setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_MASK));	
		setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.VK_CONTROL));
	}
	else {
		setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));		
	}
	setMenuGravity(1000.0f);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	shutdown.exit(returnVal);
    }
}
