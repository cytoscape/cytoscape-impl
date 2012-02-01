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
import org.cytoscape.view.model.CyNetworkView;
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
public class C2DMappingEditorPanel<V> extends ContinuousMappingEditorPanel<Number, V> {
	
	private final static long serialVersionUID = 1213748837197780L;

	private static final Logger logger = LoggerFactory.getLogger(C2DMappingEditorPanel.class);

	private final EditorManager editorManager;

	public C2DMappingEditorPanel(final VisualStyle style, final ContinuousMapping<Number, V> mapping, CyTable attr,
			final CyApplicationManager appManager, final VisualMappingManager vmm, final EditorManager editorManager) {
		super(style, mapping, attr, appManager, vmm);

		this.editorManager = editorManager;

		this.iconPanel.setVisible(false);
		this.belowPanel.setVisible(false);
		this.abovePanel.setVisible(false);
		
		initSlider();
	}

	
	private void updateView() {
		final CyNetworkView curView = appManager.getCurrentNetworkView();
		style.apply(curView);
		curView.updateView();
	}


	@Override
	protected void addButtonActionPerformed(ActionEvent evt) {
		BoundaryRangeValues<V> newRange;
		
		final V defValue = type.getDefault();
		final Double maxValue = tracer.getMax(type);
		final Float ratio;

		if (mapping.getPointCount() == 0) {
			ratio = 50f;
			// Add new slider at center
			slider.getModel().addThumb(ratio, defValue);
			newRange = new BoundaryRangeValues<V>(below, defValue, above);
			
		} else {
			ratio = 70f;
			// Add a new thumb with default value
			slider.getModel().addThumb(ratio, defValue);

			// Pick Up first point.
			final ContinuousMappingPoint<Number, V> previousPoint = mapping.getPoint(mapping.getPointCount() - 1);
			final BoundaryRangeValues<V> previousRange = previousPoint.getRange();

			V lesserVal = slider.getModel().getSortedThumbs().get(slider.getModel().getThumbCount() - 1).getObject();
			V equalVal = defValue;
			V greaterVal = previousRange.greaterValue;

			newRange = new BoundaryRangeValues<V>(lesserVal, equalVal, greaterVal);
		}

		mapping.addPoint(maxValue*(ratio/100), newRange);
		updateMap();

		slider.repaint();
		repaint();
		
		updateView();
	}

	
	@Override
	protected void updateMap() {
		// FIXME
		final List<Thumb<V>> thumbs = slider.getModel().getSortedThumbs();

		final double minValue = tracer.getMin(type);
		final double valRange = tracer.getRange(type);

		Thumb<V> t;
		Double newPosition;

		if (thumbs.size() == 1) {
			// Special case: only one handle.
			mapping.getPoint(0).setRange(new BoundaryRangeValues<V>(below, below, above));
			newPosition = ((thumbs.get(0).getPosition() / 100) * valRange) + minValue;
			mapping.getPoint(0).setValue(newPosition);
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
			mapping.getPoint(i).setRange(new BoundaryRangeValues<V>(lesserVal, equalVal, greaterVal));

			newPosition = ((t.getPosition() / 100) * valRange) + minValue;
			mapping.getPoint(i).setValue(newPosition);
		}
	}

	@Override
	protected void deleteButtonActionPerformed(ActionEvent evt) {
		final int selectedIndex = slider.getSelectedIndex();

		if (0 <= selectedIndex) {
			slider.getModel().removeThumb(selectedIndex);
			mapping.removePoint(selectedIndex);
			updateMap();
			repaint();
			
			updateView();
		}
	}

	private void initSlider() {
		Dimension dim = new Dimension(600, 100);
		setPreferredSize(dim);
		setSize(dim);
		setMinimumSize(new Dimension(300, 80));
		slider.updateUI();

		final double minValue = tracer.getMin(type);
		final double maxValue = tracer.getMax(type);

		final C2DMappingEditorPanel<V> parentComponent = this;
		slider.addMouseListener(new MouseAdapter() {

			// Handle value icon click.
			public void mouseClicked(MouseEvent e) {
				int range = ((DiscreteTrackRenderer<Number, V>) slider.getTrackRenderer()).getRangeID(e.getX(),
						e.getY());

				V newValue = null;

				if (e.getClickCount() == 2) {
					try {
						// setAlwaysOnTop(false);
						newValue = editorManager.showVisualPropertyValueEditor(parentComponent, type, null);
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
						slider.getModel().getSortedThumbs().get(range).setObject(newValue);
					}

					updateMap();

					slider.setTrackRenderer(new DiscreteTrackRenderer<Number, V>(mapping, below, above, tracer,
							appManager.getCurrentRenderingEngine()));
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

			fraction = ((Number) ((point.getValue().doubleValue() - minValue) / actualRange)).floatValue() * 100;
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
		TriangleThumbRenderer thumbRend = new TriangleThumbRenderer();
		DiscreteTrackRenderer<Number, V> dRend = new DiscreteTrackRenderer<Number, V>(mapping, below, above, tracer,
				appManager.getCurrentRenderingEngine());

		slider.setThumbRenderer(thumbRend);
		slider.setTrackRenderer(dRend);
		slider.addMouseListener(new ThumbMouseListener());
	}

	@Override
	public void propertyChange(PropertyChangeEvent pce) {}

	@Override
	public ImageIcon drawIcon(int iconWidth, int iconHeight, boolean detail) {
		DiscreteTrackRenderer<Number, V> rend = (DiscreteTrackRenderer<Number, V>) slider.getTrackRenderer();
		rend.getRendererComponent(slider);

		return rend.getTrackGraphicIcon(iconWidth, iconHeight);
	}
	
	public ImageIcon getLegend(final int width, final int height) {

		if (slider.getTrackRenderer() instanceof DiscreteTrackRenderer == false)
			return null;

		DiscreteTrackRenderer<Number, V> rend = (DiscreteTrackRenderer<Number, V>) slider.getTrackRenderer();
		rend.getRendererComponent(slider);

		return rend.getLegend(width, height);
	}
}
