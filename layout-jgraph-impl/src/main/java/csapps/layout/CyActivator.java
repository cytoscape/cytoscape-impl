



package csapps.layout; 

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

import static org.cytoscape.work.ServiceProperties.*;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {
		
		UndoSupport undo = getService(bc,UndoSupport.class);

		JGraphLayoutWrapper jGraphAnnealingLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.ANNEALING,undo);
		JGraphLayoutWrapper jGraphMoenLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.MOEN,undo);
		JGraphLayoutWrapper jGraphCircleGraphLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.CIRCLE_GRAPH,undo);
		JGraphLayoutWrapper jGraphRadialTreeLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.RADIAL_TREE,undo);
		JGraphLayoutWrapper jGraphGEMLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.GEM,undo);
		JGraphLayoutWrapper jGraphSpringEmbeddedLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.SPRING_EMBEDDED,undo);
		JGraphLayoutWrapper jGraphSugiyamaLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.SUGIYAMA,undo);
		JGraphLayoutWrapper jGraphTreeLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.TREE,undo);
		
		
		Properties jGraphAnnealingLayoutProps = new Properties();
		jGraphAnnealingLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphAnnealingLayout,CyLayoutAlgorithm.class, jGraphAnnealingLayoutProps);

		Properties jGraphMoenLayoutProps = new Properties();
		jGraphMoenLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphMoenLayout,CyLayoutAlgorithm.class, jGraphMoenLayoutProps);

		Properties jGraphCircleGraphLayoutProps = new Properties();
		jGraphCircleGraphLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphCircleGraphLayout,CyLayoutAlgorithm.class, jGraphCircleGraphLayoutProps);

		Properties jGraphRadialTreeLayoutProps = new Properties();
		jGraphRadialTreeLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphRadialTreeLayout,CyLayoutAlgorithm.class, jGraphRadialTreeLayoutProps);

		Properties jGraphGEMLayoutProps = new Properties();
		jGraphGEMLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphGEMLayout,CyLayoutAlgorithm.class, jGraphGEMLayoutProps);

		Properties jGraphSpringEmbeddedLayoutProps = new Properties();
		jGraphSpringEmbeddedLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphSpringEmbeddedLayout,CyLayoutAlgorithm.class, jGraphSpringEmbeddedLayoutProps);

		Properties jGraphSugiyamaLayoutProps = new Properties();
		jGraphSugiyamaLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphSugiyamaLayout,CyLayoutAlgorithm.class, jGraphSugiyamaLayoutProps);

		Properties jGraphTreeLayoutProps = new Properties();
		jGraphTreeLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphTreeLayout,CyLayoutAlgorithm.class, jGraphTreeLayoutProps);

		

	}
}

