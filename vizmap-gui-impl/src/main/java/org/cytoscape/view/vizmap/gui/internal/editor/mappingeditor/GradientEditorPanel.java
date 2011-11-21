/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor;

import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Color Gradient Mapping editor.
 * 
 */
public class GradientEditorPanel extends
		ContinuousMappingEditorPanel<Double, Color> implements
		PropertyChangeListener {

	private static final Logger logger = LoggerFactory
			.getLogger(GradientEditorPanel.class);

	private final static long serialVersionUID = 1202339877433771L;

	// Preset colors
	private static final Color DEF_LOWER_COLOR = Color.BLACK;
	private static final Color DEF_UPPER_COLOR = Color.WHITE;

	// For updating current network views.
	private final CyApplicationManager appManager;
	private final ValueEditor<Paint> colorEditor;

	private CyGradientTrackRenderer gRend;

	/**
	 * Creates a new GradientEditorPanel object.
	 * 
	 * @param type
	 *            DOCUMENT ME!
	 */
	public GradientEditorPanel(final VisualStyle style,
			final ContinuousMapping<Double, Color> mapping, final CyTable attr,
			final CyApplicationManager appManager,
			final ValueEditor<Paint> colorEditor, final VisualMappingManager vmm) {

		super(style, mapping, attr, appManager, vmm);

		if (colorEditor == null)
			throw new NullPointerException("Color Value Editor is missing.");

		this.colorEditor = colorEditor;
		this.appManager = appManager;

		iconPanel.setVisible(false);
		initSlider();

		belowPanel.addPropertyChangeListener(this);
		abovePanel.addPropertyChangeListener(this);

		if ((mapping != null) && (mapping.getPointCount() == 0))
			addButtonActionPerformed(null);
	}

//	public ImageIcon getLegend(final int width, final int height) {
//		final CyGradientTrackRenderer rend = (CyGradientTrackRenderer) slider
//				.getTrackRenderer();
//		rend.getRendererComponent(slider);
//
//		return rend.getLegend(width, height);
//	}
//
//	public ImageIcon getIcon(final int iconWidth, final int iconHeight) {
//		final CyGradientTrackRenderer rend = (CyGradientTrackRenderer) slider
//				.getTrackRenderer();
//		rend.getRendererComponent(slider);
//
//		return rend.getTrackGraphicIcon(iconWidth, iconHeight);
//	}

	@Override
	protected void addButtonActionPerformed(ActionEvent evt) {

		final BoundaryRangeValues<Color> lowerRange;

		double maxValue = tracer.getMax(type);

		// Currently, there is no mapping.
		if (mapping.getPointCount() == 0) {
			Number rangeValue = tracer.getRange(type);
			Number minValue = tracer.getMin(type);

			final BoundaryRangeValues<Color> upperRange;

			slider.getModel().addThumb(10f, DEF_LOWER_COLOR);
			slider.getModel().addThumb(90f, DEF_UPPER_COLOR);

			lowerRange = new BoundaryRangeValues<Color>(below, DEF_LOWER_COLOR,
					DEF_LOWER_COLOR);
			upperRange = new BoundaryRangeValues<Color>(DEF_UPPER_COLOR,
					DEF_UPPER_COLOR, above);

			// Add two points.
			mapping.addPoint(
					((Double) (rangeValue.doubleValue() * 0.1) + minValue
							.doubleValue()), lowerRange);
			mapping.addPoint(
					(rangeValue.doubleValue() * 0.9) + minValue.doubleValue(),
					upperRange);

			slider.repaint();
			repaint();

			// Update view.
			style.apply(appManager.getCurrentNetworkView());
			appManager.getCurrentNetworkView().updateView();

			return;
		}

		// Add a new white thumb in the min.
		slider.getModel().addThumb(100f, Color.white);

		// Pick Up first point.
		final ContinuousMappingPoint<Double, Color> previousPoint = mapping.getPoint(mapping.getPointCount() - 1);
		final BoundaryRangeValues<Color> previousRange = previousPoint.getRange();

		Color lesserVal = slider.getModel().getSortedThumbs()
		                    .get(slider.getModel().getThumbCount() - 1).getObject();
		Color equalVal = Color.white;
		Color greaterVal = previousRange.greaterValue;

		lowerRange = new BoundaryRangeValues<Color>(lesserVal, equalVal, greaterVal);

		mapping.addPoint(maxValue, lowerRange);

		updateMap();

		appManager.getCurrentNetworkView().updateView();
		slider.repaint();
		repaint();
	}

	@Override
	protected void deleteButtonActionPerformed(ActionEvent evt) {
		final int selectedIndex = slider.getSelectedIndex();

		if (0 <= selectedIndex) {
			slider.getModel().removeThumb(selectedIndex);
			mapping.removePoint(selectedIndex);
			updateMap();
			appManager.getCurrentNetworkView().updateView();
			repaint();
		}
	}

	private void setColor(final Color newColor) {

		logger.debug("Set color called: New color = " + newColor);

		slider.getModel().getThumbAt(slider.getSelectedIndex())
				.setObject(newColor);

		int selected = getSelectedPoint(slider.getSelectedIndex());

		Color lesserVal = mapping.getPoint(selected).getRange().lesserValue;
		Color equalVal = newColor;
		Color greaterVal = mapping.getPoint(selected).getRange().greaterValue;

		int numPoints = mapping.getAllPoints().size();
		if (numPoints > 1) {
			if (selected == 0)
				greaterVal = newColor;
			else if (selected == (numPoints - 1))
				lesserVal = newColor;
			else {
				lesserVal = newColor;
				greaterVal = newColor;
			}
		}

		final BoundaryRangeValues<Color> brv = new BoundaryRangeValues<Color>(lesserVal, newColor, greaterVal);

		mapping.getPoint(selected).setRange(brv);

		if (numPoints > 1) {
			style.apply(appManager.getCurrentNetworkView());
			appManager.getCurrentNetworkView().updateView();
			slider.repaint();
		}
	}

	/**
	 * DOCUMENT ME!
	 */
	public void initSlider() {

		slider.updateUI();

		slider.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				final JComponent selectedThumb = slider.getSelectedThumb();

				if (selectedThumb != null) {
					// final Point location = selectedThumb.getLocation();
					// double diff = Math.abs(location.getX() - e.getX());
					if (e.getClickCount() == 2) {

						final Paint newColor = colorEditor.showEditor(slider,
								null);

						if (newColor != null) {
							// Set new color
							setColor((Color) newColor);
						}
					}
				}
			}
		});

		final double actualRange = tracer.getRange(type);
		final double minValue = tracer.getMin(type);

		if (allPoints != null) {
			for (final ContinuousMappingPoint<Double, Color> point : allPoints) {
				BoundaryRangeValues<Color> bound = point.getRange();

				slider.getModel()
						.addThumb(
								((Double) ((point.getValue() - minValue) / actualRange))
										.floatValue() * 100,
								(Color) bound.equalValue);
			}

			if (allPoints.size() != 0) {
				below = (Color) allPoints.get(0).getRange().lesserValue;
				above = (Color) allPoints.get(allPoints.size() - 1).getRange().greaterValue;
			} else {
				below = Color.black;
				above = Color.white;
			}

			setSidePanelIconColor((Color) below, (Color) above);
		}

		TriangleThumbRenderer thumbRend = new TriangleThumbRenderer(slider);

		CyGradientTrackRenderer gRend = new CyGradientTrackRenderer(
				(VisualProperty<Color>) type, (Color) below, (Color) above,
				mapping.getMappingColumnName(), tracer);

		slider.setThumbRenderer(thumbRend);
		slider.setTrackRenderer(gRend);
		slider.addMouseListener(new ThumbMouseListener());

		// Add tooltip as help for users.
		slider.setToolTipText("Double-click handles to edit boundary colors.");
	}

	public void propertyChange(PropertyChangeEvent e) {

		// FIXME!!!!!!!
		if (e.getPropertyName().equals(BelowAndAbovePanel.COLOR_CHANGED)) {
			String sourceName = ((BelowAndAbovePanel) e.getSource()).getName();

			if (sourceName.equals("abovePanel"))
				this.above = (Color) e.getNewValue();
			else
				this.below = (Color) e.getNewValue();

			final CyGradientTrackRenderer gRend = new CyGradientTrackRenderer(
					(VisualProperty<Color>) type, below, above,
					mapping.getMappingColumnName(), tracer);
			slider.setTrackRenderer(gRend);

			repaint();
		}
	}

	@Override
	public ImageIcon drawIcon(int iconWidth, int iconHeight, boolean detail) {
		final CyGradientTrackRenderer rend = (CyGradientTrackRenderer) slider
				.getTrackRenderer();
		rend.getRendererComponent(slider);
		
		return rend.drawIcon(iconWidth, iconHeight, detail);
	}
}
