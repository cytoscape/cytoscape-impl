package org.cytoscape.work.internal.tunables.utils;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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


import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandler;


@SuppressWarnings("serial")
public class XorPanel extends JPanel {
	private JPanel switchPanel;
	private JPanel contentPanel;
	private JPanel currentPanel = null;

	private GUITunableHandler gh;
	private boolean first = true;

	public XorPanel(final String title, final GUITunableHandler g) {
		super();

		gh = g;
		gh.addDependent(new GUITunableHandlerSwitchListener());

		switchPanel = new JPanel();
		contentPanel = new JPanel(new CyCardLayout());
		TitledBorder titleborder = BorderFactory.createTitledBorder(title);
		titleborder.setTitleColor(Color.GREEN);
		setBorder(titleborder);
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		//setBorder(BorderFactory.createTitledBorder(title));
		super.add(switchPanel);
		super.add(contentPanel);
	}

	public Component add(Component c) {
		if (first) {
			switchPanel.add(c);
			first = false;
			return c;
		} else {
			if (currentPanel == null)
				throw new RuntimeException("current panel is null.");

			currentPanel.add(c);
			return c;
		}
	}

	public void add(Component c, Object constraint) {
		if (first) {
			switchPanel.add(c);
			first = false;
		} else {
			currentPanel = (JPanel)c;
			contentPanel.add(c,constraint);
		}
	}

	class GUITunableHandlerSwitchListener implements GUITunableHandler {
		public Tunable getTunable() {return null;}
		public Field getField() {return null;}
		public Object getObject() {return null;}
		public void actionPerformed(ActionEvent ae) { }
		public void notifyDependents() { }
		public void addDependent(GUITunableHandler gh) { }
		public String getDependency() { return null; }
		public void resetValue(){}

		public void checkDependency(String name, String state) {
			CardLayout cl = (CardLayout) contentPanel.getLayout();
			cl.show(contentPanel, state);
			repackEnclosingDialog();
		}
		
		private void repackEnclosingDialog() {
			Container container = contentPanel.getParent();
			while (container != null && !(container instanceof JDialog))
				container = container.getParent();
			if (container != null)
			{
				((JDialog)container).pack();
			}
		}

		@Override public String dependsOn() { return null; }
		@Override public String getChildKey() { return null; }
		@Override public boolean controlsMutuallyExclusiveNestedChildren() { return false; }
		@Override public String[] getGroups() { return null; }
		@Override public double getGravity() { return 0.0; }
		@Override public String getDescription() { return null; }
		@Override public Object getValue() { return null; }
		@Override public void setValue(final Object newValue) { }
		@Override public String getQualifiedName() { return null; }
		@Override public Properties getParams() { return null; }
		@Override public Class<?> getType() { return null; }
		public String getName() { return null; }
		public JPanel getJPanel() { return null; }
		public void handle() {}
		public void update() {}
		public void notifyChangeListeners() {}
		public void addChangeListener(GUITunableHandler gh) {}
		public String[] getChangeSources() { return null; }
		public String[] listenForChange() { return null; }
		public void changeOccurred(final String name, final String state){}
		public String getState() {return null;}
		public void returnPanel() {}
	}
}
