package org.cytoscape.internal.layout.ui;

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

import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



/**
 * StaticLayoutMenu provides a simple menu item to be added to a layout
 * menu.
 */
public class StaticLayoutMenu extends JMenuItem implements ActionListener {
    private final static long serialVersionUID = 1202339874301391L;
    private final CyLayoutAlgorithm layout;
    private final DialogTaskManager tm;
    private final CyApplicationManager appMgr;

    /**
     * Creates a new StaticLayoutMenu object.
     *
     * @param layout  DOCUMENT ME!
     */
    public StaticLayoutMenu(CyLayoutAlgorithm layout, boolean enabled, CyApplicationManager appMgr, DialogTaskManager tm) {
        super(layout.toString());
        this.layout = layout;
        this.appMgr = appMgr;
        this.tm = tm;
        addActionListener(this);
        setEnabled(enabled);
    }

    /**
     *  DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent e) {
    	for ( CyNetworkView view : appMgr.getSelectedNetworkViews())
    		tm.execute(layout.createTaskIterator(view,layout.getDefaultLayoutContext(),CyLayoutAlgorithm.ALL_NODE_VIEWS,""));
    }
}