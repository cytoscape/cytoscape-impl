package org.cytoscape.webservice.psicquic.ui;

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
