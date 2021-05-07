package org.cytoscape.ding.impl;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Properties;

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
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * NetworkViewLocationTaskFactory Action Support.
 */
public class NVLTFActionSupport {
	
    private final CyServiceRegistrar registrar;
    
    public NVLTFActionSupport(final CyServiceRegistrar registrar) {
        this.registrar = registrar;
    }

	public void registerAction(NetworkViewLocationTaskFactory nvltf, Map<String, String> props) {
		// If the user requests this action to be in the menu bar, create and register a CyAction for it
		if (props.containsKey(ServiceProperties.IN_MENU_BAR)
				&& Boolean.valueOf(props.get(ServiceProperties.IN_MENU_BAR)) == Boolean.TRUE) {
			CyAction action = new NVLTFAction(nvltf, props);
			registrar.registerService(action, CyAction.class, new Properties());
		}
	}

    @SuppressWarnings("serial")
	private class NVLTFAction extends AbstractCyAction {
        
        private final NetworkViewLocationTaskFactory nvltf;

        public NVLTFAction(NetworkViewLocationTaskFactory nvltf, Map<String, String> props) {
			super(
					props,
					registrar.getService(CyApplicationManager.class),
					registrar.getService(CyNetworkViewManager.class),
					createTaskFactory(nvltf)
			);
            this.nvltf = nvltf;
        }

        @Override
        public void actionPerformed(ActionEvent a) {
			CyNetworkView view = registrar.getService(CyApplicationManager.class).getCurrentNetworkView();
			DRenderingEngine re = registrar.getService(DingRenderer.class).getRenderingEngine(view);

			// We need to use the glass pane  because its on top and it's the only one that provides the mouse position.
			Component foregroundCanvas = re.getInputHandlerGlassPane();

			// Now get the current mouse position
			Point2D javaPt = foregroundCanvas.getMousePosition();
			if (javaPt == null)
				javaPt = new Point2D.Double(0.0, 0.0);

			// Now transform the mouse position to our coordinate space
			double[] coords = new double[2];
			coords[0] = javaPt.getX();
			coords[1] = javaPt.getY();
			re.getTransform().xformImageToNodeCoords(coords);
			Point2D xformPt = new Point2D.Double(coords[0], coords[1]);

			registrar.getService(DialogTaskManager.class).execute(nvltf.createTaskIterator(view, javaPt, xformPt));
        }
    }

	TaskFactory createTaskFactory(final NetworkViewLocationTaskFactory taskFactory) {
		return new TaskFactory() {
			@Override
			public boolean isReady() {
				return taskFactory.isReady(registrar.getService(CyApplicationManager.class).getCurrentNetworkView(),
						null, null);
			}

			@Override
			public TaskIterator createTaskIterator() {
				return taskFactory.createTaskIterator(
						registrar.getService(CyApplicationManager.class).getCurrentNetworkView(), null, null);
			}
		};
	}
}
