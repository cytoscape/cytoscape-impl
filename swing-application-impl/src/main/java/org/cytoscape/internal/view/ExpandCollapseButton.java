package org.cytoscape.internal.view;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.UIManager;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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


@SuppressWarnings("serial")
public class ExpandCollapseButton extends JButton {
	
	static final int WIDTH = 32;
	static final int HEIGHT = 24;
	
	public ExpandCollapseButton(boolean selected, ActionListener al) {
        setRequestFocusEnabled(true);
        setBorderPainted(false);
		setContentAreaFilled(false);
		setOpaque(false);
		setFocusPainted(false);
		setForeground(UIManager.getColor("Label.infoForeground"));
		
		final Dimension d = new Dimension(WIDTH, HEIGHT);
		setMinimumSize(d);
		setPreferredSize(d);
		setMaximumSize(d);
		setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
		
		addActionListener(al);
		setSelected(selected);
    }
    
    @Override
    public void setSelected(final boolean b) {
		setIcon(UIManager.getIcon(b ? "Tree.expandedIcon" : "Tree.collapsedIcon"));
    	super.setSelected(b);
    }
}
