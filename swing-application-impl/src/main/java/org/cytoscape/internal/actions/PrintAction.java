/*
  File: PrintAction.java

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

//-------------------------------------------------------------------------
// $Revision: 12984 $
// $Date: 2008-02-08 13:12:37 -0800 (Fri, 08 Feb 2008) $
// $Author: mes $
//-------------------------------------------------------------------------
package org.cytoscape.internal.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterJob;
import java.util.Properties;
import java.util.Set;

import javax.swing.KeyStroke;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngine;


public class PrintAction extends AbstractCyAction {

	private final static long serialVersionUID = 1202339870257629L;

	private final static String MENU_LABEL = "Print Current Network...";
	private final Properties props;
	private final CyApplicationManager appMgr;

	/**
	 * Creates a new PrintAction object.
	 */
	public PrintAction(CyApplicationManager appMgr, final CyNetworkViewManager networkViewManager, CyProperty<Properties> coreProp) {
		super(MENU_LABEL, appMgr, "networkAndView", networkViewManager);
		this.appMgr = appMgr;

		setPreferredMenu("File");
		setMenuGravity(7.0f);
		setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		this.props = coreProp.getProperties();
	}
	
	public void actionPerformed(ActionEvent e) {
		final RenderingEngine<CyNetwork> engine = appMgr.getCurrentRenderingEngine();

		final PrinterJob printJob = PrinterJob.getPrinterJob();
		
		// TODO: pick only required props
		final Set<Object> keys = props.keySet();
		for(Object key: keys)
			engine.getProperties().put(key, props.get(key));

		printJob.setPrintable(engine.createPrintable());

		if (printJob.printDialog()) {
			try {
				printJob.print();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}
}
