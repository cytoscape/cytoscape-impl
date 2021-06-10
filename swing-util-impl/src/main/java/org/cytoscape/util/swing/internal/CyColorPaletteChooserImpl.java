package org.cytoscape.util.swing.internal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.util.color.PaletteType;
import org.cytoscape.util.swing.CyColorPaletteChooser;

/*
 * #%L
 * Cytoscape Swing Utility Impl (swing-util-impl)
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

@SuppressWarnings("serial")
class CyColorPaletteChooserImpl extends JDialog implements CyColorPaletteChooser {
	
	private final PaletteProviderManager paletteManager;
	private final PaletteType paletteType;

	protected Palette initialPalette;
	protected Color initialColor;

	// private Palette selectedPalette;
	// private Color selectedColor;
	private boolean paletteOnly;

	/**
	 * True if OK was pressed; false otherwise.
	 */
	protected boolean okWasPressed;

	/**
	 * OK Action Listener
	 */
	protected ActionListener okListener;

	/**
	 * Cancel Action Listener
	 */
	protected ActionListener cancelListener;

	/**
	 * The inner panel containing everything.
	 */
	protected JPanel innerPanel;

	/**
	 * If we want to be able to remember our color choices, we need
	 * to make sure not to reset the JColorChooser.  All of this
	 * static stuff is to allow us to do that.  Unfortunately, when
	 * we switch types or the number of colors or from paletteOnly
	 * to a full chooser, we need to know that.
	 */
	private static JColorChooser colorChooser;
	private static ColorPanelSelectionModel model;
	private static boolean lastPaletteOnly;
	private static int colorCount;
	private static PaletteType lastPaletteType;
	private static ColorPaletteProviderPanel[] palettePanels;

	public CyColorPaletteChooserImpl(PaletteProviderManager paletteManager, PaletteType type, boolean paletteOnly) {
		this.paletteOnly = paletteOnly;
		this.paletteManager = paletteManager;
		this.paletteType = type;
	}

	@Override
	public Color showDialog(Component parent, String title, Palette initialPalette, Color initialColor, int colors) {
		checkColorChooser(colors, false);
		paletteOnly = false;
		lastPaletteOnly = false;
		lastPaletteType = paletteType;
		
		if (colors < 1)
			colorCount = 9;
		else
			colorCount = colors;
		
		init(parent, title, initialPalette, initialColor);
		
		if (showDialog())
			return getSelectedColor();

		return initialColor;
	}

	@Override
	public Palette showDialog(Component parent, String title, Palette initialPalette, int colors) {
		checkColorChooser(colors, true);
		paletteOnly = true;
		lastPaletteOnly = true;
		lastPaletteType = paletteType;
		
		if (colors < 1) 
			colorCount = 9;
		else
			colorCount = colors;
		
		init(parent, title, initialPalette, null);
		
		if (showDialog())
			return getSelectedPalette();
		
		return initialPalette;
	}

	@Override
	public Color getSelectedColor() {
		return getColor();
	}

	@Override
	public Palette getSelectedPalette() {
		return getColorPalette();
	}

	private void init(Component parent, String title, Palette initialPalette, Color initialColor) {
		this.initialPalette = initialPalette;
		this.initialColor = initialColor;

		// UI configuration
		setLocationRelativeTo(parent);
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setTitle(title);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		var pane = getContentPane();
		pane.setLayout(new BorderLayout());

		// Inner panel
		innerPanel = new JPanel();
		innerPanel.setLayout(new BorderLayout());
		innerPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		pane.add(innerPanel, BorderLayout.CENTER);

		// Colors Tab
		var colorsTab = new JPanel();
		colorsTab.setBorder(new EmptyBorder(10, 10, 10, 10));
		colorsTab.setLayout(new BoxLayout(colorsTab, BoxLayout.Y_AXIS));
		innerPanel.add(colorsTab);

		if (colorChooser == null) {
			model = new ColorPanelSelectionModel();
			colorChooser = new JColorChooser(model);

			// Get the list of palettes
			var providers = getPaletteProviders(false);
			palettePanels = getPanels(providers, colorCount);

			// Get the standard color panels
			if (paletteOnly) {
				colorChooser.setChooserPanels(palettePanels);
				colorChooser.setPreviewPanel(new JPanel()); // Hide the preview panel
			} else {
				AbstractColorChooserPanel[] oldPanels = colorChooser.getChooserPanels();
				AbstractColorChooserPanel[] newPanels = new AbstractColorChooserPanel[oldPanels.length+palettePanels.length];
				
				for (int i = 0; i < palettePanels.length; i++) {
					newPanels[i] = palettePanels[i];
				}

				for (int i = 0; i < oldPanels.length; i++) {
					newPanels[i+palettePanels.length] = oldPanels[i];
				}
				
				colorChooser.setChooserPanels(newPanels);
			}
		}

		// set custom colorSelectionModel
		if (initialPalette != null)
			model.setPalette(initialPalette);

		if (initialColor != null)
			model.setSelectedColor(initialColor);

		colorsTab.add(colorChooser, BorderLayout.CENTER);

		// color blind friendly checkbox
		var cbFriendlyPanel = new JPanel();
		cbFriendlyPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		cbFriendlyPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		colorsTab.add(cbFriendlyPanel, BorderLayout.SOUTH);

		var cbFriendlyGridPanel = new JPanel();

		cbFriendlyPanel.add(cbFriendlyGridPanel);

		var colorBlindOnly = new JCheckBox("show only colorblind-friendly");
		colorBlindOnly.addActionListener(e -> {
			var source = (JCheckBox) e.getSource();
			
			for (var cbccp : palettePanels) {
				cbccp.setShowColorBlindSafe(source.isSelected());
				cbccp.updateChooser();
				colorChooser.repaint();
			}
		});
		cbFriendlyGridPanel.add(colorBlindOnly);

		// OK, Cancel, and Reset buttons
		var buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		colorsTab.add(buttonPanel, BorderLayout.CENTER);

		var buttonGridPanel = new JPanel();
		buttonGridPanel.setLayout(new GridLayout(1, 3, 5, 0));
		buttonPanel.add(buttonGridPanel);

		// Reset
		var resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> {
			var palette = CyColorPaletteChooserImpl.this.initialPalette;
			setPalette(palette);
			setColor(CyColorPaletteChooserImpl.this.initialColor);

			for (var cbccp : palettePanels) {
				cbccp.setSelectedPalette(palette != null ? palette.getName() : null);
				cbccp.updateChooser();
				colorChooser.repaint();
			}
		});
		buttonGridPanel.add( resetButton );

		// OK
		var okButton = new JButton("OK");
		okButton.setDefaultCapable(true);
		getRootPane().setDefaultButton(okButton);
		okButton.addActionListener(e -> {
			CyColorPaletteChooserImpl.this.okWasPressed = true;
			CyColorPaletteChooserImpl.this.setVisible(false);
			CyColorPaletteChooserImpl.this.initialPalette = getColorPalette();
			
			if (CyColorPaletteChooserImpl.this.okListener != null)
				CyColorPaletteChooserImpl.this.okListener.actionPerformed(e);
		});
		buttonGridPanel.add(okButton);

		// Cancel
		var cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> {
			setPalette(CyColorPaletteChooserImpl.this.initialPalette);
			CyColorPaletteChooserImpl.this.okWasPressed = false;
			CyColorPaletteChooserImpl.this.setVisible(false);
			
			if (CyColorPaletteChooserImpl.this.cancelListener != null)
				CyColorPaletteChooserImpl.this.cancelListener.actionPerformed(e);
		});
		buttonGridPanel.add(cancelButton);

		this.pack();
		this.validate();
	}

	private boolean checkColorChooser(int colors, boolean paletteO) {
		if (colors < 1)
			colors = 9;

		if (CyColorPaletteChooserImpl.colorCount == colors && CyColorPaletteChooserImpl.lastPaletteOnly == paletteO
				&& CyColorPaletteChooserImpl.lastPaletteType == paletteType)
			return true;
		
		colorChooser = null;
		
		return false;
	}

//----------------------------------------------------------------------
//  Methods
//----------------------------------------------------------------------
	
	/**
	 * Shows the dialog box and waits for the user to press OK or Cancel. When
	 * either is pressed, the dialog box is hidden. A true is returned if OK was
	 * pressed, and false otherwise.
	 * <P>
	 * This method blocks until the dialog is closed by the user, regardless of
	 * whether the dialog box is modal or not.
	 *
	 * @return true if OK was pressed
	 */
	public boolean showDialog() {
		if (isModal()) {
			setVisible(true);
			return okWasPressed;
		}

		setModal(true);
		setVisible(true);

		boolean status = okWasPressed;
		setModal(false);
		dispose();

		return status;
	}

	/**
	 * Returns true if the OK button was pressed to close the window, and false
	 * otherwise.
	 *
	 * @return true if OK was pressed
	 */
	public boolean wasOKPressed() {
		return okWasPressed;
	}

	/**
	 * Get the current color in the color chooser.
	 *
	 * @return the current color
	 */
	public Color getColor() {
		return colorChooser.getColor();
	}

	/**
	 * Set the current color in the color chooser.
	 *
	 * @param color the new color
	 */
	public void setColor(Color color) {
		colorChooser.setColor(color);
		initialColor = color;
	}

	/**
	 * Set the current color in the color chooser.
	 *
	 * @param red   the red component of the new color
	 * @param green the green component of the new color
	 * @param blue  the blue component of the new color
	 */
	public void setColor(int red, int green, int blue) {
		initialColor = new Color(red, green, blue);
		colorChooser.setColor(initialColor);
	}

	/**
	 * Get the current color in the color chooser.
	 *
	 * @return the current color
	 */
	public Palette getColorPalette() {
		var model = (ColorPanelSelectionModel) colorChooser.getSelectionModel();
		return model.getPalette();
	}

	/**
	 * Set the current color in the color chooser.
	 *
	 * @param color the new color
	 */
	public void setPalette(Palette palette) {
		if (palette == null)
			palette = initialPalette;
		
		var model = (ColorPanelSelectionModel) colorChooser.getSelectionModel();
		model.setPalette(palette);
		initialPalette = palette;
	}

	private List<PaletteProvider> getPaletteProviders(boolean colorBlindOnly) {
		return paletteManager.getPaletteProviders(paletteType, colorBlindOnly);
	}

	private ColorPaletteProviderPanel[] getPanels(List<PaletteProvider> providers, int size) {
		var panels = new ArrayList<ColorPaletteProviderPanel>();
		
		for (var provider : providers) {
			if (paletteType.equals(BrewerType.ANY)) {
				for (var t : provider.getPaletteTypes()) {
					panels.add(new ColorPaletteProviderPanel(provider, t, size, paletteOnly));
				}
			} else {
				panels.add(new ColorPaletteProviderPanel(provider, paletteType, size, paletteOnly));
			}
		}
		
		return panels.toArray(new ColorPaletteProviderPanel[1]);
	}
}
