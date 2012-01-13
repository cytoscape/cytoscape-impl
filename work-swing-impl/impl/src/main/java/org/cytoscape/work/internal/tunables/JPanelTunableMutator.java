package org.cytoscape.work.internal.tunables;


import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.cytoscape.work.AbstractTunableInterceptor;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TunableMutator;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.TunableValidator.ValidationState;
import org.cytoscape.work.internal.tunables.utils.CollapsablePanel;
import org.cytoscape.work.internal.tunables.utils.XorPanel;
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
public class JPanelTunableMutator extends AbstractTunableInterceptor<GUITunableHandler> implements TunableMutator<GUITunableHandler,JPanel> {
	private Map<List<GUITunableHandler>, JPanel> panelMap;
	
	/** A reference to the parent <code>JPanel</code>, if any. */
	private JPanel tunablePanel = null;
	private JPanel providedGUI = null;

	/** Provides an initialised logger. */
	private final Logger logger = LoggerFactory.getLogger(JPanelTunableMutator.class);

	/**
	 * Constructor.
	 */
	public JPanelTunableMutator() {
		super();
		panelMap = new HashMap<List<GUITunableHandler>, JPanel>();
	}
	
	/** {@inheritDoc} */
	public void setConfigurationContext(final Object tPanel){
		if ( tPanel instanceof JPanel )
			this.tunablePanel = (JPanel)tPanel;
		else
			throw new IllegalArgumentException("this tunable mutator only works with JPanels - set the configuration context to a JPanel");
	}
	
	/** {@inheritDoc} */
	public boolean hasTunables(final Object o) {
		return super.hasTunables(o);
	}

	/** {@inheritDoc} */
	public boolean validateAndWriteBack(Object objectWithTunables) {

		List<GUITunableHandler> handlers = findHandlers(objectWithTunables); 

		for (final GUITunableHandler h : handlers)
			h.handleDependents();

		return validateTunableInput(objectWithTunables); 
	}

	/** {@inheritDoc} */
	public JPanel buildConfiguration(final Object objectWithTunables) {

		int factoryCount = 0; // # of descendents of TaskFactory...
		int otherCount = 0;   // ...everything else.  (Presumeably descendents of Task.)
		if (objectWithTunables instanceof TaskFactory)
			++factoryCount;
		else
			++otherCount;

		List<GUITunableHandler> handlers = findHandlers(objectWithTunables); 

		// Sanity check:
		if (factoryCount > 0) {
			if (factoryCount != 1) {
				logger.error("More than one annotated TaskFactory found!");
				return null;
			} else if (otherCount != 0) {
				logger.error("Found annotated Task objects in addition to an annotated TaskFactory!");
				return null;
			}
		}

		if (providedGUI != null) {
			//if no tunablePanel is defined, then create a new JDialog to display the Tunables' panels
			if (tunablePanel == null)
				return providedGUI;
			else { // add them to the "tunablePanel" JPanel
				tunablePanel.removeAll();
				tunablePanel.add(providedGUI);
				final JPanel retVal = tunablePanel;
				tunablePanel = null;
				return retVal;
			}
		} 

		if (handlers.isEmpty()) {
			if (tunablePanel != null) {
				tunablePanel.removeAll();
				return tunablePanel;
			}
			return null; 
		}

		if (!panelMap.containsKey(handlers)) {
			final String MAIN = " ";
			Map<String, JPanel> panels = new HashMap<String, JPanel>();
			final JPanel topLevel = createSimplePanel(MAIN, null, /* displayed = */ false);
			panels.put(MAIN, topLevel);

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

				// Get information about the Groups and alignment from Tunables Annotations 
				// in order to create the proper GUI
				final Map<String, Boolean> groupToVerticalMap = processGroupParams(gh,"alignments","vertical"); 
				final Map<String, Boolean> groupToDisplayedMap = processGroupParams(gh,"groupTitles","displayed"); 

				// find the proper group to put the handler panel in given the Alignment/Group parameters
				String lastGroup = MAIN;
				String groupNames = "";
				for (String g : gh.getGroups()) {
					if (g.equals(""))
						throw new IllegalArgumentException("A group's name must not be \"\"!");
					groupNames = groupNames + g;
					if (!panels.containsKey(groupNames)) {
						panels.put(groupNames,
						           createJPanel(g, gh, groupToVerticalMap.get(g),
						                        groupToDisplayedMap.get(g)));
						panels.get(lastGroup).add(panels.get(groupNames), gh.getChildKey());
					}
					lastGroup = groupNames;
				}
				panels.get(lastGroup).add(gh.getJPanel());
			}
			panelMap.put(handlers, panels.get(MAIN));
		}

		// Get the GUI into the proper state
		for (GUITunableHandler h : handlers)
			h.notifyDependents();

		//if no tunablePane is defined, then create a new JDialog to display the Tunables' panels
		if (tunablePanel == null)
			return panelMap.get(handlers);
		else { // add them to the "tunablePanel" JPanel
			tunablePanel.removeAll();
			tunablePanel.add(panelMap.get(handlers));
			final JPanel retVal = tunablePanel;
			tunablePanel = null;
			return retVal;
		}
	}

	/**
	 * Creation of a JPanel that will contain panels of <code>GUITunableHandler</code>
	 * This panel will have special features like, ability to collapse, ability to be displayed depending on another panel
	 * A layout will be set for this <i>"container"</i> of panels (horizontally or vertically), and a title (displayed or not)
	 *
	 *
	 * @param title of the panel
	 * @param gh provides access to <code>Tunable</code> annotations : see if this panel can be collapsable, and if its content will switch between different <code>GUITunableHandler</code> panels(depending on <code>xorKey</code>)
	 * @param alignment the way the panels will be set in this <i>"container</i> panel
	 * @param groupTitle parameter to choose whether or not the title of the panel has to be displayed
	 *
	 * @return a container for <code>GUITunableHandler</code>' panels with special features if it is requested, or a simple one if not
	 */
	private JPanel createJPanel(final String title, final GUITunableHandler gh, final Boolean vertical, final Boolean displayed) {
		if (gh == null)
			return createSimplePanel(title, vertical, displayed);

		// See if we need to create an XOR panel
		if (gh.controlsMutuallyExclusiveNestedChildren()) {
			JPanel p = new XorPanel(title, gh);
			return p;
		} else {
			// Figure out if the collapsable flag is set
			final String displayState = gh.getParams().getProperty("displayState");
			if (displayState != null) {
				if (displayState.equalsIgnoreCase("collapsed"))
					return new CollapsablePanel(title, false);
				else if (displayState.equalsIgnoreCase("uncollapsed"))
					return new CollapsablePanel(title, true);
				else
					System.err.println("*** in GUITunableInterceptorImpl: invalid \"displayState\": \""
					                   + displayState + "\"!");
			}

			// We're not collapsable, so return a normal jpanel
			return createSimplePanel(title, vertical, displayed);
		}
	}

	/**
	 * Creation of a JPanel that will contain panels of <code>GUITunableHandler</code>
	 * A layout will be set for this <i>"container"</i> of panels (horizontally or vertically), and a title (displayed or not)
	 *
	 * @param title of the panel
	 * @param alignment the way the panels will be set in this <i>"container</i> panel
	 * @param groupTitle parameter to choose whether or not the title of the panel has to be displayed
	 *
	 * @return a container for <code>GUITunableHandler</code>' panels
	 */
	private JPanel createSimplePanel(final String title, final Boolean vertical, final Boolean displayed) {
		JPanel outPanel = new JPanel();
		TitledBorder titleborder = BorderFactory.createTitledBorder(title);
		titleborder.setTitleColor(Color.BLUE);

		if (displayed == null || displayed)
			outPanel.setBorder(titleborder);
		if (vertical == null || vertical)
			outPanel.setLayout(new BoxLayout(outPanel, BoxLayout.PAGE_AXIS));
		else if (vertical != null)
			outPanel.setLayout(new BoxLayout(outPanel, BoxLayout.LINE_AXIS));
		return outPanel;
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
				JOptionPane.showMessageDialog(new JFrame(), errMsg.toString(),
				                              "Input Validation Problem",
				                              JOptionPane.ERROR_MESSAGE);
				return false;
			} else if (validationState == ValidationState.REQUEST_CONFIRMATION) {
				if (JOptionPane.showConfirmDialog(new JFrame(), errMsg.toString(),
								  "Confirmation",
								  JOptionPane.YES_NO_OPTION)
				    == JOptionPane.NO_OPTION)
				{
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private List<GUITunableHandler> findHandlers(Object objectWithTunables) {
		List<GUITunableHandler> handlers = new ArrayList<GUITunableHandler>();

		// clear any previously set providedGUI panels
		providedGUI = null;

		// the call to getHandlers will populate guiProviderMap
		// note that map may be empty!
		Map<String, GUITunableHandler> tunableHandlerMap = getHandlers(objectWithTunables);

		if (guiProviderMap.containsKey(objectWithTunables)) {
			try {
				providedGUI = (JPanel)guiProviderMap.get(objectWithTunables).invoke(objectWithTunables);
			} catch (final Exception e) {
				logger.error("Can't retrieve @ProvidesGUI JPanel: ", e);
			}
		} else {
			if ( tunableHandlerMap != null && !tunableHandlerMap.isEmpty() )
				handlers.addAll(tunableHandlerMap.values());
		}

		return handlers;
	}
	
	protected String getTitle(Object objectWithTunables) {
		Method method = titleProviderMap.get(objectWithTunables);
		if (method != null) {
			try {
				String title = (String) method.invoke(objectWithTunables);
				if (title != null) {
					return title;
				}
			} catch (final Exception e) {
				logger.error("Can't retrieve @ProvidesTitle String: ", e);
			}
		}
		return "Set Parameters";
	}

	// Get information about the Groups and parameters to create the proper GUI
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
