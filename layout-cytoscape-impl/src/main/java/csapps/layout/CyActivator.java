package csapps.layout;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


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

		// BioLayoutKKAlgorithm bioLayoutKKAlgorithmFALSE = new BioLayoutKKAlgorithm(false,undo);
		
		HierarchicalLayoutAlgorithm hierarchicalLayoutAlgorithm = new HierarchicalLayoutAlgorithm(undo,hf,bf);
		Properties hierarchicalLayoutAlgorithmProps = new Properties();
		// hierarchicalLayoutAlgorithmProps.setProperty(PREFERRED_MENU, "Layout");
		hierarchicalLayoutAlgorithmProps.setProperty("preferredTaskManager","menu");
		hierarchicalLayoutAlgorithmProps.setProperty(TITLE,hierarchicalLayoutAlgorithm.toString());
		hierarchicalLayoutAlgorithmProps.setProperty(MENU_GRAVITY,"10.1");
		registerService(bc,hierarchicalLayoutAlgorithm,CyLayoutAlgorithm.class, hierarchicalLayoutAlgorithmProps);
		
		CircularLayoutAlgorithm circularLayoutAlgorithm = new CircularLayoutAlgorithm(undo);
		Properties circularLayoutAlgorithmProps = new Properties();
		// circularLayoutAlgorithmProps.setProperty(PREFERRED_MENU, "Layout");
		circularLayoutAlgorithmProps.setProperty("preferredTaskManager","menu");
		circularLayoutAlgorithmProps.setProperty(TITLE,circularLayoutAlgorithm.toString());
		circularLayoutAlgorithmProps.setProperty(MENU_GRAVITY,"10.2");
		registerService(bc,circularLayoutAlgorithm,CyLayoutAlgorithm.class, circularLayoutAlgorithmProps);

		StackedNodeLayout stackedNodeLayout = new StackedNodeLayout(undo);
		Properties stackedNodeLayoutProps = new Properties();
		// stackedNodeLayoutProps.setProperty(PREFERRED_MENU, "Layout");
		stackedNodeLayoutProps.setProperty("preferredTaskManager","menu");
		stackedNodeLayoutProps.setProperty(TITLE,stackedNodeLayout.toString());
		stackedNodeLayoutProps.setProperty(MENU_GRAVITY,"10.3");
		stackedNodeLayoutProps.setProperty(INSERT_SEPARATOR_AFTER,"true");
		registerService(bc,stackedNodeLayout,CyLayoutAlgorithm.class, stackedNodeLayoutProps);

		AttributeCircleLayout attributeCircleLayout = new AttributeCircleLayout(undo);
		Properties attributeCircleLayoutProps = new Properties();
		// attributeCircleLayoutProps.setProperty(PREFERRED_MENU, "Layout");
		attributeCircleLayoutProps.setProperty("preferredTaskManager","menu");
		attributeCircleLayoutProps.setProperty(TITLE,attributeCircleLayout.toString());
		attributeCircleLayoutProps.setProperty(MENU_GRAVITY,"10.4");
		registerService(bc,attributeCircleLayout,CyLayoutAlgorithm.class, attributeCircleLayoutProps);

		DegreeSortedCircleLayout degreeSortedCircleLayout = new DegreeSortedCircleLayout(undo);
		Properties degreeSortedCircleLayoutProps = new Properties();
		// degreeSortedCircleLayoutProps.setProperty(PREFERRED_MENU, "Layout");
		degreeSortedCircleLayoutProps.setProperty("preferredTaskManager","menu");
		degreeSortedCircleLayoutProps.setProperty(TITLE,degreeSortedCircleLayout.toString());
		degreeSortedCircleLayoutProps.setProperty(MENU_GRAVITY,"10.5");
		registerService(bc,degreeSortedCircleLayout,CyLayoutAlgorithm.class, degreeSortedCircleLayoutProps);

		GroupAttributesLayout groupAttributesLayout = new GroupAttributesLayout(undo);
		Properties groupAttributesLayoutProps = new Properties();
		// groupAttributesLayoutProps.setProperty(PREFERRED_MENU, "Layout");
		groupAttributesLayoutProps.setProperty("preferredTaskManager","menu");
		groupAttributesLayoutProps.setProperty(TITLE,groupAttributesLayout.toString());
		groupAttributesLayoutProps.setProperty(MENU_GRAVITY,"10.6");
		groupAttributesLayoutProps.setProperty(INSERT_SEPARATOR_AFTER,"true");
		registerService(bc,groupAttributesLayout,CyLayoutAlgorithm.class, groupAttributesLayoutProps);

		/*
		Properties bioLayoutKKAlgorithmFALSEProps = new Properties();
		bioLayoutKKAlgorithmFALSEProps.setProperty(PREFERRED_MENU, "Layout");
		bioLayoutKKAlgorithmFALSEProps.setProperty("preferredTaskManager","menu");
		bioLayoutKKAlgorithmFALSEProps.setProperty(TITLE,bioLayoutKKAlgorithmFALSE.toString());
		bioLayoutKKAlgorithmFALSEProps.setProperty(MENU_GRAVITY,"10.99");
		registerService(bc,bioLayoutKKAlgorithmFALSE,CyLayoutAlgorithm.class, bioLayoutKKAlgorithmFALSEProps);
		*/
		
		BioLayoutFRAlgorithm bioLayoutFRAlgorithm = new BioLayoutFRAlgorithm(true,undo);
		Properties bioLayoutFRAlgorithmProps = new Properties();
		// bioLayoutFRAlgorithmProps.setProperty(PREFERRED_MENU, "Layout");
		bioLayoutFRAlgorithmProps.setProperty("preferredTaskManager","menu");
		bioLayoutFRAlgorithmProps.setProperty(TITLE,bioLayoutFRAlgorithm.toString());
		bioLayoutFRAlgorithmProps.setProperty(MENU_GRAVITY,"10.8");
		registerService(bc,bioLayoutFRAlgorithm,CyLayoutAlgorithm.class, bioLayoutFRAlgorithmProps);
		
		BioLayoutKKAlgorithm bioLayoutKKAlgorithmTRUE = new BioLayoutKKAlgorithm(true,undo);
		Properties bioLayoutKKAlgorithmTRUEProps = new Properties();
		// bioLayoutKKAlgorithmTRUEProps.setProperty(PREFERRED_MENU, "Layout");
		bioLayoutKKAlgorithmTRUEProps.setProperty("preferredTaskManager","menu");
		bioLayoutKKAlgorithmTRUEProps.setProperty(TITLE,bioLayoutKKAlgorithmTRUE.toString());
		bioLayoutKKAlgorithmTRUEProps.setProperty(MENU_GRAVITY,"10.9");
		bioLayoutKKAlgorithmTRUEProps.setProperty(INSERT_SEPARATOR_AFTER,"true");
		registerService(bc,bioLayoutKKAlgorithmTRUE,CyLayoutAlgorithm.class, bioLayoutKKAlgorithmTRUEProps);

		ISOMLayout ISOMLayout = new ISOMLayout(undo);
		Properties ISOMLayoutProps = new Properties();
		// ISOMLayoutProps.setProperty(PREFERRED_MENU, "Layout");
		ISOMLayoutProps.setProperty("preferredTaskManager","menu");
		ISOMLayoutProps.setProperty(TITLE,ISOMLayout.toString());
		ISOMLayoutProps.setProperty(MENU_GRAVITY,"10.99");
		ISOMLayoutProps.setProperty(INSERT_SEPARATOR_AFTER,"true");
		registerService(bc,ISOMLayout,CyLayoutAlgorithm.class, ISOMLayoutProps);
	}
}

