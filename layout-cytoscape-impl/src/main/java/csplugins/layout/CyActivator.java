



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
		circularLayoutAlgorithmProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		circularLayoutAlgorithmProps.setProperty("preferredTaskManager","menu");
		circularLayoutAlgorithmProps.setProperty("title",circularLayoutAlgorithm.toString());
		registerService(bc,circularLayoutAlgorithm,CyLayoutAlgorithm.class, circularLayoutAlgorithmProps);

		Properties hierarchicalLayoutAlgorithmProps = new Properties();
		hierarchicalLayoutAlgorithmProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		hierarchicalLayoutAlgorithmProps.setProperty("preferredTaskManager","menu");
		hierarchicalLayoutAlgorithmProps.setProperty("title",hierarchicalLayoutAlgorithm.toString());
		registerService(bc,hierarchicalLayoutAlgorithm,CyLayoutAlgorithm.class, hierarchicalLayoutAlgorithmProps);

		Properties attributeCircleLayoutProps = new Properties();
		attributeCircleLayoutProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		attributeCircleLayoutProps.setProperty("preferredTaskManager","menu");
		attributeCircleLayoutProps.setProperty("title",attributeCircleLayout.toString());
		registerService(bc,attributeCircleLayout,CyLayoutAlgorithm.class, attributeCircleLayoutProps);

		Properties degreeSortedCircleLayoutProps = new Properties();
		degreeSortedCircleLayoutProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		degreeSortedCircleLayoutProps.setProperty("preferredTaskManager","menu");
		degreeSortedCircleLayoutProps.setProperty("title",degreeSortedCircleLayout.toString());
		registerService(bc,degreeSortedCircleLayout,CyLayoutAlgorithm.class, degreeSortedCircleLayoutProps);

		Properties ISOMLayoutProps = new Properties();
		ISOMLayoutProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		ISOMLayoutProps.setProperty("preferredTaskManager","menu");
		ISOMLayoutProps.setProperty("title",ISOMLayout.toString());
		registerService(bc,ISOMLayout,CyLayoutAlgorithm.class, ISOMLayoutProps);

		Properties bioLayoutKKAlgorithmFALSEProps = new Properties();
		bioLayoutKKAlgorithmFALSEProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		bioLayoutKKAlgorithmFALSEProps.setProperty("preferredTaskManager","menu");
		bioLayoutKKAlgorithmFALSEProps.setProperty("title",bioLayoutKKAlgorithmFALSE.toString());
		registerService(bc,bioLayoutKKAlgorithmFALSE,CyLayoutAlgorithm.class, bioLayoutKKAlgorithmFALSEProps);

		Properties bioLayoutKKAlgorithmTRUEProps = new Properties();
		bioLayoutKKAlgorithmTRUEProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		bioLayoutKKAlgorithmTRUEProps.setProperty("preferredTaskManager","menu");
		bioLayoutKKAlgorithmTRUEProps.setProperty("title",bioLayoutKKAlgorithmTRUE.toString());
		registerService(bc,bioLayoutKKAlgorithmTRUE,CyLayoutAlgorithm.class, bioLayoutKKAlgorithmTRUEProps);

		Properties bioLayoutFRAlgorithmProps = new Properties();
		bioLayoutFRAlgorithmProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		bioLayoutFRAlgorithmProps.setProperty("preferredTaskManager","menu");
		bioLayoutFRAlgorithmProps.setProperty("title",bioLayoutFRAlgorithm.toString());
		registerService(bc,bioLayoutFRAlgorithm,CyLayoutAlgorithm.class, bioLayoutFRAlgorithmProps);

		Properties groupAttributesLayoutProps = new Properties();
		groupAttributesLayoutProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		groupAttributesLayoutProps.setProperty("preferredTaskManager","menu");
		groupAttributesLayoutProps.setProperty("title",groupAttributesLayout.toString());
		registerService(bc,groupAttributesLayout,CyLayoutAlgorithm.class, groupAttributesLayoutProps);
	}
}

