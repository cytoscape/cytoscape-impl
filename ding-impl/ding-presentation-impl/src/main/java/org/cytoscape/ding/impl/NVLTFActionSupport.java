
package org.cytoscape.ding.impl;

import java.awt.Component;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Properties;
import java.awt.event.ActionEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

/**
 * NetworkViewLocationTaskFactory Action Support.
 */
public class NVLTFActionSupport {
    private final CyApplicationManager appMgr;
    private final CyNetworkViewManager netViewMgr;    
    private final TaskManager tm;
    private final CyServiceRegistrar registrar;
    private static final String MENU_BAR_FILTER = "(" + ServiceProperties.IN_MENU_BAR + "=true)";


    public NVLTFActionSupport(final CyApplicationManager appMgr, final CyNetworkViewManager netViewMgr, 
                              final TaskManager tm, final CyServiceRegistrar registrar) {
        this.appMgr = appMgr;
        this.netViewMgr = netViewMgr;
        this.tm = tm;
        this.registrar = registrar;
    }

    public void registerAction(NetworkViewLocationTaskFactory nvltf,Map<String,String> props) {
        // If the user requests this action to be in the menu
        // bar, create and register a CyAction for it
        if (props.containsKey(ServiceProperties.IN_MENU_BAR) &&
            Boolean.valueOf(props.get(ServiceProperties.IN_MENU_BAR)) == Boolean.TRUE) {

            CyAction action = new NVLTFAction(nvltf,props);
            registrar.registerService(action,CyAction.class,new Properties());
        }
    }

    private class NVLTFAction extends AbstractCyAction {
        private static final long serialVersionUID = 6590168183571319473L;
        private final NetworkViewLocationTaskFactory nvltf;

        public NVLTFAction(NetworkViewLocationTaskFactory nvltf, Map<String,String> props) {
            super(props,appMgr,netViewMgr, createTaskFactory(nvltf));
            this.nvltf = nvltf;
        }

        public void actionPerformed(ActionEvent a) {
            CyNetworkView view = appMgr.getCurrentNetworkView();
            
            if ( !(view instanceof DGraphView) )
                return;
            DGraphView dgview = (DGraphView)view;

            // Get the canvas component.  We need to use the foreground canvas because its
            // on top and it's the only one that provides the mouse position
            Component foregroundCanvas = dgview.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS);

            // Now get the current mouse position
            Point2D javaPt = foregroundCanvas.getMousePosition();
	          if (javaPt == null) javaPt = new Point2D.Double(0.0,0.0);

            // Now transform the mouse position to our coordinate space
            double[] coords = new double[2];
            coords[0] = javaPt.getX();
            coords[1] = javaPt.getY();
            dgview.xformComponentToNodeCoords(coords);
            Point2D xformPt = new Point2D.Double(coords[0], coords[1]);

            tm.execute(nvltf.createTaskIterator(view,javaPt,xformPt));
        }
    }

	TaskFactory createTaskFactory(final NetworkViewLocationTaskFactory taskFactory) {
		return new TaskFactory() {
			@Override
			public boolean isReady() {
				return taskFactory.isReady(appMgr.getCurrentNetworkView(), null, null);
			}
			
			@Override
			public TaskIterator createTaskIterator() {
				return taskFactory.createTaskIterator(appMgr.getCurrentNetworkView(), null, null);
			}
		};
	}
}
