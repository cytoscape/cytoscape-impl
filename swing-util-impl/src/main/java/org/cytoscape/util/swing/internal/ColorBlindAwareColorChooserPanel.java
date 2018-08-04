package org.cytoscape.util.swing.internal;

import java.awt.Container;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andreas on 7/19/15.
 */
public abstract class ColorBlindAwareColorChooserPanel extends AbstractColorChooserPanel  {

    boolean showColorBlindSafe = false;
		ColorBlindAwareColorChooserPanel chooserPanel;
		protected String selectedPalette = null;

    List<Container> currentButtons = new ArrayList<Container>();

    public boolean isShowColorBlindSafe() {
        return showColorBlindSafe;
    }

    public void setShowColorBlindSafe(boolean showColorBlindSafe) {
				if (this.showColorBlindSafe == showColorBlindSafe)
					return;
        this.showColorBlindSafe = showColorBlindSafe;
				chooserPanel = null;
        this.repaint();
    }

		abstract public void setSelectedPalette(String palette);

    public void updateChooser() {

        if ( currentButtons == null)
            currentButtons = new ArrayList<Container>();

				if (chooserPanel == null) {
        	for (Container c: currentButtons) {
         	   remove(c);
        	}
        	currentButtons.clear();

        	buildChooser();
					chooserPanel = this;
				}

    }

}
