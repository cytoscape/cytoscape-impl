



package csplugins.layout; 

import org.cytoscape.work.undo.UndoSupport;

import csplugins.layout.JGraphLayoutWrapper;

import org.cytoscape.view.layout.CyLayoutAlgorithm;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		UndoSupport undoSupportServiceRef = getService(bc,UndoSupport.class);
		
		JGraphLayoutWrapper jGraphAnnealingLayout = new JGraphLayoutWrapper(undoSupportServiceRef,JGraphLayoutWrapper.ANNEALING);
		JGraphLayoutWrapper jGraphMoenLayout = new JGraphLayoutWrapper(undoSupportServiceRef,JGraphLayoutWrapper.MOEN);
		JGraphLayoutWrapper jGraphCircleGraphLayout = new JGraphLayoutWrapper(undoSupportServiceRef,JGraphLayoutWrapper.CIRCLE_GRAPH);
		JGraphLayoutWrapper jGraphRadialTreeLayout = new JGraphLayoutWrapper(undoSupportServiceRef,JGraphLayoutWrapper.RADIAL_TREE);
		JGraphLayoutWrapper jGraphGEMLayout = new JGraphLayoutWrapper(undoSupportServiceRef,JGraphLayoutWrapper.GEM);
		JGraphLayoutWrapper jGraphSpringEmbeddedLayout = new JGraphLayoutWrapper(undoSupportServiceRef,JGraphLayoutWrapper.SPRING_EMBEDDED);
		JGraphLayoutWrapper jGraphSugiyamaLayout = new JGraphLayoutWrapper(undoSupportServiceRef,JGraphLayoutWrapper.SUGIYAMA);
		JGraphLayoutWrapper jGraphTreeLayout = new JGraphLayoutWrapper(undoSupportServiceRef,JGraphLayoutWrapper.TREE);
		
		
		Properties jGraphAnnealingLayoutProps = new Properties();
		jGraphAnnealingLayoutProps.setProperty("preferredMenu","JGraph Layouts");
		registerService(bc,jGraphAnnealingLayout,CyLayoutAlgorithm.class, jGraphAnnealingLayoutProps);

		Properties jGraphMoenLayoutProps = new Properties();
		jGraphMoenLayoutProps.setProperty("preferredMenu","JGraph Layouts");
		registerService(bc,jGraphMoenLayout,CyLayoutAlgorithm.class, jGraphMoenLayoutProps);

		Properties jGraphCircleGraphLayoutProps = new Properties();
		jGraphCircleGraphLayoutProps.setProperty("preferredMenu","JGraph Layouts");
		registerService(bc,jGraphCircleGraphLayout,CyLayoutAlgorithm.class, jGraphCircleGraphLayoutProps);

		Properties jGraphRadialTreeLayoutProps = new Properties();
		jGraphRadialTreeLayoutProps.setProperty("preferredMenu","JGraph Layouts");
		registerService(bc,jGraphRadialTreeLayout,CyLayoutAlgorithm.class, jGraphRadialTreeLayoutProps);

		Properties jGraphGEMLayoutProps = new Properties();
		jGraphGEMLayoutProps.setProperty("preferredMenu","JGraph Layouts");
		registerService(bc,jGraphGEMLayout,CyLayoutAlgorithm.class, jGraphGEMLayoutProps);

		Properties jGraphSpringEmbeddedLayoutProps = new Properties();
		jGraphSpringEmbeddedLayoutProps.setProperty("preferredMenu","JGraph Layouts");
		registerService(bc,jGraphSpringEmbeddedLayout,CyLayoutAlgorithm.class, jGraphSpringEmbeddedLayoutProps);

		Properties jGraphSugiyamaLayoutProps = new Properties();
		jGraphSugiyamaLayoutProps.setProperty("preferredMenu","JGraph Layouts");
		registerService(bc,jGraphSugiyamaLayout,CyLayoutAlgorithm.class, jGraphSugiyamaLayoutProps);

		Properties jGraphTreeLayoutProps = new Properties();
		jGraphTreeLayoutProps.setProperty("preferredMenu","JGraph Layouts");
		registerService(bc,jGraphTreeLayout,CyLayoutAlgorithm.class, jGraphTreeLayoutProps);

		

	}
}

