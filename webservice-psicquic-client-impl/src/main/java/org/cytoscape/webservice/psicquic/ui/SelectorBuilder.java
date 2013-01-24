package org.cytoscape.webservice.psicquic.ui;

/*
 * #%L
 * Cytoscape PSIQUIC Web Service Impl (webservice-psicquic-client-impl)
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

import javax.swing.JComboBox;

public class SelectorBuilder {

	protected enum Species {
		HUMAN("Homo sapiens"), MOUSE("Mus musculus"), YEAST("Saccharomyces cerevisiae"), FLY(
				"Drosophila melanogaster"), ROUNDWORM("Caenorhabditis elegans"), ZEBRAFISH("Danio rerio"), MOUSE_EAR_CRESS(
				"Arabidopsis Thaliana");

		private final String name;

		private Species(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public JComboBox getComboBox() {
		final JComboBox speciesBox = new JComboBox();

		for (final Species sp : Species.values())
			speciesBox.addItem(sp);

		return speciesBox;
	}

}
