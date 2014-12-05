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
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandler;


@SuppressWarnings("serial")
public class XorPanel extends TitledPanel {
	
	private JPanel switchPanel;
	private JPanel currentPanel;

	private GUITunableHandler gh;
	private boolean first = true;

	/**
	 * @param title Panel title
	 * @param g
	 */
	public XorPanel(final String title, final GUITunableHandler g) {
		super(title, BoxLayout.PAGE_AXIS);
		gh = g;
		gh.addDependent(new GUITunableHandlerSwitchListener());
	}
	
	@Override
	public Component add(Component c) {
		adjust(c);
		
		if (first) {
			getSwitchPanel().add(c);
			first = false;
		} else {
			if (currentPanel == null)
				throw new RuntimeException("current panel is null.");

			getContentPane().add(c);
		}
		
		return c;
	}

	@Override
	public void add(Component c, Object constraint) {
		adjust(c);
		
		if (first) {
			getSwitchPanel().add(c);
			first = false;
		} else {
			currentPanel = (JPanel)c;
			getContentPane().add(c, constraint);
		}
	}

	@Override
	protected void initComponents() {
		super.initComponents();
		remove(getContentPane());
		
		addToRoot(getSwitchPanel());
		addToRoot(getContentPane());
	}
	
	@Override
	protected JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = super.getContentPane();
			contentPane.setLayout(new CyCardLayout());
		}
		
		return contentPane;
	}
	
	protected JPanel getSwitchPanel() {
		if (switchPanel == null) {
			switchPanel = new JPanel();
			switchPanel.setLayout(new BoxLayout(switchPanel, axis));
		}
		
		return switchPanel;
	}
	
	class GUITunableHandlerSwitchListener implements GUITunableHandler {
		
		public Tunable getTunable() {return null;}
		public Field getField() {return null;}
		public Object getObject() {return null;}
		public void actionPerformed(ActionEvent ae) { }
		@Override public void notifyDependents() { }
		@Override public void addDependent(GUITunableHandler gh) { }
		@Override public String getDependency() { return null; }
		public void resetValue(){}

		@Override 
		public void checkDependency(String name, String state) {
			CardLayout cl = (CardLayout) getContentPane().getLayout();
			cl.show(getContentPane(), state);
			repackEnclosingDialog();
		}
		
		private void repackEnclosingDialog() {
			Container container = getContentPane().getParent();
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
		@Override public String getDescription() { return null; }
		@Override public Object getValue() { return null; }
		@Override public void setValue(final Object newValue) { }
		@Override public String getQualifiedName() { return null; }
		@Override public Properties getParams() { return null; }
		@Override public Class<?> getType() { return null; }
		@Override public String getName() { return null; }
		@Override public JPanel getJPanel() { return null; }
		@Override public void handle() {}
		@Override public void update() {}
		@Override public void notifyChangeListeners() {}
		@Override public void addChangeListener(GUITunableHandler gh) {}
		@Override public String[] getChangeSources() { return null; }
		@Override public String[] listenForChange() { return null; }
		@Override public void changeOccurred(final String name, final String state){}
		@Override public String getState() {return null;}
		public void returnPanel() {}
	}
}
