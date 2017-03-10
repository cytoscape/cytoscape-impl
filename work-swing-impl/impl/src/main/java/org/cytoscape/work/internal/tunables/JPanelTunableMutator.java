package org.cytoscape.work.internal.tunables;

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


import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.AbstractTunableInterceptor;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TunableMutator;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.TunableValidator.ValidationState;
import org.cytoscape.work.internal.tunables.utils.SimplePanel;
import org.cytoscape.work.internal.tunables.utils.TitledPanel;
import org.cytoscape.work.internal.tunables.utils.XorPanel;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.swing.AbstractGUITunableHandler.TunableFieldPanel;
import org.cytoscape.work.swing.DirectlyPresentableTunableHandler;
import org.cytoscape.work.swing.GUITunableHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor of <code>Tunable</code> that will be applied on <code>GUITunableHandlers</code>.
 *
 * <p><pre>
 * To set the new value to the original objects contained in the <code>GUITunableHandlers</code>:
 * <ul>
 *   <li>Creates the parent container for the GUI, or use the one that is specified </li>
 *   <li>Creates a GUI with swing components for each intercepted <code>Tunable</code> </li>
 *   <li>
 *     Displays the GUI to the user, following the layout construction rule specified in the <code>Tunable</code>
 *     annotations, and the dependencies to enable or not the graphic components
 *   </li>
 *   <li>
 *     Applies the new <i>value,item,string,state...</i> to the object contained in the <code>GUITunableHandler</code>,
 *     if the modifications have been validated by the user.
 *   </li>
 * </ul>
 * </pre></p>
 *
 * @author pasteur
 */
public class JPanelTunableMutator extends AbstractTunableInterceptor<GUITunableHandler> 
								  implements TunableMutator<GUITunableHandler, JPanel> {

	private static final String TOP_GROUP = "__CY_TOP_GROUP";
	
	private Map<List<GUITunableHandler>, JPanel> panelMap;
	
	/** A reference to the parent <code>JPanel</code>, if any. */
	private JPanel tunablePanel;

	/** Provides an initialised logger. */
	private final Logger logger = LoggerFactory.getLogger(JPanelTunableMutator.class);

	/** Do not ever modify this panel. Used for special case handling of files. */
	protected final JPanel HANDLER_CANCEL_PANEL = new JPanel();
	
	private boolean updatingMargins;
	private final ComponentListener controlComponentListener;
	protected List<GUITunableHandler> handlers;
	
	private final Object lock = new Object();

	public JPanelTunableMutator() {
		super();
		panelMap = new HashMap<>();
		
		controlComponentListener = new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
				updateTunableFieldPanelMargins();
			}
			@Override
			public void componentResized(ComponentEvent e) {
				updateTunableFieldPanelMargins();
			}
			@Override
			public void componentMoved(ComponentEvent e) {
				updateTunableFieldPanelMargins();
			}
			@Override
			public void componentHidden(ComponentEvent e) {
				updateTunableFieldPanelMargins();
			}
		};
	}
	
	@Override
	public void setConfigurationContext(final Object tPanel){
		if (tPanel instanceof JPanel)
			this.tunablePanel = (JPanel)tPanel;
		else
			throw new IllegalArgumentException("this tunable mutator only works with JPanels - " +
			                                   "set the configuration context to a JPanel");
	}
	
	@Override
	public boolean hasTunables(final Object o) {
		return super.hasTunables(o);
	}

	public boolean hasTunables(final Object o, String context) {
		return getApplicableHandlers(o, context).size() > 0;
	}

	@Override
	public boolean validateAndWriteBack(Object objectWithTunables) {
		List<GUITunableHandler> handlers = getHandlers(objectWithTunables); 

		for (final GUITunableHandler h : handlers)
			h.handle();

		return validateTunableInput(objectWithTunables); 
	}

	@Override
	public JPanel buildConfiguration(final Object objectWithTunables) {
		return buildConfiguration(objectWithTunables,null);
	}

	/** 
	 * A special case of buildConfiguration that allows a parent window to be specified.
	 * This special case is when a task only has a single tunable of type File, in
	 * which case we don't want to show the normal tunable dialog, just the file dialog.
	 */
	JPanel buildConfiguration(final Object objectWithTunables, Window possibleParent) {
		int factoryCount = 0; // # of descendents of TaskFactory...
		int otherCount = 0;   // ...everything else.  (Presumeably descendents of Task.)
		
		if (objectWithTunables instanceof TaskFactory)
			++factoryCount;
		else
			++otherCount;

		synchronized (lock) {
			handlers = getApplicableHandlers(objectWithTunables, "gui");

			// Sanity check:
			if (factoryCount > 0) {
				if (factoryCount != 1) {
					logger.error("More than one annotated TaskFactory found.");
					return null;
				} else if (otherCount != 0) {
					logger.error("Found annotated Task objects in addition to an annotated TaskFactory.");
					return null;
				}
			}
	
			if (handlers.isEmpty()) {
				System.out.println("Handlers are empty!");
				if (tunablePanel != null) {
					tunablePanel.removeAll();
					
					return tunablePanel;
				}
				
				return null; 
			}
	
			// This is special case handling for when there is only one tunable specified
			// in a task, in which case we don't want a full tunable dialog
			// and all of the extra clicks, instead we just want to show the special dialog.
			if ( handlers.size() == 1 && handlers.get(0) instanceof DirectlyPresentableTunableHandler ) {
				DirectlyPresentableTunableHandler fh = (DirectlyPresentableTunableHandler) handlers.get(0);
				
				if (fh.isForcedToSetDirectly()){
					boolean fileFound = fh.setTunableDirectly(possibleParent);
					
					return fileFound ? null : HANDLER_CANCEL_PANEL; 
				}
			}
	
			if (!panelMap.containsKey(handlers)) {
				Map<String, JPanel> panels = new HashMap<String, JPanel>();
				final JPanel topLevel = new SimplePanel(true);
				panels.put(TOP_GROUP, topLevel);
	
				// construct the GUI
				for (GUITunableHandler gh : handlers) {
					// hook up dependency listeners
					String dep = gh.getDependency();
					
					if (dep != null && !dep.equals("")) {
						for (GUITunableHandler gh2 : handlers) {
							if (gh2.getName().equals(dep)) {
								gh2.addDependent(gh);
								break;
							}
						}
					}
	
					// hook up change listeners
					for (String cs : gh.getChangeSources()) {
						if (cs != null && !cs.equals("")) {
							for (GUITunableHandler gh2 : handlers) {
								if (gh2.getName().equals(cs)) {
									gh2.addChangeListener(gh);
									break;
								}
							}
						}
					}
	
					// Get information about the Groups and alignment from Tunables Annotations 
					// in order to create the proper GUI
					final Map<String, Boolean> groupToVerticalMap = processGroupParams(gh,"alignments","vertical"); 
					final Map<String, Boolean> groupToDisplayedMap = processGroupParams(gh,"groupTitles","displayed"); 
	
					// find the proper group to put the handler panel in given the Alignment/Group parameters
					String lastGroup = TOP_GROUP;
					String groupNames = "";
					
					for (String g : gh.getGroups()) {
						if (g.equals(""))
							throw new IllegalArgumentException("A group's name must not be \"\".");
						
						groupNames = groupNames + g;
						
						if (!panels.containsKey(groupNames)) {
							panels.put(groupNames,
							           createJPanel(g, gh, groupToVerticalMap.get(g), groupToDisplayedMap.get(g)));
							final JPanel pnl = panels.get(groupNames);
							panels.get(lastGroup).add(pnl, gh.getChildKey());
						}
						
						lastGroup = groupNames;
					}
					
					panels.get(lastGroup).add(gh.getJPanel());
				}
				
				panelMap.put(handlers, panels.get(TOP_GROUP));
			}
	
			updateTunableFieldPanelMargins();
			
			// Get the GUI into the proper state
			for (GUITunableHandler gh : handlers)
				gh.notifyDependents();
	
			// if no tunablePane is defined, then create a new JDialog to display the Tunables' panels
			if (tunablePanel == null) {
				return panelMap.get(handlers);
			} else { // add them to the "tunablePanel" JPanel
				tunablePanel.removeAll();
				tunablePanel.add(panelMap.get(handlers));
				final JPanel retVal = tunablePanel;
				tunablePanel = null;
				
				return retVal;
			}
		}
	}

	public List<GUITunableHandler> getApplicableHandlers(Object objectWithTunables, String desiredContext) {
		List<GUITunableHandler> handlers = getHandlers(objectWithTunables); 

		if (handlers != null ) {
			// Remove any tunables that aren't appropriate for a GUI context
			ListIterator<GUITunableHandler> li = handlers.listIterator();
			
			while (li.hasNext()) {
				GUITunableHandler gh = li.next();
				String context = gh.getParams().get(AbstractTunableHandler.CONTEXT).toString();
				
				if (desiredContext.equalsIgnoreCase("gui")) {
					if (context.equalsIgnoreCase("nogui"))
						li.remove();
				} else if (desiredContext.equalsIgnoreCase("nogui")) {
					if (context.equalsIgnoreCase("gui"))
						li.remove();
				}
			}
		}
		
		return handlers;
	}

	private void updateTunableFieldPanelMargins() {
		synchronized (lock) {
			if (updatingMargins || handlers == null)
				return;
			
			updatingMargins = true;
			int maxLeftWidth = 0, maxRightWidth = 0;
			boolean updateMargins = false;
			
			try {
				// 1st Pass: Get max left/right margin values if vertical form
				for (GUITunableHandler gh : handlers) {
					if (gh instanceof AbstractGUITunableHandler && !((AbstractGUITunableHandler)gh).isHorizontal()) {
						final JPanel p = gh.getJPanel();
						
						if (p instanceof TunableFieldPanel) {
							updateMargins = true;
							
							final TunableFieldPanel tfp = (TunableFieldPanel) p;
							final JComponent label = tfp.getLabel() != null ? tfp.getLabel() : tfp.getMultiLineLabel();
							final Component control = tfp.getControl();
							
							if (label != null)
								maxLeftWidth = Math.max(maxLeftWidth, label.getPreferredSize().width);
							if (control != null)
								maxRightWidth = Math.max(maxRightWidth, control.getPreferredSize().width);
						}
					}
				}
				
				if (updateMargins) {
					// 2nd Pass: Use empty borders to properly align all fields and labels
					for (GUITunableHandler gh : handlers) {
						final JPanel p = gh.getJPanel();
						
						if (p instanceof TunableFieldPanel) {
							// Update panel's left/right margin by setting an empty border
							final TunableFieldPanel tfp = (TunableFieldPanel) p;
							final JComponent label = tfp.getLabel() != null ? tfp.getLabel() : tfp.getMultiLineLabel();
							final Component control = tfp.getControl();
							
							if (control != null)
								control.removeComponentListener(controlComponentListener);
							
							int left = label == null ? 0 : maxLeftWidth - label.getPreferredSize().width;
							int right = control == null ? 0 : maxRightWidth - control.getPreferredSize().width;
							
							tfp.setBorder(BorderFactory.createEmptyBorder(2, left, 2, right)); // TODO Do not set top/bottom
							
							// Also notify all dependents
							gh.notifyDependents();
							
							if (control != null)
								control.addComponentListener(controlComponentListener);
						}
					}
					
					repackEnclosingDialog(panelMap.get(handlers));
				}
			} finally {
				updatingMargins = false;
			}
		}
	}
	
	/**
	 * Creation of a JPanel that will contain panels of <code>GUITunableHandler</code>
	 * This panel will have special features like, ability to collapse, ability to be displayed 
	 * depending on another panel.  A layout will be set for this <i>"container"</i> of panels 
	 * (horizontally or vertically), and a title (displayed or not)
	 *
	 *
	 * @param title of the panel
	 * @param gh provides access to <code>Tunable</code> annotations : see if this panel 
	 * can be collapsable, and if its content will switch between different 
	 * <code>GUITunableHandler</code> panels(depending on <code>xorKey</code>)
	 * @param alignment the way the panels will be set in this <i>"container</i> panel
	 * @param groupTitle parameter to choose whether or not the title of the panel has to be displayed
	 *
	 * @return a container for <code>GUITunableHandler</code>' panels with special features 
	 * if it is requested, or a simple one if not
	 */
	private JPanel createJPanel(final String title, final GUITunableHandler gh, 
			final Boolean vertical, final Boolean displayed) {
		if (gh == null)
			return createTitledPanel(title, vertical, displayed);
		
		// See if we need to create an XOR panel
		if (gh.controlsMutuallyExclusiveNestedChildren()) {
			return new XorPanel(title, gh);
		} else {
			// Figure out if the collapsable flag is set
			final String displayState = gh.getParams().getProperty("displayState");
			
			if (displayState != null) {
				final BasicCollapsiblePanel cp = new BasicCollapsiblePanel(title);
				
				if (displayState.equalsIgnoreCase("uncollapsed"))
					cp.setCollapsed(false);	
			
				cp.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						repackEnclosingDialog(cp);
					}
				});
				
				return cp;
			} else {
				// We're not collapsable, so return a normal jpanel
				return createTitledPanel(title, vertical, displayed);
			}
		}
	}
	
	/**
	 * Attempts to locate the instance of the enclosing JDialog.
	 * If successful we will call the pack() method on it.
	 */
	private void repackEnclosingDialog(final Component cp) {
		if (cp == null)
			return;
		
		Container container = cp.getParent();
		
		while (container != null && !(container instanceof JDialog))
			container = container.getParent();
		
		if (container != null)
			((JDialog)container).pack();
	}

	/**
	 * Creation of a JPanel that will contain panels of <code>GUITunableHandler</code>
	 * A layout will be set for this <i>"container"</i> of panels (horizontally or vertically), 
	 * and a title (displayed or not)
	 *
	 * @param title of the panel
	 * @param alignment the way the panels will be set in this <i>"container</i> panel
	 * @param groupTitle parameter to choose whether or not the title of the panel has to be displayed
	 *
	 * @return a container for <code>GUITunableHandler</code>' panels
	 */
	private TitledPanel createTitledPanel(final String title, final Boolean vertical, final Boolean displayed) {
		final TitledPanel panel = new TitledPanel(
				(displayed == null || displayed) ? title : null,
				vertical == Boolean.TRUE
		);

		return panel;
	}

	/**
	 * Check if the conditions set in validate method from <code>TunableValidator</code> are met
	 *
	 * If an exception is thrown, or something's wrong, it will be displayed to the user
	 *
	 * @return success(true) or failure(false) for the validation
	 */
	private boolean validateTunableInput(final Object objectWithTunables) {
		if (!(objectWithTunables instanceof TunableValidator))
			return true;

		final Appendable errMsg = new StringBuilder();
		try {
			final ValidationState validationState = ((TunableValidator)objectWithTunables).getValidationState(errMsg);
			if (validationState == ValidationState.INVALID) {
				JOptionPane.showMessageDialog(
						new JFrame(),
						errMsg.toString(),
				        "Input Validation Problem",
				        JOptionPane.ERROR_MESSAGE);
				return false;
			} else if (validationState == ValidationState.REQUEST_CONFIRMATION) {
				if (JOptionPane.showConfirmDialog(
						new JFrame(),
						errMsg.toString(),
						"Confirmation",
						JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	protected String getTitle(Object objectWithTunables) {
		Method method = titleProviderMap.get(objectWithTunables);
		
		if (method != null) {
			try {
				String title = (String) method.invoke(objectWithTunables);
				
				if (title != null)
					return title;
			} catch (final Exception e) {
				logger.error("Can't retrieve @ProvidesTitle String: ", e);
			}
		}
		
		return "Set Parameters";
	}

	/**
	 * Get information about the Groups and parameters to create the proper GUI.
	 */
	private Map<String, Boolean> processGroupParams(GUITunableHandler gh, String paramName, String defaultValue) {
		final Map<String, Boolean> groupMap = new HashMap<String, Boolean>();

		final String[] groups = gh.getGroups();
		// empty string splits to single element array containing string "" 
		final String[] params = gh.getParams().getProperty(paramName, "").split(","); 

		if (groups.length <= params.length) {
			for (int i = 0; i < groups.length; i++) {
				final boolean vertical = params[i].equalsIgnoreCase(defaultValue) || params[i].equals("");
				groupMap.put(groups[i], vertical);
			}
		} else {
			for (int i = 0; i < params.length; i++) {
				final boolean vertical = params[i].equalsIgnoreCase(defaultValue) || params[i].equals(""); 
				groupMap.put(groups[i], vertical);
			}

			// Default 
			for (int i = params.length; i < groups.length; i++) 
				groupMap.put(groups[i], true);
		}

		return groupMap;
	}
}
