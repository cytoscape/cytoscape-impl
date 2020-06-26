package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Paint;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.util.swing.LookAndFeelUtil;
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
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

	private JCheckBox lineColorCheck;
	private ColorButton lineColorButton;
	private JSlider lineOpacitySlider;
	private JComboBox<String> lineWidthCombo;
	private ArrowPanel sourceArrowPanel;
	private ArrowPanel targetArrowPanel;

	public ArrowAnnotationEditor(AnnotationFactory<ArrowAnnotation> factory, CyServiceRegistrar serviceRegistrar) {
		super(factory, serviceRegistrar);
	}

	@Override
	protected void update() {
		if (annotation != null) {
			// Line Color
			var lineColor = (Color) annotation.getLineColor();
			getLineColorCheck().setSelected(lineColor != null); // TODO check enabling related fields 
			getLineColorButton().setColor(lineColor);
			
			// Line Opacity
			int opacity = getOpacity(lineColor);
			
			if (opacity != 100 || getLineColorCheck().isSelected())
				getLineOpacitySlider().setValue(opacity);
			else
				getLineOpacitySlider().setEnabled(false);
			
			// Line Width
			{
				var model = getLineWidthCombo().getModel();
				int size = model.getSize();

				for (int i = 0; i < size; i++) {
					if (Integer.parseInt((String) model.getElementAt(i)) == (int) annotation.getLineWidth()) {
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
			if (getLineColorCheck().isSelected())
				annotation.setLineColor(mixColor(getLineColorButton().getColor(), getLineOpacitySlider().getValue()));
			else
				annotation.setLineColor(null);

			// Line Width
			annotation.setLineWidth(Integer.parseInt((String) getLineWidthCombo().getModel().getSelectedItem()));

			// Arrows
			getSourceArrowPanel().apply();
			getTargetArrowPanel().apply();
		}
	}

	@Override
	protected void init() {
		getSourceArrowPanel().init();
		getTargetArrowPanel().init();
		
		// TODO disable labels too
		var label1 = new JLabel("Line Color:");
		var label2 = new JLabel("Line Opacity:");
		var label3 = new JLabel("Line Width:");

		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
				.addGroup(layout.createSequentialGroup()
						.addGap(10, 20, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(TRAILING, true)
								.addComponent(label1)
								.addComponent(label2)
								.addComponent(label3)
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addGroup(layout.createSequentialGroup()
										.addComponent(getLineColorCheck(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(getLineColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addComponent(getLineOpacitySlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(getLineWidthCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addGap(10, 20, Short.MAX_VALUE)
				)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getSourceArrowPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(getTargetArrowPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(label1)
						.addComponent(getLineColorCheck())
						.addComponent(getLineColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(label2)
						.addComponent(getLineOpacitySlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(label3)
						.addComponent(getLineWidthCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(LEADING, true)
						.addComponent(getSourceArrowPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getTargetArrowPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
		
		makeSmall(label1, label2, label3);
		makeSmall(getLineColorCheck(), getLineColorButton(), getLineOpacitySlider(), getLineWidthCombo());
	}

	private JCheckBox getLineColorCheck() {
		if (lineColorCheck == null) {
			lineColorCheck = new JCheckBox();
			lineColorCheck.setSelected(false);
			lineColorCheck.addActionListener(evt -> {
				getLineColorButton().setEnabled(lineColorCheck.isSelected());
				getLineOpacitySlider().setEnabled(lineColorCheck.isSelected());
				apply();
			});
		}
		
		return lineColorCheck;
	}

	private ColorButton getLineColorButton() {
		if (lineColorButton == null) {
			lineColorButton = new ColorButton(Color.BLACK);
			lineColorButton.setToolTipText("Select line color...");
			lineColorButton.setEnabled(false);
			lineColorButton.addPropertyChangeListener("color", evt -> apply());
		}
		
		return lineColorButton;
	}

	private JSlider getLineOpacitySlider() {
		if (lineOpacitySlider == null) {
			lineOpacitySlider = new JSlider(0, 100, 100);
			lineOpacitySlider.setMajorTickSpacing(100);
			lineOpacitySlider.setMinorTickSpacing(25);
			lineOpacitySlider.setPaintTicks(true);
			lineOpacitySlider.setPaintLabels(true);
			lineOpacitySlider.setEnabled(false);
			lineOpacitySlider.addChangeListener(evt -> apply());
		}
		
		return lineOpacitySlider;
	}
	
	private JComboBox<String> getLineWidthCombo() {
		if (lineWidthCombo == null) {
			lineWidthCombo = new JComboBox<>();
			lineWidthCombo.setModel(new DefaultComboBoxModel<>(
					new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
			lineWidthCombo.setSelectedIndex(1);
			lineWidthCombo.addActionListener(evt -> apply());
		}
		
		return lineWidthCombo;
	}
	
	private ArrowPanel getSourceArrowPanel() {
		if (sourceArrowPanel == null) {
			sourceArrowPanel = new ArrowPanel(ArrowEnd.SOURCE);
		}
		
		return sourceArrowPanel;
	}
	
	private ArrowPanel getTargetArrowPanel() {
		if (targetArrowPanel == null) {
			targetArrowPanel = new ArrowPanel(ArrowEnd.TARGET);
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
		
		private JComboBox<String> arrowTypeCombo;
		private JCheckBox arrowColorCheck;
		private ColorButton arrowColorButton;
		private JSlider arrowOpacitySlider;
		private JComboBox<String> arrowSizeCombo;
		private JComboBox<String> anchorTypeCombo;
		
		private final ArrowEnd arrowEnd;

		ArrowPanel(ArrowEnd arrowEnd) {
			this.arrowEnd = arrowEnd;
		}
		
		void update() {
			if (annotation != null) {
				var arrows = annotation.getSupportedArrows();
				getArrowTypeCombo().setModel(new DefaultComboBoxModel<>(arrows.toArray(new String[arrows.size()])));
				getArrowTypeCombo().setSelectedItem(annotation.getArrowType(arrowEnd));
				
				var arrowColor = (Color) annotation.getArrowColor(arrowEnd);
				getArrowColorCheck().setSelected(arrowColor != null);
				
				getArrowColorButton().setColor(arrowColor);
				getArrowColorButton().setEnabled(getArrowColorCheck().isSelected());	
				
				int opacity = getOpacity(arrowColor);
				
				if (opacity != 100 || getArrowColorCheck().isSelected())
					getArrowOpacitySlider().setValue(opacity);
				else
					getArrowOpacitySlider().setEnabled(false);

				{
					var model = getArrowSizeCombo().getModel();

					for (int i = 0; i < model.getSize(); i++) {
						int size = Integer.parseInt((String) model.getElementAt(i));
						
						if (size == (int) annotation.getArrowSize(arrowEnd)) {
							getArrowSizeCombo().setSelectedIndex(i);
							break;
						}
					}
				}
				
				getAnchorTypeCombo().setSelectedIndex(annotation.getAnchorType(arrowEnd) == AnchorType.CENTER ? 1 : 0);
			}
		}
		
		void apply() {
			if (annotation != null) {
				annotation.setArrowType(arrowEnd, (String) getArrowTypeCombo().getSelectedItem());
				
				if (getArrowColorCheck().isSelected())
					annotation.setArrowColor(arrowEnd,
							mixColor(getArrowColorButton().getColor(), getArrowOpacitySlider().getValue()));
				else
					annotation.setArrowColor(arrowEnd, null);
				
				annotation.setArrowSize(arrowEnd,
						Integer.parseInt(getArrowSizeCombo().getModel().getSelectedItem().toString()));
				
				if (getAnchorTypeCombo().getModel().getSelectedItem().equals("Center"))
					annotation.setAnchorType(arrowEnd, AnchorType.CENTER);
				else
					annotation.setAnchorType(arrowEnd, AnchorType.ANCHOR);
			}
		}
		
		private void init() {
			setBorder(LookAndFeelUtil.createTitledBorder(arrowEnd == ArrowEnd.TARGET ? "Target Arrow" : "Source Arrow"));

			var label1 = new JLabel("Shape:");
			var label2 = new JLabel("Color:");
			var label3 = new JLabel("Opacity:");
			var label4 = new JLabel("Size:");
			var label5 = new JLabel("Anchor:");
			
			if (LookAndFeelUtil.isAquaLAF())
				setOpaque(false);
			
			var layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(TRAILING, true)
							.addComponent(label1)
							.addComponent(label2)
							.addComponent(label3)
							.addComponent(label4)
							.addComponent(label5)
					)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(getArrowTypeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addGroup(layout.createSequentialGroup()
									.addComponent(getArrowColorCheck(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									.addComponent(getArrowColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							)
							.addComponent(getArrowOpacitySlider(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getArrowSizeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getAnchorTypeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(label1)
							.addComponent(getArrowTypeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(label2)
							.addComponent(getArrowColorCheck(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getArrowColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(LEADING, false)
							.addComponent(label3)
							.addComponent(getArrowOpacitySlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(label4)
							.addComponent(getArrowSizeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(label5)
							.addComponent(getAnchorTypeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			
			makeSmall(label1, label2, label3, label4, label5);
			makeSmall(getArrowTypeCombo(), getArrowColorCheck(), getArrowColorButton(), getArrowOpacitySlider(),
					getArrowSizeCombo(), getAnchorTypeCombo());
		}
		
		private JComboBox<String> getArrowTypeCombo() {
			if (arrowTypeCombo == null) {
				arrowTypeCombo = new JComboBox<>();
				arrowTypeCombo.addActionListener(evt -> apply());
			}
			
			return arrowTypeCombo;
		}
		
		private JCheckBox getArrowColorCheck() {
			if (arrowColorCheck == null) {
				arrowColorCheck = new JCheckBox();
				arrowColorCheck.addActionListener(evt -> {
					getArrowColorButton().setEnabled(arrowColorCheck.isSelected());
					getArrowOpacitySlider().setEnabled(arrowColorCheck.isSelected());
					apply();
				});
			}
			
			return arrowColorCheck;
		}
		
		private ColorButton getArrowColorButton() {
			if (arrowColorButton == null) {
				arrowColorButton = new ColorButton(Color.BLACK);
				arrowColorButton.setToolTipText("Select arrow color...");
				arrowColorButton.setEnabled(false);
				arrowColorButton.addPropertyChangeListener("color", evt -> apply());
			}
			
			return arrowColorButton;
		}
		
		private JSlider getArrowOpacitySlider() {
			if (arrowOpacitySlider == null) {
				arrowOpacitySlider = new JSlider(0, 100, 100);
				arrowOpacitySlider.setMajorTickSpacing(100);
				arrowOpacitySlider.setMinorTickSpacing(25);
				arrowOpacitySlider.setPaintTicks(true);
				arrowOpacitySlider.setPaintLabels(true);
				arrowOpacitySlider.setEnabled(false);
				arrowOpacitySlider.addChangeListener(evt -> apply());
			}
			
			return arrowOpacitySlider;
		}
		
		private JComboBox<String> getArrowSizeCombo() {
			if (arrowSizeCombo == null) {
				arrowSizeCombo = new JComboBox<>();
				arrowSizeCombo.setModel(new DefaultComboBoxModel<>(
						new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", 
								       "11", "12", "13", "14", "15", "16", "17", "18", "19", "20" }
				));
				arrowSizeCombo.setSelectedIndex(1);
				arrowSizeCombo.addActionListener(evt -> apply());
			}
			
			return arrowSizeCombo;
		}

		private JComboBox<String> getAnchorTypeCombo() {
			if (anchorTypeCombo == null) {
				anchorTypeCombo = new JComboBox<>();
				anchorTypeCombo.setModel(new DefaultComboBoxModel<>(new String[] { "Edge", "Center" }));
				anchorTypeCombo.addActionListener(evt -> apply());
			}
			
			return anchorTypeCombo;
		}
	}
}
