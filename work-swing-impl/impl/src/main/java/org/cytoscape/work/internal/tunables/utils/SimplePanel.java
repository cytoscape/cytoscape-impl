package org.cytoscape.work.internal.tunables.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.cytoscape.work.swing.AbstractGUITunableHandler.TunableFieldPanel;
import org.cytoscape.work.swing.GUITunableHandler;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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
public class SimplePanel extends JPanel implements HierarchyListener {

	protected final boolean vertical;
	protected final String title;
	
	protected JPanel contentPane;
	
	public SimplePanel(final boolean vertical) {
		this(vertical, null);
	}
	
	protected SimplePanel(final boolean vertical, final String title) {
		this.vertical = vertical;
		this.title = title;
		init();
		initComponents();
	}
	
	protected void init() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}
	
	protected void initComponents() {
		addToRoot(getContentPane());
	}
	
	@Override
	public Dimension getMaximumSize() {
		final Dimension d = super.getMaximumSize();
		d.width = Integer.MAX_VALUE;
		
		return d;
	}
	
	@Override
	public Component add(Component c) {
		adjust(c);
		return getContentPane().add(c);
	}

	@Override
	public void add(Component c, Object constraint) {
		adjust(c);
		getContentPane().add(c, constraint);
	}
	
	protected Component addToRoot(final Component c) {
		adjust(c);
		return super.add(c);
	}
	
	protected void addToRoot(final Component c, final Object constraint) {
		adjust(c);
		super.add(c, constraint);
	}
	
	protected void addStrutToRoot(int size) {
		addToRoot(Box.createVerticalStrut(size));
	}
	
	protected void adjust(final Component c) {
		if (c instanceof JPanel) {
			((JPanel) c).setAlignmentX(Component.CENTER_ALIGNMENT);
			
			if (!vertical)
				((JPanel) c).setAlignmentY(Component.TOP_ALIGNMENT);
		}
	}
	
	protected JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setLayout(new BoxLayout(contentPane, vertical ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
		}
		
		return contentPane;
	}
	
	@Override
    public void addNotify() {
        super.addNotify();
        addHierarchyListener(this);
    }

	@Override
    public void removeNotify() {
        removeHierarchyListener(this);
        super.removeNotify();
    }

	@Override
    public void hierarchyChanged(HierarchyEvent e) {
		// Hide this panel if all of its fields are disabled, so the UI is not cluttered with
		// fields that cannot be interacted with. However, if any of its children is a tunable that
		// depends on another tunable, we cannot hide it even when everything is disabled,
		// since it can become enabled again at any time the field it depends on has its value changed.
        if (isDisplayed() && !hasDependentFields(this) && !hasEnabledControls(this))
        		setVisible(false);
    }
	
	public boolean hasDependentFields(final JPanel panel) {
		final int total = panel.getComponentCount();

		for (int i = 0; i < total; i++) {
			final Component c = panel.getComponent(i);

			if (c.isVisible()) {
				if (c instanceof TunableFieldPanel) {
					GUITunableHandler gh = ((TunableFieldPanel) c).getTunableHandler();
					
					// Does this tunable field depend on another tunable field's value
					if (gh != null && gh.dependsOn() != null && !gh.dependsOn().trim().isEmpty())
						return true;
				} else if (c instanceof JPanel) {
					if (hasDependentFields((JPanel) c))
						return true;
				}
			}
		}

		return false;
    }
	
	public boolean hasEnabledControls(final JPanel panel) {
		final int total = panel.getComponentCount();
		
		for (int i = 0; i < total; i++) {
			final Component c = panel.getComponent(i);
			
			if (c.isVisible() && c.isEnabled()) {
				if (c instanceof JPanel) {
					if (hasEnabledControls((JPanel) c))
						return true;
				} else if (c instanceof Filler == false) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean isDisplayed() {
        Container c = getParent();
        
        while (c != null) {
            if (!c.isVisible())
                return false;
            else
                c = c.getParent();
        }
        
        return true;
    }
}
