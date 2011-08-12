/*
 File: CytoPanelTaskFactoryTunableAction.java

 Copyright (c) 2010, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.internal.task;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Icon;
import java.awt.Component;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.TunableValidator.ValidationState;
import org.cytoscape.work.swing.GUITaskManager;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  This class is used to provide actions for task factories that have been annotated with tunables and therefore
 *  should end up wrapped in CytoPanel components.
 */
public class CytoPanelTaskFactoryTunableAction extends AbstractCyAction {
	/**
	 *  A listener that upon receiving the button-click event validates the tunables and then
	 *  creates and executes a task.
	 */
	private static class ExecuteButtonListener implements ActionListener {
		final private TaskFactory factory;
		final private GUITaskManager manager;

		ExecuteButtonListener(final TaskFactory factory, final GUITaskManager manager) {
			this.factory = factory;
			this.manager = manager;
		}

		public void actionPerformed(final ActionEvent event) {
			// Perform input validation?
			if (factory instanceof TunableValidator) {
				final Appendable errMsg = new StringBuilder();
				try {
					final ValidationState validationState =
						((TunableValidator)factory).getValidationState(errMsg);
					if (validationState == ValidationState.INVALID) {
						JOptionPane.showMessageDialog(new JFrame(), errMsg.toString(),
									      "Input Validation Problem",
									      JOptionPane.ERROR_MESSAGE);
						return;
					} else if (validationState == ValidationState.REQUEST_CONFIRMATION) {
						if (JOptionPane.showConfirmDialog(new JFrame(), errMsg.toString(),
										  "Request Confirmation",
										  JOptionPane.YES_NO_OPTION)
						    == JOptionPane.NO_OPTION)
							return;
					}
				} catch (final Exception e) {
					e.printStackTrace();
					return;
				}
			}

			manager.execute(factory);
		}
	}


	final private static CytoPanelName DEFAULT_CYTOPANEL = CytoPanelName.WEST;
	final private TaskFactory factory;
	final private GUITaskManager manager;
	final private Map<String, String> serviceProps;
	final private CytoPanelName cytoPanelName;
	final private CyServiceRegistrar registrar;
	final private static Logger logger = LoggerFactory.getLogger(CytoPanelTaskFactoryTunableAction.class);

	public CytoPanelTaskFactoryTunableAction(final TaskFactory factory, 
	                                         final GUITaskManager manager,
	                                         final Map<String, String> serviceProps, 
	                                         final CyApplicationManager appMgr,
											 final CyServiceRegistrar registrar)
	{
		super(serviceProps, appMgr);

		this.factory = factory;
		this.manager = manager;
		this.serviceProps = serviceProps;
		this.registrar = registrar;
		this.cytoPanelName = getCytoPanelName(); 
	}

	private CytoPanelName getCytoPanelName() {
		CytoPanelName n;
		try {
			Object name = serviceProps.get("preferredCytoPanel");
			if ( name != null )
				n = CytoPanelName.valueOf(name.toString());
			else 
				n = CytoPanelName.WEST;
		} catch (Exception e) {
			logger.warn("couldn't find 'preferredCytoPanel' property",e);
			n = CytoPanelName.WEST;
		}
		return n;
	}

	/**
	 *  Creates a new CytoPanel component and adds it to a CytoPanel.
	 */
	public void actionPerformed(final ActionEvent a) {
		final JPanel innerPanel = manager.getConfigurationPanel(factory);
		if (innerPanel == null)
			return;

		CytoPanelComponentImp imp = new CytoPanelComponentImp(innerPanel,
		                                                      getCytoPanelComponentTitle());
		registrar.registerService(imp,CytoPanelComponent.class,new Properties());
	}

	/**
	 *  Attempts to provide a title for our new CytoPanel component.  First we try to get the value of the
	 *  "cytoPanelComponentTitle" from the service properties, if that fails we attempt to generate a title
	 *  from the menu title which can be found in the "title" service property entry.  If all this fails we
	 *  provide a goofy title that is only really useful for debugging.
	 */
	private String getCytoPanelComponentTitle() {
		try {
			final String cytoPanelComponentTitle = (String)serviceProps.get("cytoPanelComponentTitle");
			if (cytoPanelComponentTitle != null)
				return cytoPanelComponentTitle;

			// Try to create a panel component title from the menu item:
			final String menuTitle = (String)serviceProps.get("title");
			if (menuTitle != null) {
				if (menuTitle.endsWith("..."))
					return menuTitle.substring(0, menuTitle.length() - 3);
				else
					return menuTitle;
			}

			return "*No Title*";
		} catch (final ClassCastException e) {
			logger.warn("This should *never* happen!",e);
			return "*Missing Title*";
		}
	}

	private class CytoPanelComponentImp implements CytoPanelComponent {
		private final Component innerPanel;
		private final Component comp;
		private final String title;
		CytoPanelComponentImp(Component innerPanel, String title) {
			this.innerPanel = innerPanel;
			this.title = title;
			this.comp = createComponent();
		}
		public String getTitle() { return title; }
		public CytoPanelName getCytoPanelName() { return cytoPanelName; }
		public Icon getIcon() { return null; }
		public Component getComponent() { return comp; }
		private Component createComponent() { 
			final JPanel outerPanel = new JPanel();
			outerPanel.add(innerPanel);

			final JButton executeButton = new JButton("Execute");
			executeButton.addActionListener(new ExecuteButtonListener(factory, manager));
			outerPanel.add(executeButton);
	
			final JButton closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent event) {
						registrar.unregisterService(this,CytoPanelComponent.class);	
					}
				});
			outerPanel.add(closeButton);
		
			return outerPanel;
		}
	}
}
