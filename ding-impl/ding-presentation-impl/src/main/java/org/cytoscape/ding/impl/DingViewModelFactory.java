package org.cytoscape.ding.impl;

import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.values.HandleFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class DingViewModelFactory implements CyNetworkViewFactory {

	private final VisualLexicon dingLexicon;
	private final CyServiceRegistrar registrar;

	private ViewTaskFactoryListener vtfListener;
	private final AnnotationFactoryManager annMgr;
	private final DingGraphLOD dingGraphLOD;
	private final HandleFactory handleFactory;

	public DingViewModelFactory(
			final VisualLexicon dingLexicon, 
			final ViewTaskFactoryListener vtfListener,
			final AnnotationFactoryManager annMgr,
			final DingGraphLOD dingGraphLOD,
			final HandleFactory handleFactory,
			final CyServiceRegistrar registrar
	) {
		this.dingLexicon = dingLexicon;
		this.vtfListener = vtfListener;
		this.annMgr = annMgr;
		this.dingGraphLOD = dingGraphLOD;
		this.handleFactory = handleFactory;
		this.registrar = registrar;
	}

	@Override
	public CyNetworkView createNetworkView(final CyNetwork network) {
		if (network == null)
			throw new IllegalArgumentException("Cannot create view without model.");

		final DGraphView dgv = new DGraphView(network, dingLexicon, vtfListener, annMgr, dingGraphLOD, handleFactory,
				registrar);

		dgv.registerServices();

		return dgv;
	}
}
