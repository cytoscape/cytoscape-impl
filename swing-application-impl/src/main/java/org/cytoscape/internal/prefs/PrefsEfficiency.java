package org.cytoscape.internal.prefs;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;

import org.cytoscape.internal.prefs.lib.HBox;

public class PrefsEfficiency extends AbstractPrefsPanel {

	protected PrefsEfficiency(Cy3PreferencesRoot dlog) {
		super(dlog,  "cytoscape3");
		setBorder(BorderFactory.createEmptyBorder(20,32,0,0));

	}
    @Override public void initUI()
    {
        super.initUI();
		Box col1 = Box.createVerticalBox();
		HBox firstLine = new HBox(new JLabel("Thresholds"), Box.createHorizontalGlue());
		col1.add(firstLine);
		col1.add(Box.createRigidArea(new Dimension(20,14)));
	    col1.add( makeNumberSliderRow("Coarse Detail", "render.coarseDetailThreshold", 500, 50000, 1000));
	    col1.add(makeNumberSliderRow("Edge Arrow",  "render.edgeArrowThreshold", 500, 5000000, 1000));
	    col1.add(makeNumberSliderRow("Node Border",  "render.nodeBorderThreshold", 1000, 5000000, 5000));
	    col1.add(makeNumberSliderRow("Node Label",  "render.nodeLabelThreshold", 100, 50000, 500));
	    col1.add(makeNumberSliderRow("View Threshold", "viewThreshold",  1000, 5000000, 100000));
		col1.add(Box.createRigidArea(new Dimension(20,14)));
	    col1.add(makeNumberSliderRow("Undo Depth", "undo.limit",  1, 500, 10));

		add(col1);   
	}


}
