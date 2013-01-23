package org.cytoscape.internal.actions;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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
