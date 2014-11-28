package org.cytoscape.network.merge.internal.ui;

/*
 * #%L
 * Cytoscape Merge Impl (network-merge-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2004 - 2013
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

/*
 * User: Vuk Pavlovic
 * Date: Nov 29, 2006
 * Time: 5:34:46 PM
 * Description: The user-triggered collapsable panel containing the component (trigger) in the titled border
 */

import java.util.EventListener;
import java.util.Vector;

import javax.swing.JRadioButton;

import org.cytoscape.util.swing.BasicCollapsiblePanel;


@SuppressWarnings("serial")
public class CollapsiblePanel extends BasicCollapsiblePanel {
	
	public interface CollapseListener extends EventListener {
		
		public void collapsed();
		public void expanded();
	}

	private final Vector<CollapseListener> collapseListeners = new Vector<CollapseListener>();

    public CollapsiblePanel(JRadioButton component) {
    	super(component);
    }

    public CollapsiblePanel(final String title) {
    	super(title);
    }
    
	public void addCollapseListener(CollapseListener listener) {
		collapseListeners.add(listener);
	}

	public boolean removeCollapeListener(CollapseListener listener) {
		return collapseListeners.remove(listener);
	}
	
	@Override
    public void setCollapsed(final boolean collapse) {
    	super.setCollapsed(collapse);
    	
    	if (collapseListeners != null) {
	        for (CollapseListener listener : collapseListeners) {
	        	if (collapse)
	        		listener.collapsed();
	        	else
	        		listener.expanded();
	        }
    	}
    }
}
