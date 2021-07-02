/*
 * BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence. This should
 * be distributed with the code. If you do not have a copy,
 * see:
 *
 * http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors. These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 * http://www.biojava.org/
 *
 * This code was contributed from the Molecular Biology Toolkit
 * (MBT) project at the University of California San Diego.
 *
 * Please reference J.L. Moreland, A.Gramada, O.V. Buzko, Qing
 * Zhang and P.E. Bourne 2005 The Molecular Biology Toolkit (MBT):
 * A Modular Platform for Developing Molecular Visualization
 * Applications. BMC Bioinformatics, 6:21.
 *
 * The MBT project was funded as part of the National Institutes
 * of Health PPG grant number 1-P01-GM63208 and its National
 * Institute of General Medical Sciences (NIGMS) division. Ongoing
 * development for the MBT project is managed by the RCSB
 * Protein Data Bank(http://www.pdb.org) and supported by funds
 * from the National Science Foundation (NSF), the National
 * Institute of General Medical Sciences (NIGMS), the Office of
 * Science, Department of Energy (DOE), the National Library of
 * Medicine (NLM), the National Cancer Institute (NCI), the
 * National Center for Research Resources (NCRR), the National
 * Institute of Biomedical Imaging and Bioengineering (NIBIB),
 * the National Institute of Neurological Disorders and Stroke
 * (NINDS), and the National Institute of Diabetes and Digestive
 * and Kidney Diseases (NIDDK).
 *
 * Created on 2011/11/08
 *
 */
package org.cytoscape.util.swing.internal;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;

import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteType;


/**
 * Creates a color palette of qualitative colors defined by ColorBrewer
 * 
 * @author Peter Rose
 */
@SuppressWarnings("serial")
public class ColorPaletteProviderPanel extends ColorBlindAwareColorChooserPanel implements ActionListener {

	private Map<String, JPanel> paletteMap;
	private PaletteProvider provider;
	private PaletteType paletteType;
	private List<Palette> palettes;
	private boolean paletteOnly;
	private int paletteSize;

	public ColorPaletteProviderPanel(PaletteProvider provider, PaletteType type, int size, boolean paletteOnly) {
		this.provider = provider;
		this.paletteType = type;
		this.paletteOnly = paletteOnly;
		this.paletteSize = size;
		palettes = new ArrayList<>();
		
		for (var paletteId : provider.listPaletteIdentifiers(type, false)) {
			palettes.add(provider.getPalette(paletteId, size));
		}
	}

	protected JPanel createPalette(Palette palette, Border normalBorder, Border selectedBorder) {
		var panel = new JPanel();
		var layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);
		
		var colors = palette.getColors(paletteSize);
		
		for (int colorIndex = 0; colorIndex < paletteSize; colorIndex++) {
			Color clr;
			clr = colors[colorIndex];
			var colorButton = new JButton();
			colorButton.setActionCommand(palette.getName() + ":" + String.valueOf(clr.getRGB()));
			colorButton.addActionListener(this);
			colorButton.setIcon(new ColorIcon(clr, 15, 15, paletteOnly));
			// colorButton.setBorder(normalBorder);
			colorButton.setBorder(BorderFactory.createEmptyBorder());
			colorButton.setToolTipText(palette.getName());
			panel.add(colorButton);
		}

		if (palette.getName().equals(selectedPalette)) {
			panel.setBorder(selectedBorder);
		} else {
			panel.setBorder(normalBorder);
		}
		
		if (paletteMap == null)
			paletteMap = new HashMap<String, JPanel>();
		
		paletteMap.put(palette.getName(), panel);
		
		return panel;
	}

	@Override
	protected void buildChooser() {
		setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

		var border = BorderFactory.createEmptyBorder(2, 6, 2, 6);
		var selectedBorder = BorderFactory.createLineBorder(UIManager.getColor("List.selectionBackground"), 2);

		if (selectedPalette == null) {
			var model = (ColorPanelSelectionModel) getColorSelectionModel();
			var p = model.getPalette();
			
			if (p != null)
				selectedPalette = p.getName();
		}

		for (var palette : palettes) {
			if (isShowColorBlindSafe()) {
				if (!palette.isColorBlindSafe())
					continue;
			}

      // Get the color order right
      palette.reverse(reverseColors);

			var button = createPalette(palette, border, selectedBorder);
			add(button);
			currentButtons.add(button);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		var model = getColorSelectionModel();

		var command = ((JButton) e.getSource()).getActionCommand();
		var colorSplit = command.split(":");
		selectedPalette = colorSplit[0];
		
		var color = new Color(Integer.parseInt(colorSplit[1]));
		
		if (!paletteOnly)
			model.setSelectedColor(color);

		for (var palette: palettes) {
			var selectedPanel = paletteMap.get(palette.getName());
			
			if (palette.getName().equals(selectedPalette)) {
				((ColorPanelSelectionModel) model).setPalette(palette);
				selectedPanel.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(2, 4, 2, 4),
						BorderFactory.createLineBorder(UIManager.getColor("List.selectionBackground"), 2)
				));
			} else {
				selectedPanel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
			}
		}
	}

	@Override
	public String getDisplayName() {
		return provider.getProviderName() + " " + getTypeName();
	}

	@Override
	public void setSelectedPalette(String palette) {
		selectedPalette = palette;
		var model = getColorSelectionModel();

		for (var plt: palettes) {
			var selectedPanel = paletteMap.get(plt.getName());
			
			if (plt.getName().equals(selectedPalette)) {
				((ColorPanelSelectionModel) model).setPalette(plt);
				selectedPanel.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(2, 4, 2, 4),
						BorderFactory.createLineBorder(UIManager.getColor("List.selectionBackground"), 2)
				));
			} else {
				selectedPanel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
			}
		}
	}

	public void stateChanged(ChangeEvent ce) {
		// getColorSelectionModel().setSelectedColor(new Color(1));
	}

	@Override
	public Icon getLargeDisplayIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Icon getSmallDisplayIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	private String getTypeName() {
		if (paletteType.equals(BrewerType.DIVERGING))
			return "Diverging";
		else if (paletteType.equals(BrewerType.SEQUENTIAL))
			return "Sequential";
		else if (paletteType.equals(BrewerType.QUALITATIVE))
			return "Qualitative";
		
		return "";
	}
}
