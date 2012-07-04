



package csapps.layout; 

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

import csapps.layout.algorithms.GroupAttributesLayout;
import csapps.layout.algorithms.StackedNodeLayout;
import csapps.layout.algorithms.bioLayout.BioLayoutFRAlgorithm;
import csapps.layout.algorithms.bioLayout.BioLayoutKKAlgorithm;
import csapps.layout.algorithms.circularLayout.CircularLayoutAlgorithm;
import csapps.layout.algorithms.graphPartition.AttributeCircleLayout;
import csapps.layout.algorithms.graphPartition.DegreeSortedCircleLayout;
import csapps.layout.algorithms.graphPartition.ISOMLayout;
import csapps.layout.algorithms.hierarchicalLayout.HierarchicalLayoutAlgorithm;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.presentation.property.values.BendFactory;

import static org.cytoscape.work.ServiceProperties.*;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {
		
		UndoSupport undo = getService(bc,UndoSupport.class);
		HandleFactory hf = getService(bc,HandleFactory.class);
		BendFactory bf = getService(bc,BendFactory.class);

		CircularLayoutAlgorithm circularLayoutAlgorithm = new CircularLayoutAlgorithm(undo);
		HierarchicalLayoutAlgorithm hierarchicalLayoutAlgorithm = new HierarchicalLayoutAlgorithm(undo,hf,bf);
		AttributeCircleLayout attributeCircleLayout = new AttributeCircleLayout(undo);
		DegreeSortedCircleLayout degreeSortedCircleLayout = new DegreeSortedCircleLayout(undo);
		ISOMLayout ISOMLayout = new ISOMLayout(undo);
		// BioLayoutKKAlgorithm bioLayoutKKAlgorithmFALSE = new BioLayoutKKAlgorithm(false,undo);
		BioLayoutKKAlgorithm bioLayoutKKAlgorithmTRUE = new BioLayoutKKAlgorithm(true,undo);
		BioLayoutFRAlgorithm bioLayoutFRAlgorithm = new BioLayoutFRAlgorithm(true,undo);
		StackedNodeLayout stackedNodeLayout = new StackedNodeLayout(undo);
		GroupAttributesLayout groupAttributesLayout = new GroupAttributesLayout(undo);
		
		
		Properties circularLayoutAlgorithmProps = new Properties();
		circularLayoutAlgorithmProps.setProperty(PREFERRED_MENU,"Layout.Cytoscape Layouts");
		circularLayoutAlgorithmProps.setProperty("preferredTaskManager","menu");
		circularLayoutAlgorithmProps.setProperty(TITLE,circularLayoutAlgorithm.toString());
		circularLayoutAlgorithmProps.setProperty(MENU_GRAVITY,"10.2");
		
		registerService(bc,circularLayoutAlgorithm,CyLayoutAlgorithm.class, circularLayoutAlgorithmProps);
		
		Properties hierarchicalLayoutAlgorithmProps = new Properties();
		hierarchicalLayoutAlgorithmProps.setProperty(PREFERRED_MENU,"Layout.Cytoscape Layouts");
		hierarchicalLayoutAlgorithmProps.setProperty("preferredTaskManager","menu");
		hierarchicalLayoutAlgorithmProps.setProperty(TITLE,hierarchicalLayoutAlgorithm.toString());
		hierarchicalLayoutAlgorithmProps.setProperty(MENU_GRAVITY,"10.8");
		registerService(bc,hierarchicalLayoutAlgorithm,CyLayoutAlgorithm.class, hierarchicalLayoutAlgorithmProps);

		Properties attributeCircleLayoutProps = new Properties();
		attributeCircleLayoutProps.setProperty(PREFERRED_MENU,"Layout.Cytoscape Layouts");
		attributeCircleLayoutProps.setProperty("preferredTaskManager","menu");
		attributeCircleLayoutProps.setProperty(TITLE,attributeCircleLayout.toString());
		attributeCircleLayoutProps.setProperty(MENU_GRAVITY,"10.1");
		registerService(bc,attributeCircleLayout,CyLayoutAlgorithm.class, attributeCircleLayoutProps);

		Properties degreeSortedCircleLayoutProps = new Properties();
		degreeSortedCircleLayoutProps.setProperty(PREFERRED_MENU,"Layout.Cytoscape Layouts");
		degreeSortedCircleLayoutProps.setProperty("preferredTaskManager","menu");
		degreeSortedCircleLayoutProps.setProperty(TITLE,degreeSortedCircleLayout.toString());
		degreeSortedCircleLayoutProps.setProperty(MENU_GRAVITY,"10.3");
		registerService(bc,degreeSortedCircleLayout,CyLayoutAlgorithm.class, degreeSortedCircleLayoutProps);

		Properties ISOMLayoutProps = new Properties();
		ISOMLayoutProps.setProperty(PREFERRED_MENU,"Layout.Cytoscape Layouts");
		ISOMLayoutProps.setProperty("preferredTaskManager","menu");
		ISOMLayoutProps.setProperty(TITLE,ISOMLayout.toString());
		ISOMLayoutProps.setProperty(MENU_GRAVITY,"10.9");
		registerService(bc,ISOMLayout,CyLayoutAlgorithm.class, ISOMLayoutProps);

		Properties bioLayoutKKAlgorithmFALSEProps = new Properties();
		bioLayoutKKAlgorithmFALSEProps.setProperty(PREFERRED_MENU,"Layout.Cytoscape Layouts");
		bioLayoutKKAlgorithmFALSEProps.setProperty("preferredTaskManager","menu");
		bioLayoutKKAlgorithmFALSEProps.setProperty(TITLE,bioLayoutKKAlgorithmFALSE.toString());
		bioLayoutKKAlgorithmFALSEProps.setProperty(MENU_GRAVITY,"10.99");
		registerService(bc,bioLayoutKKAlgorithmFALSE,CyLayoutAlgorithm.class, bioLayoutKKAlgorithmFALSEProps);
		
		Properties bioLayoutKKAlgorithmTRUEProps = new Properties();
		bioLayoutKKAlgorithmTRUEProps.setProperty(PREFERRED_MENU,"Layout.Cytoscape Layouts");
		bioLayoutKKAlgorithmTRUEProps.setProperty("preferredTaskManager","menu");
		bioLayoutKKAlgorithmTRUEProps.setProperty(TITLE,bioLayoutKKAlgorithmTRUE.toString());
		bioLayoutKKAlgorithmTRUEProps.setProperty(MENU_GRAVITY,"10.5");
		registerService(bc,bioLayoutKKAlgorithmTRUE,CyLayoutAlgorithm.class, bioLayoutKKAlgorithmTRUEProps);
		
		Properties bioLayoutFRAlgorithmProps = new Properties();
		bioLayoutFRAlgorithmProps.setProperty(PREFERRED_MENU,"Layout.Cytoscape Layouts");
		bioLayoutFRAlgorithmProps.setProperty("preferredTaskManager","menu");
		bioLayoutFRAlgorithmProps.setProperty(TITLE,bioLayoutFRAlgorithm.toString());
		bioLayoutFRAlgorithmProps.setProperty(MENU_GRAVITY,"10.4");
		registerService(bc,bioLayoutFRAlgorithm,CyLayoutAlgorithm.class, bioLayoutFRAlgorithmProps);

		Properties groupAttributesLayoutProps = new Properties();
		groupAttributesLayoutProps.setProperty(PREFERRED_MENU,"Layout.Cytoscape Layouts");
		groupAttributesLayoutProps.setProperty("preferredTaskManager","menu");
		groupAttributesLayoutProps.setProperty(TITLE,groupAttributesLayout.toString());
		groupAttributesLayoutProps.setProperty(MENU_GRAVITY,"10.6");
		registerService(bc,groupAttributesLayout,CyLayoutAlgorithm.class, groupAttributesLayoutProps);
	}
}

