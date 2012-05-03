
package org.cytoscape.ding.impl;

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
import org.cytoscape.work.TaskManager;

/**
 * NetworkViewLocationTaskFactory Action Support.
 */
public class NVLTFActionSupport {
    private final CyApplicationManager appMgr;
    private final CyNetworkViewManager netViewMgr;    
    private final TaskManager tm;
    private final CyServiceRegistrar registrar;

    public NVLTFActionSupport(final CyApplicationManager appMgr, final CyNetworkViewManager netViewMgr, final TaskManager
 tm, final CyServiceRegistrar registrar) {
        this.appMgr = appMgr;
        this.netViewMgr = netViewMgr;
        this.tm = tm;
        this.registrar = registrar;
    }

    public void registerAction(NetworkViewLocationTaskFactory nvltf,Map<String,String> props) {
    	// TODO: evaluate the properties to determine if this is something we actually want to make an action out of?
        CyAction action = new NVLTFAction(nvltf,props);
        registrar.registerService(action,CyAction.class,new Properties());
    }

    private class NVLTFAction extends AbstractCyAction {
		private static final long serialVersionUID = 6590168183571319473L;
		private final NetworkViewLocationTaskFactory nvltf;

        public NVLTFAction(NetworkViewLocationTaskFactory nvltf, Map<String,String> props) {
            super(props,appMgr,netViewMgr);
            this.nvltf = nvltf;
        }

        public void actionPerformed(ActionEvent a) {
            CyNetworkView view = appMgr.getCurrentNetworkView();
            
            if ( !(view instanceof DGraphView) )
                return;
            DGraphView dgview = (DGraphView)view;

            // SCOOTER START HERE
            Point2D javaPt = new Point2D.Double(0.0,0.0);
            Point2D xformPt = new Point2D.Double(0.0,0.0);
            // SCOOTER END HERE

            tm.execute(nvltf.createTaskIterator(view,javaPt,xformPt));
        }
    }
}
