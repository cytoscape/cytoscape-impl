package org.cytoscape.ding.action;


import java.awt.event.ActionEvent;
import java.util.Properties;
import javax.swing.Action;
import javax.swing.event.MenuEvent;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DingGraphLODAll;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.TaskManager;
import org.cytoscape.ding.impl.SwitchGraphicsDetailTaskFactory;

public class GraphicsDetailAction extends AbstractCyAction {

	private final static long serialVersionUID = 1202323129387651L;
	
	private static String  GraphicsDetails = "Graphics Details"; 
		
	protected static String SHOW = "Show";
	protected static String HIDE = "Hide";

	private final CyProperty<Properties> defaultProps;
    private final CyApplicationManager applicationManager;
    private final TaskManager taskManagerServiceRef;


	public GraphicsDetailAction(final CyApplicationManager applicationManager, final TaskManager taskManagerServiceRef, 
			final CyProperty<Properties> defaultProps)
	{
		super(SHOW + " " + GraphicsDetails, applicationManager,"networkAndView");

		setPreferredMenu("View");
		setMenuGravity(5.0f);
		
		this.applicationManager = applicationManager;
		this.defaultProps = defaultProps;
		this.taskManagerServiceRef = taskManagerServiceRef;
	}

	/**
	 * Toggles the Show/Hide state.  
	 *
	 * @param ev Triggering event - not used. 
	 */
	public void actionPerformed(ActionEvent ev) {
		
		SwitchGraphicsDetailTaskFactory tf = new SwitchGraphicsDetailTaskFactory(applicationManager, defaultProps);
		taskManagerServiceRef.execute(tf.createTaskIterator());		
	} 

	/**
	 * This dynamically sets the title of the menu based on the state of the graphics detail.
	 */
	public void menuSelected(MenuEvent me) {
		
		if (isDetailShown()) {
			putValue(Action.NAME, HIDE + " " + GraphicsDetails);
		} else {
			putValue(Action.NAME, SHOW + " " + GraphicsDetails);
		}
	}

	public boolean isDetailShown(){
		
		final RenderingEngine<CyNetwork> engine = applicationManager.getCurrentRenderingEngine();

		if(engine instanceof DGraphView == false)
			return false;

		final GraphLOD lod = ((DGraphView) engine).getGraphLOD();

		if (lod instanceof DingGraphLODAll)
			return true; 
		else
			return false;
	}
}
