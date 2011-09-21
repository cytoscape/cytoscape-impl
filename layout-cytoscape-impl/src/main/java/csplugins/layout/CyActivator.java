



package csplugins.layout; 

import org.cytoscape.work.undo.UndoSupport;

import csplugins.layout.algorithms.bioLayout.BioLayoutFRAlgorithm;
import csplugins.layout.algorithms.StackedNodeLayout;
import csplugins.layout.algorithms.bioLayout.BioLayoutKKAlgorithm;
import csplugins.layout.algorithms.GroupAttributesLayout;
import csplugins.layout.algorithms.graphPartition.ISOMLayout;
import csplugins.layout.algorithms.circularLayout.CircularLayoutAlgorithm;
import csplugins.layout.algorithms.graphPartition.DegreeSortedCircleLayout;
import csplugins.layout.algorithms.hierarchicalLayout.HierarchicalLayoutAlgorithm;
import csplugins.layout.algorithms.graphPartition.AttributeCircleLayout;

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
		
		CircularLayoutAlgorithm circularLayoutAlgorithm = new CircularLayoutAlgorithm(undoSupportServiceRef);
		HierarchicalLayoutAlgorithm hierarchicalLayoutAlgorithm = new HierarchicalLayoutAlgorithm(undoSupportServiceRef);
		AttributeCircleLayout attributeCircleLayout = new AttributeCircleLayout(undoSupportServiceRef);
		DegreeSortedCircleLayout degreeSortedCircleLayout = new DegreeSortedCircleLayout(undoSupportServiceRef);
		ISOMLayout ISOMLayout = new ISOMLayout(undoSupportServiceRef);
		BioLayoutKKAlgorithm bioLayoutKKAlgorithmFALSE = new BioLayoutKKAlgorithm(undoSupportServiceRef,false);
		BioLayoutKKAlgorithm bioLayoutKKAlgorithmTRUE = new BioLayoutKKAlgorithm(undoSupportServiceRef,true);
		BioLayoutFRAlgorithm bioLayoutFRAlgorithm = new BioLayoutFRAlgorithm(undoSupportServiceRef,true);
		StackedNodeLayout stackedNodeLayout = new StackedNodeLayout(undoSupportServiceRef);
		GroupAttributesLayout groupAttributesLayout = new GroupAttributesLayout(undoSupportServiceRef);
		
		
		Properties circularLayoutAlgorithmProps = new Properties();
		circularLayoutAlgorithmProps.setProperty("preferredMenu","Cytoscape Layouts");
		registerService(bc,circularLayoutAlgorithm,CyLayoutAlgorithm.class, circularLayoutAlgorithmProps);

		Properties hierarchicalLayoutAlgorithmProps = new Properties();
		hierarchicalLayoutAlgorithmProps.setProperty("preferredMenu","Cytoscape Layouts");
		registerService(bc,hierarchicalLayoutAlgorithm,CyLayoutAlgorithm.class, hierarchicalLayoutAlgorithmProps);

		Properties attributeCircleLayoutProps = new Properties();
		attributeCircleLayoutProps.setProperty("preferredMenu","Cytoscape Layouts");
		registerService(bc,attributeCircleLayout,CyLayoutAlgorithm.class, attributeCircleLayoutProps);

		Properties degreeSortedCircleLayoutProps = new Properties();
		degreeSortedCircleLayoutProps.setProperty("preferredMenu","Cytoscape Layouts");
		registerService(bc,degreeSortedCircleLayout,CyLayoutAlgorithm.class, degreeSortedCircleLayoutProps);

		Properties ISOMLayoutProps = new Properties();
		ISOMLayoutProps.setProperty("preferredMenu","Cytoscape Layouts");
		registerService(bc,ISOMLayout,CyLayoutAlgorithm.class, ISOMLayoutProps);

		Properties bioLayoutKKAlgorithmFALSEProps = new Properties();
		bioLayoutKKAlgorithmFALSEProps.setProperty("preferredMenu","Cytoscape Layouts");
		registerService(bc,bioLayoutKKAlgorithmFALSE,CyLayoutAlgorithm.class, bioLayoutKKAlgorithmFALSEProps);

		Properties bioLayoutKKAlgorithmTRUEProps = new Properties();
		bioLayoutKKAlgorithmTRUEProps.setProperty("preferredMenu","Cytoscape Layouts");
		registerService(bc,bioLayoutKKAlgorithmTRUE,CyLayoutAlgorithm.class, bioLayoutKKAlgorithmTRUEProps);

		Properties bioLayoutFRAlgorithmProps = new Properties();
		bioLayoutFRAlgorithmProps.setProperty("preferredMenu","Cytoscape Layouts");
		registerService(bc,bioLayoutFRAlgorithm,CyLayoutAlgorithm.class, bioLayoutFRAlgorithmProps);

		Properties groupAttributesLayoutProps = new Properties();
		groupAttributesLayoutProps.setProperty("preferredMenu","Cytoscape Layouts");
		registerService(bc,groupAttributesLayout,CyLayoutAlgorithm.class, groupAttributesLayoutProps);
	}
}

