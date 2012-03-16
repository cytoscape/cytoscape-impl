



package csapps.layout; 

import org.cytoscape.work.undo.UndoSupport;

import csapps.layout.algorithms.bioLayout.BioLayoutFRAlgorithm;
import csapps.layout.algorithms.bioLayout.BioLayoutKKAlgorithm;
import csapps.layout.algorithms.circularLayout.CircularLayoutAlgorithm;
import csapps.layout.algorithms.graphPartition.AttributeCircleLayout;
import csapps.layout.algorithms.graphPartition.DegreeSortedCircleLayout;
import csapps.layout.algorithms.graphPartition.ISOMLayout;
import csapps.layout.algorithms.hierarchicalLayout.HierarchicalLayoutAlgorithm;
import csapps.layout.algorithms.GroupAttributesLayout;
import csapps.layout.algorithms.StackedNodeLayout;

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
		circularLayoutAlgorithmProps.setProperty("menuGravity","10.2");
		
		registerService(bc,circularLayoutAlgorithm,CyLayoutAlgorithm.class, circularLayoutAlgorithmProps);
		
		Properties hierarchicalLayoutAlgorithmProps = new Properties();
		hierarchicalLayoutAlgorithmProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		hierarchicalLayoutAlgorithmProps.setProperty("preferredTaskManager","menu");
		hierarchicalLayoutAlgorithmProps.setProperty("title",hierarchicalLayoutAlgorithm.toString());
		hierarchicalLayoutAlgorithmProps.setProperty("menuGravity","10.8");
		registerService(bc,hierarchicalLayoutAlgorithm,CyLayoutAlgorithm.class, hierarchicalLayoutAlgorithmProps);

		Properties attributeCircleLayoutProps = new Properties();
		attributeCircleLayoutProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		attributeCircleLayoutProps.setProperty("preferredTaskManager","menu");
		attributeCircleLayoutProps.setProperty("title",attributeCircleLayout.toString());
		attributeCircleLayoutProps.setProperty("menuGravity","10.1");
		registerService(bc,attributeCircleLayout,CyLayoutAlgorithm.class, attributeCircleLayoutProps);

		Properties degreeSortedCircleLayoutProps = new Properties();
		degreeSortedCircleLayoutProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		degreeSortedCircleLayoutProps.setProperty("preferredTaskManager","menu");
		degreeSortedCircleLayoutProps.setProperty("title",degreeSortedCircleLayout.toString());
		degreeSortedCircleLayoutProps.setProperty("menuGravity","10.3");
		registerService(bc,degreeSortedCircleLayout,CyLayoutAlgorithm.class, degreeSortedCircleLayoutProps);

		Properties ISOMLayoutProps = new Properties();
		ISOMLayoutProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		ISOMLayoutProps.setProperty("preferredTaskManager","menu");
		ISOMLayoutProps.setProperty("title",ISOMLayout.toString());
		ISOMLayoutProps.setProperty("menuGravity","10.9");
		registerService(bc,ISOMLayout,CyLayoutAlgorithm.class, ISOMLayoutProps);

		Properties bioLayoutKKAlgorithmFALSEProps = new Properties();
		bioLayoutKKAlgorithmFALSEProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		bioLayoutKKAlgorithmFALSEProps.setProperty("preferredTaskManager","menu");
		bioLayoutKKAlgorithmFALSEProps.setProperty("title",bioLayoutKKAlgorithmFALSE.toString());
		bioLayoutKKAlgorithmFALSEProps.setProperty("menuGravity","10.99");
		registerService(bc,bioLayoutKKAlgorithmFALSE,CyLayoutAlgorithm.class, bioLayoutKKAlgorithmFALSEProps);
		
		Properties bioLayoutKKAlgorithmTRUEProps = new Properties();
		bioLayoutKKAlgorithmTRUEProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		bioLayoutKKAlgorithmTRUEProps.setProperty("preferredTaskManager","menu");
		bioLayoutKKAlgorithmTRUEProps.setProperty("title",bioLayoutKKAlgorithmTRUE.toString());
		bioLayoutKKAlgorithmTRUEProps.setProperty("menuGravity","10.5");
		registerService(bc,bioLayoutKKAlgorithmTRUE,CyLayoutAlgorithm.class, bioLayoutKKAlgorithmTRUEProps);
		
		Properties bioLayoutFRAlgorithmProps = new Properties();
		bioLayoutFRAlgorithmProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		bioLayoutFRAlgorithmProps.setProperty("preferredTaskManager","menu");
		bioLayoutFRAlgorithmProps.setProperty("title",bioLayoutFRAlgorithm.toString());
		bioLayoutFRAlgorithmProps.setProperty("menuGravity","10.4");
		registerService(bc,bioLayoutFRAlgorithm,CyLayoutAlgorithm.class, bioLayoutFRAlgorithmProps);

		Properties groupAttributesLayoutProps = new Properties();
		groupAttributesLayoutProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		groupAttributesLayoutProps.setProperty("preferredTaskManager","menu");
		groupAttributesLayoutProps.setProperty("title",groupAttributesLayout.toString());
		groupAttributesLayoutProps.setProperty("menuGravity","10.6");
		registerService(bc,groupAttributesLayout,CyLayoutAlgorithm.class, groupAttributesLayoutProps);
	}
}

