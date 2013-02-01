package org.cytoscape.psi_mi.internal.plugin;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
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

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public class PsiMiNetworkWriterFactory implements CyNetworkViewWriterFactory {

	private final SchemaVersion version;
	private final CyFileFilter filter;

	public PsiMiNetworkWriterFactory(SchemaVersion version, CyFileFilter filter) {
		this.version = version;
		this.filter = filter;
	}
	
	@Override
	public CyWriter createWriter(OutputStream os, CyNetwork network) {
		return new PsiMiWriter(os, network, version);
	}

	@Override
	public CyWriter createWriter(OutputStream os, CyNetworkView view) {
		return new PsiMiWriter(os, view.getModel(), version);
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}
}
