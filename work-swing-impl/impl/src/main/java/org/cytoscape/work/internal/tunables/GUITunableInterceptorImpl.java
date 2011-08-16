package org.cytoscape.work.internal.tunables;


import java.awt.Color;
import java.awt.Dialog;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.cytoscape.di.util.DIUtil;
import org.cytoscape.work.AbstractTunableInterceptor;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.TunableValidator.ValidationState;
import org.cytoscape.work.internal.tunables.utils.CollapsablePanel;
import org.cytoscape.work.internal.tunables.utils.XorPanel;
import org.cytoscape.work.swing.GUITunableHandler;
import org.cytoscape.work.swing.GUITunableInterceptor;
import org.cytoscape.work.swing.TunableDialog;
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
public class GUITunableInterceptorImpl extends AbstractTunableInterceptor<GUITunableHandler> implements GUITunableInterceptor<GUITunableHandler> {
	private Map<List<GUITunableHandler>, JPanel> panelMap;
	private boolean newValuesSet;
	
	/** A reference to the parent <code>JPanel</code>, if any. */
	private JPanel tunablePanel = null;

	/** The list of handlers associated with this interceptor. */
	private List<GUITunableHandler> handlers;

	/** The actual/original objects whose tunables this interceptor manages. */
	private Object[] objectsWithTunables;

	/** Provides an initialised logger. */
	private Logger logger = LoggerFactory.getLogger(GUITunableInterceptorImpl.class);

	private Window parent = null;
	
	private TunableDialog tunnableDialog = null;


	/**
	 * Constructor.
	 */
	public GUITunableInterceptorImpl() {
		super();
		panelMap = new HashMap<List<GUITunableHandler>, JPanel>();
	}
	
	/** {@inheritDoc} */
	public void setParent(Window win){
		parent = win;
	}
	
	/** {@inheritDoc} */
	public void setTunablePanel(final JPanel tPanel){
		this.tunablePanel = tPanel;
	}
	
	/** {@inheritDoc} */
	final public JPanel getUI(final Object... proxyObjs) {
		this.objectsWithTunables = DIUtil.stripProxies(proxyObjs);
		handlers = new ArrayList<GUITunableHandler>();
		return constructUI();
	}

	/**
	 * Creates a GUI for the detected <code>Tunables</code>, following the graphic rules specified in <code>Tunable</code>s annotations
	 * or uses the JPanel provided by the method annotated with <code>@ProvidesGUI</code>
	 *
	 * The new values that have been entered for the Object contained in <code>GUITunableHandlers</code> are also set if the user clicks on <i>"OK"</i>
	 *
	 * @param proxyObjs an array of objects with <code>Tunables</code>s
	 *
	 * @return if new values have been successfully set
	 */
	final public boolean execUI(Object... proxyObjs) {
		final JPanel panel = getUI(proxyObjs);
		if (panel == null)
			return true;

		return displayGUI(panel, proxyObjs);
	}

	/** {@inheritDoc} */
	final public boolean hasTunables(final Object o) {
		return super.hasTunables(DIUtil.stripProxy(o));
	}

        /**                                                                                                                          
         * This method calls {@link AbstractTunableInterceptor.loadTunables} with the                                                
         * unwrapped object instead of the Spring proxy object, which is provided as                                                 
         * an argument.                                                                                                              
         * @param obj The Spring proxy object from which we'd like the raw object.                                                   
         */
        final public void loadTunables(final Object obj) {
		super.loadTunables(DIUtil.stripProxy(obj));
        }
	

	/** This implements the final action in execUI() and executes the UI.
	 *  @param optionPanel  the panel containing the various UI elements corresponding to individual tunables
	 *  @param proxyObjs    represents the objects annotated with tunables
	 */
	private boolean displayGUI(final JPanel optionPanel, Object... proxyObjs) {
		tunnableDialog = new TunableDialog(parent);
		tunnableDialog.setLocationRelativeTo(parent);
		tunnableDialog.setTitle("Set Parameters");
		tunnableDialog.setModal(true);
		tunnableDialog.setAlwaysOnTop(true);

		tunnableDialog.addComponent(optionPanel);
		tunnableDialog.setVisible(true);
		
		String userInput = tunnableDialog.getUserInput();
		
		if (userInput.equalsIgnoreCase("OK")){
			return validateAndWriteBackTunables(proxyObjs);			
		}
		else { // CANCEL
			return false;			
		}
	}

	private JPanel constructUI() {
		JPanel providedGUI = null;
		int factoryCount = 0; // # of descendents of TaskFactory...
		int otherCount = 0;   // ...everything else.  (Presumeably descendents of Task.)
		if (handlers == null)
			handlers = new ArrayList<GUITunableHandler>();
		for (final Object objectWithTunables : objectsWithTunables) {
			if (objectWithTunables instanceof TaskFactory)
				++factoryCount;
			else
				++otherCount;

			if (guiProviderMap.containsKey(objectWithTunables)) {
				if (providedGUI != null)
					throw new IllegalStateException("Found more than one provided GUI!");
				try {
					providedGUI = (JPanel)guiProviderMap.get(objectWithTunables).invoke(objectWithTunables);
				} catch (final Exception e) {
					logger.error("Can't retrieve @ProvidesGUI JPanel: " + e);
					return null;
				}
			} else if (handlerMap.containsKey(objectWithTunables))
				handlers.addAll(handlerMap.get(objectWithTunables).values());
			else
				// TODO For task tunableHandlerFactories throwing an exception is wrong because
				// their tunables simply haven't been loaded yet. 
				//throw new IllegalArgumentException("No Tunables and no provided GUI exists for Object yet!");
				logger.warn("No Tunables and no provided GUI exists for Object yet!");
		}

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
			//if no parentPanel is defined, then create a new JDialog to display the Tunables' panels
			if (tunablePanel == null)
				return providedGUI;
			else { // add them to the "parentPanel" JPanel
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

				// Get information about the Groups and alignment from Tunables Annotations in order to create the proper GUI
				final Map<String, Boolean> groupToVerticalMap = new HashMap<String, Boolean>();
				final Map<String, Boolean> groupToDisplayedMap = new HashMap<String, Boolean>();

				final String[] group = gh.getGroups();
				final String[] alignments = gh.getParams().getProperty("alignments", "").split(",");
				final String[] groupTitles = gh.getParams().getProperty("groupTitles", "").split(",");

				if (group.length <= alignments.length) {
					for (int i = 0; i < group.length; i++) {
						final boolean vertical = groupTitles[i].equalsIgnoreCase("vertical");
						groupToVerticalMap.put(group[i], vertical);
					}
				} else {
					for (int i = 0; i < alignments.length; i++) {
						final boolean vertical = groupTitles[i].equalsIgnoreCase("vertical");
						groupToVerticalMap.put(group[i], vertical);
					}

					// Default alignment is "vertical."
					for (int i = alignments.length; i < group.length; i++)
						groupToVerticalMap.put(group[i], true);
				}

				if (group.length <= groupTitles.length) {
					for (int i = 0; i < group.length; i++) {
						final boolean displayed =
							groupTitles[i].equalsIgnoreCase("displayed");
						groupToDisplayedMap.put(group[i], displayed);
					}
				} else {
					for (int i = 0; i < groupTitles.length; i++) {
						final boolean displayed =
							groupTitles[i].equalsIgnoreCase("displayed");
						groupToDisplayedMap.put(group[i], displayed);
					}

					// Default group setting is "displayed."
					for (int i = groupTitles.length; i < group.length; i++)
						groupToDisplayedMap.put(group[i], true);
				}

				// find the proper group to put the handler panel in given the Alignment/Group parameters
				String lastGroup = MAIN;
				String groupNames = null;
				for (String g : group) {
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

		//if no parentPanel is defined, then create a new JDialog to display the Tunables' panels
		if (tunablePanel == null)
			return panelMap.get(handlers);
		else { // add them to the "parentPanel" JPanel
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
		}
		else {
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
	private boolean validateTunableInput() {
		for (final Object objectWithTunables : objectsWithTunables) {
			if (!(objectWithTunables instanceof TunableValidator))
				continue;

			final Appendable errMsg = new StringBuilder();
			try {
				final ValidationState validationState =
					((TunableValidator)objectWithTunables).getValidationState(errMsg);
				if (validationState == ValidationState.INVALID) {
					JOptionPane.showMessageDialog(new JFrame(), errMsg.toString(),
					                              "Input Validation Problem",
					                              JOptionPane.ERROR_MESSAGE);
					if (tunablePanel == null)
						displayGUI(panelMap.get(handlers));
					return false;
				} else if (validationState == ValidationState.REQUEST_CONFIRMATION) {
					if (JOptionPane.showConfirmDialog(new JFrame(), errMsg.toString(),
									  "Confirmation",
									  JOptionPane.YES_NO_OPTION)
					    == JOptionPane.NO_OPTION)
					{
						if (tunablePanel == null)
							displayGUI(panelMap.get(handlers));
						return false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		tunablePanel = null;
		newValuesSet = true;
		return true;
	}

	//get the value(Handle) of the Tunable if its JPanel is enabled(Dependency) and check if we have to validate the values of tunables                      
	/**
	 * get the <i>value,item,string,state...</i> from the GUI component, and check with the dependencies, if it can be set as the new one 
	 *
	 * <p><pre>
	 * If the <code>TunableValidator</code> interface is implemented by the class that contains the <code>Tunables</code> :
	 * <ul>
	 * <li>a validate method has to be applied</li>
	 * <li>it checks the conditions that have been declared about the chosen <code>Tunable(s)</code> </li>
	 * <li>if validation fails, it displays an error to the user, and new values are not set</li>
	 * </ul>
	 * </pre></p>
	 * @return success or not of the <code>TunableValidator</code> validate method
	 */
	final public boolean validateAndWriteBackTunables(Object... proxyObjs) {
		final Object objectsWithTunables[] = DIUtil.stripProxies(proxyObjs);

		// Update handler list:
		if (handlers == null)
			handlers = new ArrayList<GUITunableHandler>();
		else
			handlers.clear();
		JPanel providedGUI = null;
		for (final Object objectWithTunables : objectsWithTunables) {
			if (guiProviderMap.containsKey(objectWithTunables)) {
				if (providedGUI != null)
					throw new IllegalStateException("Found more than one provided GUI!");
				try {
					providedGUI = (JPanel)guiProviderMap.get(objectWithTunables).invoke(objectWithTunables);
				} catch (final Exception e) {
					logger.error("Can't retrieve @ProvidesGUI JPanel: " + e);
					return false;
				}
			} else if (handlerMap.containsKey(objectWithTunables))
				handlers.addAll(handlerMap.get(objectWithTunables).values());
			else
				// TODO For task tunableHandlerFactories throwing an exception is wrong because
				// their tunables simply haven't been loaded yet. 
				//throw new IllegalArgumentException("No Tunables and no provided GUI exists for Object yet!");
				logger.warn("No Tunables and no provided GUI exists for Object yet!");
		}

		for (final GUITunableHandler h : handlers)
			h.handleDependents();

		return validateTunableInput();
	}
	

}
