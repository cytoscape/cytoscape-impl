package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl.ArrowType;
import org.cytoscape.ding.impl.cyannotator.annotations.GraphicsUtilities;
import org.cytoscape.ding.impl.cyannotator.utils.EnhancedSlider;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation.AnchorType;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation.ArrowEnd;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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
public class ArrowAnnotationEditor extends AbstractAnnotationEditor<ArrowAnnotation> {

	private JLabel lineWidthLabel;
	private JLabel lineColorLabel;
	private JLabel lineOpacityLabel;
	
	private ColorButton lineColorButton;
	private EnhancedSlider lineOpacitySlider;
	private JComboBox<Integer> lineWidthCombo;
	private JPanel linePanel;
	private JPanel arrowsPanel;
	private ArrowPanel sourceArrowPanel;
	private ArrowPanel targetArrowPanel;

	public ArrowAnnotationEditor(AnnotationFactory<ArrowAnnotation> factory, CyServiceRegistrar serviceRegistrar) {
		super(factory, serviceRegistrar);
	}

	@Override
	protected void doUpdate() {
		if (annotation != null) {
			// Line Color
			var lineColor = (Color) annotation.getLineColor();
			getLineColorButton().setColor(lineColor);
			
			// Line Opacity
			int opacity = getOpacity(lineColor);
			getLineOpacitySlider().setValue(opacity);
			
			// Line Width
			{
				int lineWidth = Math.max(1, (int) Math.round(annotation.getLineWidth()));
				var model = getLineWidthCombo().getModel();
				int size = model.getSize();

				for (int i = 0; i < size; i++) {
					if (lineWidth == model.getElementAt(i)) {
						getLineWidthCombo().setSelectedIndex(i);
						break;
					}
				}
			}
		}
		
		getSourceArrowPanel().update();
		getTargetArrowPanel().update();
	}

	@Override
	public void apply(ArrowAnnotation annotation) {
		if (annotation != null) {
			// Line Color and Opacity
			annotation.setLineColor(mixColor(getLineColorButton().getColor(), getLineOpacitySlider().getValue()));

			// Line Width
			annotation.setLineWidth((int) getLineWidthCombo().getSelectedItem());

			// Arrows
			getSourceArrowPanel().apply(annotation);
			getTargetArrowPanel().apply(annotation);
		}
	}

	@Override
	protected void init() {
		lineWidthLabel = new JLabel("Line Width:");
		lineColorLabel = new JLabel("Line Color:");
		lineOpacityLabel = new JLabel("Line Opacity:");
		
		getSourceArrowPanel().init();
		getTargetArrowPanel().init();
		
		var sep = new JSeparator();
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(!isAquaLAF());
		layout.setAutoCreateGaps(!isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(getLinePanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getArrowsPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getLinePanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getArrowsPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
	}

	private JPanel getLinePanel() {
		if (linePanel == null) {
			linePanel = new JPanel();
			linePanel.setOpaque(!isAquaLAF());
			
			var layout = new GroupLayout(linePanel);
			linePanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(!isAquaLAF());
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(TRAILING, false)
							.addComponent(lineWidthLabel)
							.addComponent(lineColorLabel)
							.addComponent(lineOpacityLabel)
					)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
							.addComponent(getLineWidthCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getLineColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getLineOpacitySlider(), 100, 140, 140)
					)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(lineWidthLabel)
							.addComponent(getLineWidthCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(lineColorLabel)
							.addComponent(getLineColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(LEADING, false)
							.addComponent(lineOpacityLabel)
							.addComponent(getLineOpacitySlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			
			makeSmall(lineColorLabel, lineOpacityLabel, lineWidthLabel);
			makeSmall(getLineColorButton(), getLineOpacitySlider(), getLineWidthCombo());
		}
		
		return linePanel;
	}
	
	private JPanel getArrowsPanel() {
		if (arrowsPanel == null) {
			arrowsPanel = new JPanel();
			arrowsPanel.setOpaque(!isAquaLAF());
			
			var sep = new JSeparator(JSeparator.VERTICAL);
			
			var layout = new GroupLayout(arrowsPanel);
			arrowsPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(getSourceArrowPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getTargetArrowPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(LEADING, false)
					.addComponent(getSourceArrowPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getTargetArrowPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return arrowsPanel;
	}
	
	private ColorButton getLineColorButton() {
		if (lineColorButton == null) {
			lineColorButton = new ColorButton(serviceRegistrar, null, BrewerType.ANY, Color.BLACK, false);
			lineColorButton.setToolTipText("Select line color...");
			lineColorButton.addPropertyChangeListener("color", evt -> apply());
		}
		
		return lineColorButton;
	}

	private EnhancedSlider getLineOpacitySlider() {
		if (lineOpacitySlider == null) {
			lineOpacitySlider = new EnhancedSlider(100);
			lineOpacitySlider.addChangeListener(evt -> apply());
		}
		
		return lineOpacitySlider;
	}
	
	private JComboBox<Integer> getLineWidthCombo() {
		if (lineWidthCombo == null) {
			lineWidthCombo = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
			lineWidthCombo.setSelectedIndex(0);
			lineWidthCombo.addActionListener(evt -> apply());
		}
		
		return lineWidthCombo;
	}
	
	private ArrowPanel getSourceArrowPanel() {
		if (sourceArrowPanel == null) {
			sourceArrowPanel = new ArrowPanel(ArrowEnd.SOURCE, true);
		}
		
		return sourceArrowPanel;
	}
	
	private ArrowPanel getTargetArrowPanel() {
		if (targetArrowPanel == null) {
			targetArrowPanel = new ArrowPanel(ArrowEnd.TARGET, false);
		}
		
		return targetArrowPanel;
	}

	/**
	 * @return value between 1-100
	 */
	private int getOpacity(Color c) {
		return c == null ? 100 : (int) (100 * c.getAlpha() / 255.0);
	}

	private Paint mixColor(Paint p, int value) {
		if (p == null || !(p instanceof Color))
			return p;

		var c = (Color) p;

		return new Color(c.getRed(), c.getGreen(), c.getBlue(), value * 255 / 100);
	}

	private class ArrowPanel extends JPanel {
		
		private JLabel shapeLabel = new JLabel("Shape:");
		private JLabel colorLabel = new JLabel("Color:");
		private JLabel opacityLabel = new JLabel("Opacity:");
		private JLabel sizeLabel = new JLabel("Size:");
		private JLabel anchorLabel = new JLabel("Anchor:");
		
		private JComboBox<String> arrowTypeCombo;
		private JCheckBox arrowColorCheck;
		private ColorButton arrowColorButton;
		private EnhancedSlider arrowOpacitySlider;
		private JComboBox<Integer> arrowSizeCombo;
		private JComboBox<String> anchorTypeCombo;
		
		private final ArrowEnd arrowEnd;
		private final boolean showLabels;

		ArrowPanel(ArrowEnd arrowEnd, boolean showLabels) {
			this.arrowEnd = arrowEnd;
			this.showLabels = showLabels;
		}
		
		void update() {
			if (annotation != null) {
				var arrows = new ArrayList<>(annotation.getSupportedArrows());
				// Sort arrow types and put "No Arrow" on top to make it easier for the user to find it
				arrows.sort(new Comparator<String>() {
					@Override
					public int compare(String s1, String s2) {
						if (ArrowType.NONE.getName().equalsIgnoreCase(s1))
							return -1;
						if (ArrowType.NONE.getName().equalsIgnoreCase(s2))
							return 1;
						
						return s1.compareTo(s2);
					}
				});
				
				var arrowType = annotation.getArrowType(arrowEnd);
				
				getArrowTypeCombo().setModel(new DefaultComboBoxModel<>(arrows.toArray(new String[arrows.size()])));
				getArrowTypeCombo().setSelectedItem(arrowType);
				
				var color = (Color) annotation.getArrowColor(arrowEnd);
				int opacity = getOpacity(color);
				
				{
					int arrowSize = Math.max(1, (int) Math.round(annotation.getArrowSize(arrowEnd)));
					var model = getArrowSizeCombo().getModel();

					for (int i = 0; i < model.getSize(); i++) {
						if (arrowSize == model.getElementAt(i)) {
							getArrowSizeCombo().setSelectedIndex(i);
							break;
						}
					}
				}
				
				getAnchorTypeCombo().setSelectedIndex(annotation.getAnchorType(arrowEnd) == AnchorType.CENTER ? 1 : 0);
				getArrowColorCheck().setSelected(color != null || opacity != 100);
				getArrowColorButton().setColor(color);
				getArrowOpacitySlider().setValue(opacity);
			}
			
			updateEnabled();
		}
		
		void apply(ArrowAnnotation annotation) {
			if (annotation != null) {
				var arrowType = (String) getArrowTypeCombo().getSelectedItem();
				annotation.setArrowType(arrowEnd, arrowType);
				
				if (getArrowColorCheck().isSelected())
					annotation.setArrowColor(arrowEnd,
							mixColor(getArrowColorButton().getColor(), getArrowOpacitySlider().getValue()));
				else
					annotation.setArrowColor(arrowEnd, null);
				
				annotation.setArrowSize(arrowEnd, (int) getArrowSizeCombo().getSelectedItem());
				
				var anchorType = getAnchorTypeCombo().getSelectedItem();
				annotation.setAnchorType(arrowEnd, "Center".equals(anchorType) ? AnchorType.CENTER : AnchorType.ANCHOR);
			}
		}
		
		private void init() {
			var titleLabel = new JLabel(arrowEnd == ArrowEnd.TARGET ? "Target Arrow:" : "Source Arrow:");
			makeSmall(titleLabel);
			
			shapeLabel.setVisible(showLabels);
			colorLabel.setVisible(showLabels);
			opacityLabel.setVisible(showLabels);
			sizeLabel.setVisible(showLabels);
			anchorLabel.setVisible(showLabels);
			
			if (isAquaLAF())
				setOpaque(false);
			
			var layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(!isAquaLAF());
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(TRAILING, true)
							.addGap(titleLabel.getPreferredSize().height)
							.addComponent(shapeLabel)
							.addComponent(colorLabel)
							.addComponent(opacityLabel)
							.addComponent(sizeLabel)
							.addComponent(anchorLabel)
					)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(titleLabel)
							.addComponent(getArrowTypeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addGroup(layout.createSequentialGroup()
									.addComponent(getArrowColorCheck(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									.addComponent(getArrowColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							)
							.addComponent(getArrowOpacitySlider(), 100, 140, 140)
							.addComponent(getArrowSizeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getAnchorTypeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(titleLabel)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(shapeLabel)
							.addComponent(getArrowTypeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(colorLabel)
							.addComponent(getArrowColorCheck(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getArrowColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(LEADING, false)
							.addComponent(opacityLabel)
							.addComponent(getArrowOpacitySlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(sizeLabel)
							.addComponent(getArrowSizeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(anchorLabel)
							.addComponent(getAnchorTypeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			
			makeSmall(shapeLabel, colorLabel, opacityLabel, sizeLabel, anchorLabel);
			makeSmall(getArrowTypeCombo(), getArrowColorCheck(), getArrowColorButton(), getArrowOpacitySlider(),
					getArrowSizeCombo(), getAnchorTypeCombo());
		}
		
		private JComboBox<String> getArrowTypeCombo() {
			if (arrowTypeCombo == null) {
				var typeNames = GraphicsUtilities.getSupportedArrowTypeNames();
				
				arrowTypeCombo = new JComboBox<>(new Vector<>(typeNames));
				arrowTypeCombo.setSelectedItem(
						arrowEnd == ArrowEnd.SOURCE ? ArrowType.NONE.toString() : ArrowType.OPEN.toString());
				arrowTypeCombo.setRenderer(new DefaultListCellRenderer() {
					@Override
					public Component getListCellRendererComponent(JList<?> list, Object value, int index,
							boolean isSelected, boolean cellHasFocus) {
						super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
						
						// Highlight "No Arrow" entry, to make it easier for users to remove the arrow
						if (value == null || ArrowType.NONE.getName().equalsIgnoreCase(value.toString()))
							setText("-- " + value + " --");
						
						return this;
					}
				});
				arrowTypeCombo.addActionListener(evt -> {
					updateEnabled();
					ArrowAnnotationEditor.this.apply();
				});
			}
			
			return arrowTypeCombo;
		}
		
		private JCheckBox getArrowColorCheck() {
			if (arrowColorCheck == null) {
				arrowColorCheck = new JCheckBox();
				arrowColorCheck.addActionListener(evt -> {
					updateEnabled();
					ArrowAnnotationEditor.this.apply();
				});
			}
			
			return arrowColorCheck;
		}
		
		private ColorButton getArrowColorButton() {
			if (arrowColorButton == null) {
				arrowColorButton = new ColorButton(serviceRegistrar, null, BrewerType.ANY, Color.BLACK, false);
				arrowColorButton.setToolTipText("Select arrow color...");
				arrowColorButton.addPropertyChangeListener("color", evt -> ArrowAnnotationEditor.this.apply());
			}
			
			return arrowColorButton;
		}
		
		private EnhancedSlider getArrowOpacitySlider() {
			if (arrowOpacitySlider == null) {
				arrowOpacitySlider = new EnhancedSlider(100);
				arrowOpacitySlider.addChangeListener(evt -> ArrowAnnotationEditor.this.apply());
			}
			
			return arrowOpacitySlider;
		}
		
		private JComboBox<Integer> getArrowSizeCombo() {
			if (arrowSizeCombo == null) {
				arrowSizeCombo = new JComboBox<>(
						new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 });
				arrowSizeCombo.setSelectedIndex(0);
				arrowSizeCombo.addActionListener(evt -> ArrowAnnotationEditor.this.apply());
			}
			
			return arrowSizeCombo;
		}

		private JComboBox<String> getAnchorTypeCombo() {
			if (anchorTypeCombo == null) {
				anchorTypeCombo = new JComboBox<>();
				anchorTypeCombo.setModel(new DefaultComboBoxModel<>(new String[] { "Edge", "Center" }));
				anchorTypeCombo.addActionListener(evt -> ArrowAnnotationEditor.this.apply());
			}
			
			return anchorTypeCombo;
		}
		
		private void updateEnabled() {
			var arrowType = (String) getArrowTypeCombo().getSelectedItem();
			boolean hasArrow = !ArrowType.NONE.getName().equalsIgnoreCase(arrowType);
			
			getArrowColorCheck().setEnabled(hasArrow);
			getArrowSizeCombo().setEnabled(hasArrow);
			getAnchorTypeCombo().setEnabled(hasArrow);
			
			var enabled = hasArrow && getArrowColorCheck().isSelected();
			
			getArrowColorButton().setEnabled(enabled);
			getArrowOpacitySlider().setEnabled(enabled);
		}
	}
}
