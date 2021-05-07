package org.cytoscape.ding.impl.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;

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
public class SimpleRootPaneContainer extends JComponent implements RootPaneContainer {

	/**
     * The <code>JRootPane</code> instance that manages the <code>contentPane</code>
     * and optional <code>menuBar</code> for this frame, as well as the <code>glassPane</code>.
     */
	protected JRootPane rootPane;
	
	/**
     * If true then calls to <code>add</code> and <code>setLayout</code>
     * will be forwarded to the <code>contentPane</code>. This is initially
     * false, but is set to true when the <code>NetworkViewContainer</code> is constructed.
     */
    private boolean rootPaneCheckingEnabled;
	
    public SimpleRootPaneContainer() {
    	setRootPane(createDefaultRootPane());
    	setLayout(new BorderLayout());
		add(getRootPane(), BorderLayout.CENTER);
	}
    
	@Override
	public Container getContentPane() {
        return getRootPane().getContentPane();
    }
	
	@Override
	public void setContentPane(final Container c) {
		Container oldValue = getContentPane();
        getRootPane().setContentPane(c);
        firePropertyChange("contentPane", oldValue, c);
	}
	
	@Override
	public JLayeredPane getLayeredPane() {
		return getRootPane().getLayeredPane();
	}
	
	@Override
    public void setLayeredPane(JLayeredPane layered) {
        final JLayeredPane oldValue = getLayeredPane();
        getRootPane().setLayeredPane(layered);
        firePropertyChange("layeredPane", oldValue, layered);
    }
	
	@Override
	public Component getGlassPane() {
		return getRootPane().getGlassPane();
	}
	
	@Override
    public void setGlassPane(Component glass) {
        Component oldValue = getGlassPane();
        getRootPane().setGlassPane(glass);
        firePropertyChange("glassPane", oldValue, glass);
    }
	
	@Override
	public JRootPane getRootPane() {
        return rootPane;
    }
	
	protected JRootPane createDefaultRootPane() {
		return new JRootPane();
	}
	
	protected void setRootPane(JRootPane root) {
		if (rootPane != null)
			remove(rootPane);
		
		JRootPane oldValue = getRootPane();
		rootPane = root;
		
		if (rootPane != null) {
			boolean checkingEnabled = isRootPaneCheckingEnabled();
			
			try {
				setRootPaneCheckingEnabled(false);
				add(rootPane, BorderLayout.CENTER);
			} finally {
				setRootPaneCheckingEnabled(checkingEnabled);
			}
		}
		
		firePropertyChange("rootPane", oldValue, root);
    }
	
    /**
     * Removes the specified component from the container. If
     * <code>comp</code> is not the <code>rootPane</code>, this will forward
     * the call to the <code>contentPane</code>. This will do nothing if
     * <code>comp</code> is not a child of the <code>JFrame</code> or <code>contentPane</code>.
     */
	@Override
	public void remove(Component comp) {
		final int oldCount = getComponentCount();
		super.remove(comp);
		
		if (oldCount == getComponentCount())
			getContentPane().remove(comp);
	}

    /**
     * Overridden to conditionally forward the call to the <code>contentPane</code>.
     * Refer to {@link javax.swing.RootPaneContainer} for more information.
     */
	@Override
	public void setLayout(LayoutManager manager) {
		if (isRootPaneCheckingEnabled())
			getContentPane().setLayout(manager);
		else
			super.setLayout(manager);
	}
	
	/**
     * This method is overridden to conditionally forward calls to the <code>contentPane</code>.
     * By default, children are added to the <code>contentPane</code> instead
     * of the frame, refer to {@link javax.swing.RootPaneContainer} for details.
     */
	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		if (isRootPaneCheckingEnabled())
			getContentPane().add(comp, constraints, index);
		else
			super.addImpl(comp, constraints, index);
	}
	
	private boolean isRootPaneCheckingEnabled() {
		return rootPaneCheckingEnabled;
	}

	private void setRootPaneCheckingEnabled(boolean enabled) {
		rootPaneCheckingEnabled = enabled;
	}
}
