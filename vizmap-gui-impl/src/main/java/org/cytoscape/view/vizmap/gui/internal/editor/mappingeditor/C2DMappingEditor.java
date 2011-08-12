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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;

import javax.swing.ImageIcon;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.jdesktop.swingx.multislider.Thumb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Continuous Mapping editor for discrete values, such as Font, Shape, Label
 * Position, etc.
 * 
 */
public class C2DMappingEditor<V> extends
		ContinuousMappingEditorPanel<Number, V> {
	private final static long serialVersionUID = 1213748837197780L;

	private static final Logger logger = LoggerFactory
			.getLogger(C2DMappingEditor.class);

	private final EditorManager editorManager;

	public C2DMappingEditor(final VisualStyle style,
			final ContinuousMapping<Number, V> mapping, CyTable attr,
			final CyApplicationManager appManager,
			final VisualMappingManager vmm, final EditorManager editorManager) {
		super(style, mapping, attr, appManager, vmm);

		if (editorManager == null)
			throw new NullPointerException("Editor manager is null.");

		this.editorManager = editorManager;

		this.iconPanel.setVisible(false);
		this.belowPanel.setVisible(false);
		this.abovePanel.setVisible(false);

		setSlider();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public ImageIcon getIcon(final int iconWidth, final int iconHeight,
			VisualProperty<V> type) {

		if (slider.getTrackRenderer() instanceof DiscreteTrackRenderer == false) {
			return null;
		}

		DiscreteTrackRenderer<Number, V> rend = (DiscreteTrackRenderer<Number, V>) slider
				.getTrackRenderer();
		rend.getRendererComponent(slider);

		return new ImageIcon();
		// FIXME
		// return rend.getTrackGraphicIcon(iconWidth, iconHeight);
	}

	/**
	 * 
	 * @param width
	 * @param height
	 * @param type
	 * @return
	 */
	public ImageIcon getLegend(final int width, final int height,
			final VisualProperty<?> type) {

		if (slider.getTrackRenderer() instanceof DiscreteTrackRenderer == false) {
			return null;
		}

		DiscreteTrackRenderer<Number, V> rend = (DiscreteTrackRenderer<Number, V>) slider
				.getTrackRenderer();
		rend.getRendererComponent(slider);

		return null;
		// FIXME
		// return rend.getLegend(width, height);
	}

	@Override
	protected void addButtonActionPerformed(ActionEvent evt) {
		BoundaryRangeValues<V> newRange;
		V defValue = type.getDefault();

		Double maxValue = tracer.getMax(type);

		if (mapping.getPointCount() == 0) {
			slider.getModel().addThumb(50f, defValue);

			newRange = new BoundaryRangeValues<V>(below, defValue, above);
			mapping.addPoint(maxValue / 2, newRange);
			slider.repaint();
			repaint();

			return;
		}

		// Add a new thumb with default value
		slider.getModel().addThumb(100f, defValue);

		// Pick Up first point.
		final ContinuousMappingPoint<Number, V> previousPoint = mapping
				.getPoint(mapping.getPointCount() - 1);

		final BoundaryRangeValues<V> previousRange = previousPoint.getRange();

		V lesserVal = slider.getModel().getSortedThumbs()
		                    .get(slider.getModel().getThumbCount() - 1).getObject();
		V equalVal = defValue;
		V greaterVal = previousRange.greaterValue;

		newRange = new BoundaryRangeValues<V>(lesserVal, equalVal, greaterVal);

		mapping.addPoint(maxValue, newRange);

		updateMap();

		slider.repaint();
		repaint();
	}

	protected void updateMap() {
		// FIXME
		List<Thumb<V>> thumbs = slider.getModel().getSortedThumbs();

		final double minValue = tracer.getMin(type);
		final double valRange = tracer.getRange(type);

		// List<ContinuousMappingPoint> points = mapping.getAllPoints();
		Thumb<V> t;
		Double newVal;

		if (thumbs.size() == 1) {
			// Special case: only one handle.
			mapping.getPoint(0).setRange(new BoundaryRangeValues<V>(below, below, above));
			newVal = ((thumbs.get(0).getPosition() / 100) * valRange) + minValue;
			mapping.getPoint(0).setValue(newVal);

			return;
		}

		for (int i = 0; i < thumbs.size(); i++) {
			t = thumbs.get(i);

			V lesserVal;
			V equalVal;
			V greaterVal;

			if (i == 0) {
				// First thumb
				lesserVal = below;
				equalVal = below;
				greaterVal = thumbs.get(i + 1).getObject();
			} else if (i == (thumbs.size() - 1)) {
				// Last thumb
				greaterVal = above;
				equalVal = t.getObject();
				lesserVal = t.getObject();
			} else {
				// Others
				lesserVal = t.getObject();
				equalVal = t.getObject();
				greaterVal = thumbs.get(i + 1).getObject();
			}
			mapping.getPoint(i).setRange( new BoundaryRangeValues<V>(lesserVal, equalVal, greaterVal) );

			newVal = ((t.getPosition() / 100) * valRange) + minValue;
			mapping.getPoint(i).setValue(newVal);
		}
	}

	@Override
	protected void deleteButtonActionPerformed(ActionEvent evt) {
		final int selectedIndex = slider.getSelectedIndex();

		if (0 <= selectedIndex) {
			slider.getModel().removeThumb(selectedIndex);
			mapping.removePoint(selectedIndex);
			updateMap();
			// mapping.fireStateChanged();

			// Cytoscape.redrawGraph(vmm.getNetworkView());
			repaint();
		}
	}

	private void setSlider() {
		Dimension dim = new Dimension(600, 100);
		setPreferredSize(dim);
		setSize(dim);
		setMinimumSize(new Dimension(300, 80));
		slider.updateUI();

		final double minValue = tracer.getMin(type);
		final double maxValue = tracer.getMax(type);

		final C2DMappingEditor<V> parentComponent = this;
		slider.addMouseListener(new MouseAdapter() {

			// Handle value icon click.
			public void mouseClicked(MouseEvent e) {
				int range = ((DiscreteTrackRenderer<Number, V>) slider
						.getTrackRenderer()).getRangeID(e.getX(), e.getY());

				V newValue = null;

				if (e.getClickCount() == 2) {
					try {
						// setAlwaysOnTop(false);
						newValue = editorManager.showVisualPropertyValueEditor(
								parentComponent, type, null);
					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {
						// setAlwaysOnTop(true);
					}

					if (newValue == null)
						return;

					if (range == 0) {
						below = newValue;
					} else if (range == slider.getModel().getThumbCount()) {
						above = newValue;
					} else {
						slider.getModel().getSortedThumbs().get(range)
								.setObject(newValue);
					}

					updateMap();

					slider.setTrackRenderer(new DiscreteTrackRenderer<Number, V>(
							mapping, below, above, tracer, appManager
									.getCurrentRenderingEngine()));
					slider.repaint();

					// Update network
					style.apply(appManager.getCurrentNetworkView());
					appManager.getCurrentNetworkView().updateView();
				}
			}
		});

		double actualRange = tracer.getRange(type);

		BoundaryRangeValues<V> bound;
		Float fraction;

		/*
		 * NPE?
		 */
		if (allPoints == null) {
			return;
		}

		for (ContinuousMappingPoint<Number, V> point : allPoints) {
			bound = point.getRange();

			fraction = ((Number) ((point.getValue().doubleValue() - minValue) / actualRange))
					.floatValue() * 100;
			slider.getModel().addThumb(fraction, bound.equalValue);
		}

		if (allPoints.size() != 0) {
			below = allPoints.get(0).getRange().lesserValue;
			above = allPoints.get(allPoints.size() - 1).getRange().greaterValue;
		} else {
			V defaultVal = type.getDefault();
			below = defaultVal;
			above = defaultVal;
		}

		/*
		 * get min and max for the value object
		 */
		TriangleThumbRenderer thumbRend = new TriangleThumbRenderer(slider);
		DiscreteTrackRenderer<Number, V> dRend = new DiscreteTrackRenderer<Number, V>(
				mapping, below, above, tracer,
				appManager.getCurrentRenderingEngine());

		slider.setThumbRenderer(thumbRend);
		slider.setTrackRenderer(dRend);
		slider.addMouseListener(new ThumbMouseListener());
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public ImageIcon drawIcon(int iconWidth, int iconHeight, boolean detail) {
		DiscreteTrackRenderer<Number, V> rend = (DiscreteTrackRenderer<Number, V>) slider
				.getTrackRenderer();
		rend.getRendererComponent(slider);
		
		return rend.getTrackGraphicIcon(iconWidth, iconHeight);
	}
}
