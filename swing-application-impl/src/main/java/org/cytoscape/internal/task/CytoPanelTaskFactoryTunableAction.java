package org.cytoscape.internal.task;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.TunableValidator.ValidationState;
import org.cytoscape.work.swing.PanelTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2016 The Cytoscape Consortium
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

/**
 *  This class is used to provide actions for task factories that have been annotated with tunables and therefore
 *  should end up wrapped in CytoPanel components.
 */
@SuppressWarnings("serial")
public class CytoPanelTaskFactoryTunableAction extends AbstractCyAction {
	
	/**
	 *  A listener that upon receiving the button-click event validates the tunables and then
	 *  creates and executes a task.
	 */
	private static class ExecuteButtonListener implements ActionListener {
		
		private final TaskFactory factory;
		private final Object context;
		private CyServiceRegistrar serviceRegistrar;

		ExecuteButtonListener(final TaskFactory factory, Object context, final CyServiceRegistrar serviceRegistrar) {
			this.factory = factory;
			this.context = context;
			this.serviceRegistrar = serviceRegistrar;
		}

		@Override
		public void actionPerformed(final ActionEvent event) {
			// Perform input validation?
			if (context instanceof TunableValidator) {
				final Appendable errMsg = new StringBuilder();
				try {
					final ValidationState validationState =
						((TunableValidator)context).getValidationState(errMsg);
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

			final PanelTaskManager taskManager = serviceRegistrar.getService(PanelTaskManager.class);
			taskManager.execute(factory.createTaskIterator());
		}
	}


	final private TaskFactory factory;
	final private Object context;
	final private Map<String, String> serviceProps;
	final private CytoPanelName cytoPanelName;
	final private CyServiceRegistrar serviceRegistrar;
	final private static Logger logger = LoggerFactory.getLogger(CytoPanelTaskFactoryTunableAction.class);

	public CytoPanelTaskFactoryTunableAction(
			final TaskFactory factory,
			final Object context,
	        final Map<String, String> serviceProps,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(serviceProps, serviceRegistrar.getService(CyApplicationManager.class),
				serviceRegistrar.getService(CyNetworkViewManager.class));

		this.factory = factory;
		this.serviceProps = serviceProps;
		this.serviceRegistrar = serviceRegistrar;
		this.cytoPanelName = getCytoPanelName();
		this.context = context;
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
	@Override
	public void actionPerformed(final ActionEvent a) {
		final PanelTaskManager taskManager = serviceRegistrar.getService(PanelTaskManager.class);
		final JPanel innerPanel = taskManager.getConfiguration(factory, context);
		
		if (innerPanel == null)
			return;

		CytoPanelComponentImp imp = new CytoPanelComponentImp(innerPanel,
		                                                      getCytoPanelComponentTitle());
		serviceRegistrar.registerService(imp,CytoPanelComponent.class,new Properties());
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
			logger.warn("This should *never* happen.",e);
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
		
		@Override
		public String getTitle() { return title; }
		
		@Override
		public CytoPanelName getCytoPanelName() { return cytoPanelName; }
		
		@Override
		public Icon getIcon() { return null; }
		
		@Override
		public Component getComponent() { return comp; }
		
		private Component createComponent() { 
			final JPanel outerPanel = new JPanel();
			outerPanel.add(innerPanel);

			final JButton executeButton = new JButton("Execute");
			executeButton.addActionListener(new ExecuteButtonListener(factory, context, serviceRegistrar));
			outerPanel.add(executeButton);
	
			final JButton closeButton = new JButton("Close");
			closeButton.addActionListener((final ActionEvent event) -> {
				serviceRegistrar.unregisterService(this,CytoPanelComponent.class);	
			});
			outerPanel.add(closeButton);
		
			return outerPanel;
		}
	}
}
