package org.cytoscape.util.swing.internal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
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

import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;

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

class CyColorPaletteChooserImpl extends JDialog implements CyColorPaletteChooser {
	private static final long serialVersionUID = -1L; // FIXME

	private final PaletteProviderManager paletteManager;
	private final PaletteType paletteType;
	protected boolean paletteOnly;

	private Component parent;
	private String title;
	private PaletteType type;
	protected Palette initialPalette;
	protected Color initialColor;

	// private Palette selectedPalette;
	// private Color selectedColor;
	private int colorCount;
	private ColorPaletteProviderPanel[] palettePanels;

  /**
   * True if OK was pressed; false otherwise.
   */
  protected boolean okWasPressed = false;

  /**
   * OK Action Listener
   */
  protected ActionListener okListener = null;

  /**
   * Cancel Action Listener
   */
  protected ActionListener cancelListener = null;


	/**
	 * The inner panel containing everything.
	 */
	protected JPanel innerPanel = null;

	/**
	 * The style editor panel.
	 */
	protected JColorChooser colorChooser = null;

	public CyColorPaletteChooserImpl(final PaletteProviderManager paletteManager, final PaletteType type, boolean paletteOnly) {
		this.paletteOnly = paletteOnly;
		this.paletteManager = paletteManager;
		this.paletteType = type;
	}

	@Override
  public Color showDialog(final Component parent, final String title, 
                          final Palette initialPalette, final Color initialColor, int colorCount) {

		paletteOnly = false;
		init(parent, title, initialPalette, initialColor, colorCount);
		if (showDialog())
			return getSelectedColor();

		return initialColor;
	}

	@Override
  public Palette showDialog(final Component parent, final String title,
                            final Palette initialPalette, int colorCount) {

		paletteOnly = true;
		init(parent, title, initialPalette, null, colorCount);
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

	private void init(final Component parent, final String title, 
                    final Palette initialPalette, final Color initialColor, int colorCount) {
		this.parent = parent;
		this.title = title;
		this.initialPalette = initialPalette;
		this.initialColor = initialColor;
		this.colorCount = colorCount;
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);


		// UI configuration
		this.setTitle(title);
		this.setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );

		final Container pane = this.getContentPane();
		pane.setLayout( new BorderLayout( ) );

		// Inner panel
		this.innerPanel = new JPanel( );
		this.innerPanel.setLayout( new BorderLayout( ) );
		this.innerPanel.setBorder( new CompoundBorder(
			new BevelBorder( BevelBorder.LOWERED ),
			new EmptyBorder( new Insets( 10, 10, 10, 10 ) ) ) );
		pane.add(this.innerPanel, BorderLayout.CENTER);

		// Colors Tab
		final JPanel colorsTab = new JPanel( );
		colorsTab.setBorder(new EmptyBorder(10, 10, 10, 10));
		//colorsTab.setLayout( new GridLayout(3, 1, 5, 0) );
		colorsTab.setLayout(new BoxLayout(colorsTab, BoxLayout.Y_AXIS));
		this.innerPanel.add(colorsTab);

		// set custom colorSelectionModel
		ColorPanelSelectionModel model = new ColorPanelSelectionModel();
		this.colorChooser = new JColorChooser(model);
		if (initialPalette != null)
			model.setPalette(initialPalette);

		if (initialColor != null)
			model.setSelectedColor(initialColor);

		// Get the list of palettes
		List<PaletteProvider> providers = getPaletteProviders(false);
		palettePanels = getPanels(providers);

		// overwrite the color chooser panels
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

		colorsTab.add( this.colorChooser, BorderLayout.CENTER );

		// color blind friendly checkbox
		final JPanel cbFriendlyPanel = new JPanel( );
		cbFriendlyPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		cbFriendlyPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		colorsTab.add(cbFriendlyPanel, BorderLayout.SOUTH);

		final JPanel cbFriendlyGridPanel = new JPanel( );

		cbFriendlyPanel.add(cbFriendlyGridPanel);

		final JCheckBox colorBlindOnly = new JCheckBox("show only colorblind-friendly");
		colorBlindOnly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox source = (JCheckBox) e.getSource();
				for (ColorBlindAwareColorChooserPanel cbccp : palettePanels) {

					cbccp.setShowColorBlindSafe(source.isSelected());

					cbccp.updateChooser();

					colorChooser.repaint();

				}
			}
		});
		cbFriendlyGridPanel.add(colorBlindOnly);

		// OK, Cancel, and Reset buttons
		final JPanel buttonPanel = new JPanel( );
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		colorsTab.add(buttonPanel, BorderLayout.CENTER);

		final JPanel buttonGridPanel = new JPanel( );
		buttonGridPanel.setLayout(new GridLayout(1, 3, 5, 0));
		buttonPanel.add(buttonGridPanel);


		// Reset
		final JButton resetButton = new JButton( "Reset" );
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {

				setPalette(CyColorPaletteChooserImpl.this.initialPalette);
				setColor(CyColorPaletteChooserImpl.this.initialColor);
				for (ColorBlindAwareColorChooserPanel cbccp : palettePanels) {
					cbccp.setSelectedPalette(CyColorPaletteChooserImpl.this.initialPalette.getName());
					cbccp.updateChooser();
					colorChooser.repaint();
				}
			}
		});
		buttonGridPanel.add( resetButton );

		// OK
		final JButton okButton = new JButton( "OK" );
		okButton.setDefaultCapable(true);
		this.getRootPane( ).setDefaultButton(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				CyColorPaletteChooserImpl.this.okWasPressed = true;
				CyColorPaletteChooserImpl.this.setVisible(false);
				CyColorPaletteChooserImpl.this.initialPalette = getColorPalette();
				if (CyColorPaletteChooserImpl.this.okListener != null) 
					CyColorPaletteChooserImpl.this.okListener.actionPerformed(e);
			}
		});
		buttonGridPanel.add( okButton );

		// Cancel
		final JButton cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				setPalette(CyColorPaletteChooserImpl.this.initialPalette);
				CyColorPaletteChooserImpl.this.okWasPressed = false;
				CyColorPaletteChooserImpl.this.setVisible(false);
				if (CyColorPaletteChooserImpl.this.cancelListener != null) 
					CyColorPaletteChooserImpl.this.cancelListener.actionPerformed(e);
			}
		});
		buttonGridPanel.add( cancelButton );

		this.pack( );
		this.validate( );
	}

//----------------------------------------------------------------------
//  Methods
//----------------------------------------------------------------------
  /**
   * Shows the dialog box and waits for the user to press OK or
   * Cancel.  When either is pressed, the dialog box is hidden.
   * A true is returned if OK was pressed, and false otherwise.
   * <P>
   * This method blocks until the dialog is closed by the user,
   * regardless of whether the dialog box is modal or not.
   *
   * @return      true if OK was pressed
   */
  public boolean showDialog( )
  { 
    if ( this.isModal( ) )
    { 
      this.show( );
      return this.okWasPressed;
    }
    this.setModal( true );
    this.show( ); 
    final boolean status = this.okWasPressed;
    this.setModal( false );
    return status;
  }

  /**
   * Returns true if the OK button was pressed to close the
   * window, and false otherwise.
   *
   * @return      true if OK was pressed
   */
  public boolean wasOKPressed( )
  {
    return this.okWasPressed;
  }

  /**
   * Get the current color in the color chooser.
   *
   * @return      the current color
   */
  public Color getColor( )
  {
    return this.colorChooser.getColor( );
  }

  /**
   * Set the current color in the color chooser.
   *
   * @param color   the new color
   */
  public void setColor( final Color color )
  {
    this.colorChooser.setColor( color );
    this.initialColor = color;
  }

  /**
   * Set the current color in the color chooser.
   *
   * @param red   the red component of the new color
   * @param green   the green component of the new color
   * @param blue    the blue component of the new color
   */
  public void setColor( final int red, final int green, final int blue )
  {
    this.initialColor = new Color( red, green, blue );
    this.colorChooser.setColor( this.initialColor );
  }
  
  /**
   * Get the current color in the color chooser.
   *
   * @return      the current color
   */
  public Palette getColorPalette( )
  {
    ColorPanelSelectionModel model = (ColorPanelSelectionModel)colorChooser.getSelectionModel();
    return model.getPalette();
  }

  /**
   * Set the current color in the color chooser.
   *
   * @param color   the new color
   */
  public void setPalette(Palette palette)
  {
    if (palette == null) palette = initialPalette;
    ColorPanelSelectionModel model = (ColorPanelSelectionModel)colorChooser.getSelectionModel();
    model.setPalette(palette);
    initialPalette = palette;
  }

	private List<PaletteProvider> getPaletteProviders(boolean colorBlindOnly) {
		return paletteManager.getPaletteProviders(paletteType, colorBlindOnly);
	}

	private ColorPaletteProviderPanel[] getPanels(List<PaletteProvider> providers) {
		ColorPaletteProviderPanel[] panels = new ColorPaletteProviderPanel[providers.size()];
		int i = 0;
		for (PaletteProvider provider: providers) {
			panels[i++] = new ColorPaletteProviderPanel(provider, paletteType, paletteOnly);
		}
		return panels;
	}
}
