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

import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
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

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		final UndoSupport undoSupport = getService(bc, UndoSupport.class);

		{
			final HierarchicalLayoutAlgorithm layout = new HierarchicalLayoutAlgorithm(serviceRegistrar, undoSupport);
			final Properties props = new Properties();
			props.setProperty("preferredTaskManager", "menu");
			props.setProperty(TITLE, layout.toString());
			props.setProperty(MENU_GRAVITY, "10.1");
			registerService(bc, layout, CyLayoutAlgorithm.class, props);
		}
		{
			final CircularLayoutAlgorithm layout = new CircularLayoutAlgorithm(undoSupport);
			final Properties props = new Properties();
			props.setProperty("preferredTaskManager", "menu");
			props.setProperty(TITLE, layout.toString());
			props.setProperty(MENU_GRAVITY, "10.2");
			registerService(bc, layout, CyLayoutAlgorithm.class, props);
		}
		{
			final StackedNodeLayout layout = new StackedNodeLayout(undoSupport);
			final Properties props = new Properties();
			props.setProperty("preferredTaskManager", "menu");
			props.setProperty(TITLE, layout.toString());
			props.setProperty(MENU_GRAVITY, "10.3");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			registerService(bc, layout, CyLayoutAlgorithm.class, props);
		}
		{
			final AttributeCircleLayout layout = new AttributeCircleLayout(undoSupport);
			final Properties props = new Properties();
			props.setProperty("preferredTaskManager", "menu");
			props.setProperty(TITLE, layout.toString());
			props.setProperty(MENU_GRAVITY, "10.4");
			registerService(bc, layout, CyLayoutAlgorithm.class, props);
		}
		{
			final DegreeSortedCircleLayout layout = new DegreeSortedCircleLayout(undoSupport);
			final Properties props = new Properties();
			props.setProperty("preferredTaskManager", "menu");
			props.setProperty(TITLE, layout.toString());
			props.setProperty(MENU_GRAVITY, "10.5");
			registerService(bc, layout, CyLayoutAlgorithm.class, props);
		}
		{
			final GroupAttributesLayout layout = new GroupAttributesLayout(undoSupport);
			final Properties props = new Properties();
			props.setProperty("preferredTaskManager", "menu");
			props.setProperty(TITLE, layout.toString());
			props.setProperty(MENU_GRAVITY, "10.6");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			registerService(bc, layout, CyLayoutAlgorithm.class, props);
		}
		{
			final BioLayoutFRAlgorithm layout = new BioLayoutFRAlgorithm(true, undoSupport);
			final Properties props = new Properties();
			props.setProperty("preferredTaskManager", "menu");
			props.setProperty(TITLE, layout.toString());
			props.setProperty(MENU_GRAVITY, "10.8");
			registerService(bc, layout, CyLayoutAlgorithm.class, props);
		}
		{
			final BioLayoutKKAlgorithm layout = new BioLayoutKKAlgorithm(true, undoSupport);
			final Properties props = new Properties();
			props.setProperty("preferredTaskManager", "menu");
			props.setProperty(TITLE, layout.toString());
			props.setProperty(MENU_GRAVITY, "10.9");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			registerService(bc, layout, CyLayoutAlgorithm.class, props);
		}
		{
			final ISOMLayout layout = new ISOMLayout(undoSupport);
			final Properties props = new Properties();
			props.setProperty("preferredTaskManager", "menu");
			props.setProperty(TITLE, layout.toString());
			props.setProperty(MENU_GRAVITY, "10.99");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			registerService(bc, layout, CyLayoutAlgorithm.class, props);
		}
	}
}
