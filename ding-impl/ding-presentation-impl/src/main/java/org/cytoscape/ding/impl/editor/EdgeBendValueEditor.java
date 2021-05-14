package org.cytoscape.ding.impl.editor;

import java.awt.Component;
import java.util.Objects;

import org.cytoscape.ding.impl.BendImpl;
import org.cytoscape.ding.impl.DingNetworkViewFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

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
public class EdgeBendValueEditor implements ValueEditor<Bend> {

	private final DingNetworkViewFactory cyNetworkViewFactory;
	private final RenderingEngineFactory<CyNetwork> presentationFactory;
	private final CyServiceRegistrar serviceRegistrar;


	public EdgeBendValueEditor(
			final DingNetworkViewFactory cyNetworkViewFactory,
			final RenderingEngineFactory<CyNetwork> presentationFactory,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.cyNetworkViewFactory = Objects.requireNonNull(cyNetworkViewFactory, "CyNetworkViewFactory is null.");
		this.presentationFactory  = Objects.requireNonNull(presentationFactory, "RenderingEngineFactory is null.");
		this.serviceRegistrar = serviceRegistrar;
	}

	
	@Override
	public <S extends Bend> Bend showEditor(Component parent, S initialValue) {
		EdgeBendValueEditorDialog dialog = new EdgeBendValueEditorDialog(cyNetworkViewFactory, presentationFactory, serviceRegistrar);
		Bend bend = dialog.showDialog(parent, initialValue);
		
		// The reason to create a new object is so that the view model thinks its a new value
		// and a ViewChangeEvent gets fired. If we just modify the existing Bend object
		// then the view model doesn't see the change.
		if(!dialog.isEditCancelled() && bend instanceof BendImpl) {
			bend = new BendImpl((BendImpl)bend);
		}
		
		return bend;
	}

	@Override
	public Class<Bend> getValueType() {
		return Bend.class;
	}
}
