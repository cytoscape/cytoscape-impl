package org.cytoscape.ding.impl.cyannotator.utils;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.cytoscape.ding.impl.cyannotator.utils.GradientEditor.ControlPoint;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.util.color.PaletteType;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;
import org.cytoscape.util.swing.IconManager;

@SuppressWarnings("serial")
public class MultipleGradientEditor extends JPanel {
	
	public enum GradientType {
		LINEAR("Linear Gradient"),
		RADIAL("Radial Gradient");

		private String label;

		private GradientType(String label) {
			this.label = label;
		}
		
		@Override
		public String toString() {
			return label;
		}
	}
	
	private static Double[] ANGLES = new Double[] {
			-315.0, -270.0, -225.0, -180.0, -135.0, -90.0, -45.0,
            0.0,
            45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0
    };
	
	private JToggleButton linearToggle;
	private JToggleButton radialToggle;
	private ButtonGroup typeGroup = new ButtonGroup();
	
	private JButton paletteBtn;
	private JButton reverseBtn;
	private GradientEditor grEditor;
	
	private JPanel linearOptionsPnl;
	private JLabel angleLbl = new JLabel("Angle (degrees):");
	private JComboBox<Double> angleCmb;
	
	private JPanel radialOptionsPnl;
	private JLabel centerLbl = new JLabel("Center:");
	private PointPicker pointPicker;

	private Palette lastPalette;
	private Palette currentPalette;
	private PaletteType paletteType;
	
	private float[] fractions;
	private Color[] colors;
	private double angle; // Linear Gradient only
	private Point2D centerPoint = (Point2D) PointPicker.DEFAULT_VALUE.clone(); // Radial Gradient only
	
	private GradientType type;
	
	private final String targetId;
	private final CyServiceRegistrar serviceRegistrar;

	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	/**
	 * Start with the Linear Gradient editor.
	 */
	public MultipleGradientEditor(
			double angle,
			float[] fractions,
			Color[] colors,
			String targetId,
			CyServiceRegistrar serviceRegistrar
	) {
		this(GradientType.LINEAR, fractions, colors, targetId, serviceRegistrar);
		this.angle = angle;
		
		init();
	}
	
	/**
	 * Start with the Radial Gradient editor.
	 */
	public MultipleGradientEditor(
			Point2D centerPoint,
			float[] fractions,
			Color[] colors,
			String targetId,
			CyServiceRegistrar serviceRegistrar
	) {
		this(GradientType.RADIAL, fractions, colors, targetId, serviceRegistrar);
		this.centerPoint = (Point2D) centerPoint.clone();
		
		init();
	}
	
	private MultipleGradientEditor(
			GradientType type,
			float[] fractions,
			Color[] colors,
			String targetId,
			CyServiceRegistrar serviceRegistrar
	) {
		this.type = type;
		this.fractions = fractions.clone();
		this.colors = colors.clone();
		
		this.targetId = targetId;
		this.serviceRegistrar = serviceRegistrar;
		
		paletteType = BrewerType.ANY;
		lastPalette = retrievePalette();
		
		if (lastPalette != null)
			setCurrentPalette(lastPalette);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public GradientType getType() {
		return type;
	}
	
	public float[] getFractions() {
		return fractions.clone();
	}
	
	public Color[] getColors() {
		return colors.clone();
	}
	
	public double getAngle() {
		return angle;
	}
	
	public Point2D getCenterPoint() {
		return centerPoint != null ? (Point2D) centerPoint.clone() : (Point2D) PointPicker.DEFAULT_VALUE.clone();
	}
	
	public void saveCurrentPalette() {
		if (currentPalette != null) {
			var mgr = serviceRegistrar.getService(PaletteProviderManager.class);
			mgr.savePalette("annotation::" + targetId, currentPalette);
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================

	private void init() {
		setOpaque(!isAquaLAF()); // Transparent if Aqua
		setMinimumSize(new Dimension(320, 320));
		
		typeGroup.add(getLinearToggle());
		typeGroup.add(getRadialToggle());
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addGap(10, 10, Short.MAX_VALUE)
						.addComponent(getLinearToggle())
						.addComponent(getRadialToggle())
						.addGap(10, 10, Short.MAX_VALUE)
				)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getPaletteBtn())
						.addGap(10, 10, Short.MAX_VALUE)
						.addComponent(getReverseBtn())
				)
				.addComponent(getGrEditor())
				.addComponent(getLinearOptionsPnl())
				.addComponent(getRadialOptionsPnl())
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getLinearToggle(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getRadialToggle(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getPaletteBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getReverseBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(getGrEditor(), 100, 100, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(getLinearOptionsPnl())
				.addComponent(getRadialOptionsPnl())
		);
		
		makeSmall(centerLbl);
		makeSmall(getLinearToggle(), getRadialToggle());
		
		if (isAquaLAF()) {
			// Mac OS properties:
			var toggleButtons = new JToggleButton[] { getLinearToggle(), getRadialToggle() };
			
			for (int i = 0; i < toggleButtons.length; i++) {
				var btn = toggleButtons[i];
				btn.putClientProperty("JButton.buttonType", "segmented");
	
				if (i == 0)
					btn.putClientProperty("JButton.segmentPosition", "first");
				else if (i == toggleButtons.length - 1)
					btn.putClientProperty("JButton.segmentPosition", "last");
				else
					btn.putClientProperty("JButton.segmentPosition", "middle");
			}
		}
		
		if (type == GradientType.LINEAR)
			typeGroup.setSelected(getLinearToggle().getModel(), true);
		else
			typeGroup.setSelected(getRadialToggle().getModel(), true);
		
		updateOptionPanel();
	}
	
	private JToggleButton getLinearToggle() {
		if (linearToggle == null) {
			linearToggle = new JToggleButton("Linear Gradient");
			linearToggle.addActionListener(evt -> {
				if (linearToggle.isSelected())
					type = GradientType.LINEAR;
				
				updateOptionPanel();
			});
		}
		
		return linearToggle;
	}

	private JToggleButton getRadialToggle() {
		if (radialToggle == null) {
			radialToggle = new JToggleButton("Radial Gradient");
			radialToggle.addActionListener(evt -> {
				if (radialToggle.isSelected())
					type = GradientType.RADIAL;
				
				updateOptionPanel();
				
				if (radialToggle.isSelected())
					centerPoint = getPointPicker().getValue();
			});
		}
		
		return radialToggle;
	}
	
	private JButton getPaletteBtn() {
		if (paletteBtn == null) {
			paletteBtn = new JButton("Palette");
			paletteBtn.setToolTipText("None");
			paletteBtn.addActionListener(evt -> {
				// Bring up the palette chooser dialog
				var factory = serviceRegistrar.getService(CyColorPaletteChooserFactory.class);
				var chooser = factory.getColorPaletteChooser(paletteType, false);
				var newPalette = chooser.showDialog(MultipleGradientEditor.this, "Set Palette", currentPalette, 9);

				if (newPalette == null)
					return;

				// Get the palette
				var colors = newPalette.getColors(this.colors.length);
				
				Object[] options = { "Yes", "No" };
				int n = JOptionPane.showOptionDialog(
						null, 
						"This will reset your current settings.\nAre you sure you want to continue?", 
				        "Warning",
				        JOptionPane.DEFAULT_OPTION,
				        JOptionPane.WARNING_MESSAGE,
				        null,
				        options,
				        options[1]
				);
				
				if (n == 0) {
					setCurrentPalette(newPalette);
					setColors(colors);
				}
			});
		}
		
		return paletteBtn;
	}
	
	public JButton getReverseBtn() {
		if (reverseBtn == null) {
			reverseBtn = new JButton(IconManager.ICON_EXCHANGE);
			reverseBtn.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(14.0f));
			reverseBtn.addActionListener(evt -> reverseColors());
		}
		
		return reverseBtn;
	}
	
	private GradientEditor getGrEditor() {
		if (grEditor == null) {
			var fractions = getFractions();
			var colors = getColors();
			grEditor = new GradientEditor(fractions, colors, serviceRegistrar);
			
			// Add listener--update gradient when user interacts with the UI
			grEditor.addActionListener(e -> {
				this.fractions = grEditor.getPositions();
				this.colors = grEditor.getColors();
				
				if (getRadialOptionsPnl().isVisible())
					updatePointPicker();
			});
		}
		
		return grEditor;
	}

	private JPanel getLinearOptionsPnl() {
		if (linearOptionsPnl == null) {
			linearOptionsPnl = new JPanel();
			linearOptionsPnl.setVisible(false);
			
			var layout = new GroupLayout(linearOptionsPnl);
			linearOptionsPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(angleLbl)
					.addComponent(getAngleCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
					.addComponent(angleLbl)
					.addComponent(getAngleCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			makeSmall(angleLbl, getAngleCmb());
		}
		
		return linearOptionsPnl;
	}
	
	private JComboBox<Double> getAngleCmb() {
		if (angleCmb == null) {
			angleCmb = new JComboBox<>(ANGLES);
			angleCmb.setEditable(true);
			((JLabel)angleCmb.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
			angleCmb.setSelectedItem(getAngle());
			angleCmb.setInputVerifier(new DoubleInputVerifier());
			
			angleCmb.addActionListener(e -> {
				var angle = angleCmb.getSelectedItem();
				this.angle = angle instanceof Number ? ((Number) angle).doubleValue() : 0.0;
			});
		}
		
		return angleCmb;
	}
	
	private JPanel getRadialOptionsPnl() {
		if (radialOptionsPnl == null) {
			radialOptionsPnl = new JPanel();
			radialOptionsPnl.setVisible(false);
			
			var layout = new GroupLayout(radialOptionsPnl);
			radialOptionsPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, false)
					.addComponent(centerLbl)
					.addComponent(getPointPicker(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(centerLbl)
					.addComponent(getPointPicker(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			makeSmall(centerLbl);
			makeSmall(getPaletteBtn(), getReverseBtn());
		}
		
		return radialOptionsPnl;
	}
	
	private PointPicker getPointPicker() {
		if (pointPicker == null) {
			pointPicker = new PointPicker(100, 12, getCenterPoint());
			
			pointPicker.addPropertyChangeListener("value", evt -> {
				centerPoint = (Point2D) evt.getNewValue();
			});
		}
		
		return pointPicker;
	}
	
	private Palette retrievePalette() {
		var mgr = serviceRegistrar.getService(PaletteProviderManager.class);
		
		return mgr.retrievePalette("annotation::" + targetId);
	}

	private void setCurrentPalette(Palette palette) {
		getPaletteBtn().setToolTipText(palette.toString());
		currentPalette = palette;
	}
	
	private void setColors(Color[] colors) {
		var controlPoints = new ArrayList<ControlPoint>();
		
		for (int i = 0; i < colors.length; i++) {
			var c = colors[i];
			float f = fractions[i];
			
			controlPoints.add(new GradientEditor.ControlPoint(c, f));
		}
		
		grEditor.setPoints(controlPoints);
	}
	
	private void reverseColors() {
		var controlPoints = new ArrayList<ControlPoint>();
		
		for (int i = fractions.length - 1; i >= 0; i--) {
			var c = colors[i];
			
			float f = 1.0f - fractions[i];
			f = Math.max(0.0f, Math.min(1.0f, f));
			
			controlPoints.add(new GradientEditor.ControlPoint(c, f));
		}
		
		grEditor.setPoints(controlPoints);
	}
	
	private void updateOptionPanel() {
		getLinearOptionsPnl().setVisible(getLinearToggle().isSelected());
		getRadialOptionsPnl().setVisible(getRadialToggle().isSelected());
		
		if (getRadialOptionsPnl().isVisible())
			updatePointPicker();
	}
	
	private void updatePointPicker() {
		if (getRadialOptionsPnl().isVisible())
			getPointPicker().update(getGrEditor().getPositions(), getGrEditor().getColors());
	}
}
