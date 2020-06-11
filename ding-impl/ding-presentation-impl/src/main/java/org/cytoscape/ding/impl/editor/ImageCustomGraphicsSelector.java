package org.cytoscape.ding.impl.editor;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphicsmgr.internal.ui.CustomGraphicsBrowser;
import org.cytoscape.ding.customgraphicsmgr.internal.ui.CustomGraphicsManagerDialog;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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
public class ImageCustomGraphicsSelector extends JPanel {
	
	/** Flag to ensure that infinite loops do not occur with ActionEvents. */
    private boolean firingActionEvent;
    private String actionCommand = "imageCustomGraphicsSelectorChanged";
	
	private DiscreteValueList<CyCustomGraphics> graphicsList;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ImageCustomGraphicsSelector(CustomGraphicsBrowser browser, CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		var scrollPane = new JScrollPane();
		scrollPane.setViewportView(getGraphicsList());
		
		var openImgMgrBtn = new JButton(new AbstractAction("Open Image Manager...") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				var owner = SwingUtilities.getWindowAncestor(ImageCustomGraphicsSelector.this);
				var customGraphicsMgr = serviceRegistrar.getService(CustomGraphicsManager.class);
				
				var dialog = new CustomGraphicsManagerDialog(owner, customGraphicsMgr, browser, serviceRegistrar);
				dialog.setVisible(true);
				
				update(getSelectedValue());
			}
		});
		
		var layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(openImgMgrBtn, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(openImgMgrBtn, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		update((CyCustomGraphics) null);
	}
	
	public void update(CyCustomGraphics selectedValue) {
		var customGraphicsMgr = serviceRegistrar.getService(CustomGraphicsManager.class);
		var newValues = customGraphicsMgr.getAllCustomGraphics();
		
		getGraphicsList().setListItems(newValues, selectedValue);
	}
	
	public CyCustomGraphics getSelectedValue() {
		return (CyCustomGraphics) getGraphicsList().getSelectedValue();
	}
	
	public void setSelectedValue(CyCustomGraphics value) {
		if (value != null) // The list's selection mode must be SINGLE_SELECTION!
			getGraphicsList().setSelectedValue(value, true); 
		else
			getGraphicsList().clearSelection();
	}
	
	/**
	 * Add a listener to be notified when the user finally chooses an image (e.g. double-click an item).
	 */
	public void addActionListener(ActionListener l) {
		listenerList.add(ActionListener.class, l);
	}

	public void removeActionListener(ActionListener l) {
		listenerList.remove(ActionListener.class, l);
	}

	public ActionListener[] getActionListeners() {
		return listenerList.getListeners(ActionListener.class);
	}
	
	/**
     * Sets the action command that should be included in the event
     * sent to action listeners.
     *
     * @param aCommand  a string containing the "command" that is sent
     *                  to action listeners; the same listener can then
     *                  do different things depending on the command it
     *                  receives
     */
    public void setActionCommand(String aCommand) {
        actionCommand = aCommand;
    }

    /**
     * Returns the action command that is included in the event sent to
     * action listeners.
     *
     * @return  the string containing the "command" that is sent
     *          to action listeners.
     */
    public String getActionCommand() {
        return actionCommand;
    }
	
	protected void fireActionEvent() {
		if (!firingActionEvent) {
			// Set flag to ensure that an infinite loop is not created
			firingActionEvent = true;
			ActionEvent e = null;
			
			// Guaranteed to return a non-null array
			Object[] listeners = listenerList.getListenerList();
			long mostRecentEventTime = EventQueue.getMostRecentEventTime();
			int modifiers = 0;
			AWTEvent currentEvent = EventQueue.getCurrentEvent();
			
			if (currentEvent instanceof InputEvent)
				modifiers = ((InputEvent) currentEvent).getModifiers();
			else if (currentEvent instanceof ActionEvent)
				modifiers = ((ActionEvent) currentEvent).getModifiers();
			
			try {
				// Process the listeners last to first, notifying those that are interested in this event
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == ActionListener.class) {
						// Lazily create the event:
						if (e == null)
							e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand(),
									mostRecentEventTime, modifiers);
						((ActionListener) listeners[i + 1]).actionPerformed(e);
					}
				}
			} finally {
				firingActionEvent = false;
			}
		}
    }
	
	protected DiscreteValueList<CyCustomGraphics> getGraphicsList() {
		if (graphicsList == null) {
			var defViewPanel = serviceRegistrar.getService(DefaultViewPanel.class);
			
			graphicsList = new DiscreteValueList<>(CyCustomGraphics.class, defViewPanel);
			graphicsList.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent evt) {
					if (evt.getClickCount() == 2)
						fireActionEvent();
				}
			});
		}
		
		return graphicsList;
	}
}