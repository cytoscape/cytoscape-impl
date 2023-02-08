package org.cytoscape.cg.internal.gradient;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.cg.internal.gradient.AbstractGradient.GRADIENT_COLORS;
import static org.cytoscape.cg.internal.gradient.AbstractGradient.GRADIENT_FRACTIONS;
import static org.cytoscape.cg.model.AbstractCustomGraphics2.COLOR_SCHEME;
import static org.cytoscape.util.swing.LookAndFeelUtil.equalizeSize;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;

import org.cytoscape.cg.internal.util.ColorUtil;
import org.cytoscape.cg.internal.util.GradientEditor;
import org.cytoscape.cg.internal.util.GradientEditor.ControlPoint;
import org.cytoscape.cg.internal.util.ViewUtil;
import org.cytoscape.cg.model.AbstractCustomGraphics2;
import org.cytoscape.cg.model.ColorScheme;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteType;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
public abstract class AbstractGradientEditor<T extends AbstractCustomGraphics2<?>> extends JPanel {

	private JPanel colorsPnl;
	private JButton paletteBtn;
	private JButton reverseBtn;
	private GradientEditor grEditor;
	private JButton addBtn;
	private JButton removeBtn;
	private JButton editBtn;
	
	private JPanel otherOptionsPnl;
	
	private Palette lastPalette;
	private Palette currentPalette;
	private PaletteType paletteType;

	protected final T gradient;
	protected final CyServiceRegistrar serviceRegistrar;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public AbstractGradientEditor(T gradient, CyServiceRegistrar serviceRegistrar) {
		this.gradient = gradient;
		this.serviceRegistrar = serviceRegistrar;
		
		paletteType = BrewerType.ANY;
		lastPalette = retrievePalette();
		setCurrentPalette(lastPalette);
		
		init();
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	protected void init() {
		setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
					.addComponent(getColorsPnl(), PREFERRED_SIZE, 320, PREFERRED_SIZE)
					.addComponent(getOtherOptionsPnl())
				)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getColorsPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(getOtherOptionsPnl())
		);
		
		var buttons = new JButton[] { getPaletteBtn(), getReverseBtn(), getAddBtn(), getRemoveBtn(), getEditBtn() };
		ViewUtil.styleEditorButtons(buttons);
		equalizeSize(buttons);
		
		setMaximumSize(new Dimension(350, getPreferredSize().height));
		
		update();
	}
	
	private JPanel getColorsPnl() {
		if (colorsPnl == null) {
			colorsPnl = new JPanel();
			colorsPnl.setOpaque(!LookAndFeelUtil.isAquaLAF());
			
			var layout = new GroupLayout(colorsPnl);
			colorsPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addGroup(layout.createSequentialGroup()
							.addGap(5)
							.addComponent(getPaletteBtn())
							.addGap(10, 10, Short.MAX_VALUE)
							.addComponent(getReverseBtn())
							.addGap(5)
					)
					.addComponent(getGrEditor(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
							.addComponent(getAddBtn())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getRemoveBtn())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getEditBtn())
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(getPaletteBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getReverseBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getGrEditor(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(getAddBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getRemoveBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getEditBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
		}
		
		return colorsPnl;
	}
	
	private JButton getPaletteBtn() {
		if (paletteBtn == null) {
			paletteBtn = new JButton("Palette");
			paletteBtn.addActionListener(evt -> {
				// Bring up the palette chooser dialog
				var factory = serviceRegistrar.getService(CyColorPaletteChooserFactory.class);
				var chooser = factory.getColorPaletteChooser(paletteType, false);
				var newPalette = chooser.showDialog(AbstractGradientEditor.this, "Set Palette", currentPalette, 9);

				if (newPalette == null)
					return;
				
				Object[] options = { "Yes", "No" };
				int n = JOptionPane.showOptionDialog(
						null, 
						"This will reset your current colors.\nAre you sure you want to continue?", 
				        "Warning",
				        JOptionPane.DEFAULT_OPTION,
				        JOptionPane.WARNING_MESSAGE,
				        null,
				        options,
				        options[1]
				);
				
				if (n == 0) {
					setCurrentPalette(newPalette);
					
					var colorList = gradient.getList(GRADIENT_COLORS, Color.class);
					var colors = newPalette.getColors(colorList.size());
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
			reverseBtn.setToolTipText("Reverse Colors");
			reverseBtn.addActionListener(evt -> reverseColors());
		}
		
		return reverseBtn;
	}

	protected GradientEditor getGrEditor() {
		if (grEditor == null) {
			var fractions = gradient.getList(GRADIENT_FRACTIONS, Float.class);
			var colors = gradient.getList(GRADIENT_COLORS, Color.class);
			grEditor = new GradientEditor(fractions, colors, serviceRegistrar);
			
			// Add listener--update gradient when user interacts with the UI
			grEditor.addActionListener(e -> onGradientUpdated());
			grEditor.addPropertyChangeListener("selected", evt-> update());
			
			if (fractions == null || fractions.size() < 2)
				onGradientUpdated();
		}
		
		return grEditor;
	}
	
	private JButton getAddBtn() {
		if (addBtn == null) {
			var icoMgr = serviceRegistrar.getService(IconManager.class);
			
			addBtn = new JButton();
			addBtn.setIcon(new TextIcon(IconManager.ICON_PLUS, icoMgr.getIconFont(14.0f), 16,  16));
			addBtn.setToolTipText("Add Color");
			addBtn.addActionListener(evt -> getGrEditor().addPoint());
		}
		
		return addBtn;
	}
	
	private JButton getRemoveBtn() {
		if (removeBtn == null) {
			var icoMgr = serviceRegistrar.getService(IconManager.class);
			
			removeBtn = new JButton();
			removeBtn.setIcon(new TextIcon(IconManager.ICON_TRASH_O, icoMgr.getIconFont(16.0f), 16,  16));
			removeBtn.setToolTipText("Remove Color");
			removeBtn.setEnabled(false);
			removeBtn.addActionListener(evt -> getGrEditor().deletePoint());
		}
		
		return removeBtn;
	}
	
	private JButton getEditBtn() {
		if (editBtn == null) {
			editBtn = new JButton();
			editBtn.setIcon(new ColorIcon(16, 16));
			editBtn.setToolTipText("Edit Color");
			editBtn.setEnabled(false);
			editBtn.addActionListener(evt -> getGrEditor().editPoint());
		}
		
		return editBtn;
	}
	
	/**
	 * Should be overridden by the concrete subclass if it provides extra fields.
	 * @return
	 */
	protected JPanel getOtherOptionsPnl() {
		if (otherOptionsPnl == null) {
			otherOptionsPnl = new JPanel();
			otherOptionsPnl.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
			otherOptionsPnl.setVisible(false);
		}
		
		return otherOptionsPnl;
	}
	
	private Palette retrievePalette() {
		var scheme = gradient.get(COLOR_SCHEME, ColorScheme.class);
		
		return scheme != null ? scheme.getPalette() : null;
	}

	private void setCurrentPalette(Palette palette) {
		getPaletteBtn().setToolTipText(palette != null ? palette.toString() : "None");
		currentPalette = palette;
	}
	
	private void setColors(Color[] colors) {
		var controlPoints = new ArrayList<ControlPoint>();
		var fractions = gradient.getList(GRADIENT_FRACTIONS, Float.class);
		
		for (int i = 0; i < colors.length; i++) {
			var c = colors[i];
			float f = fractions.get(i);
			
			controlPoints.add(new GradientEditor.ControlPoint(c, f));
		}
		
		grEditor.setPoints(controlPoints);
	}
	
	private void reverseColors() {
		var fractions = gradient.getList(GRADIENT_FRACTIONS, Float.class);
		var colors = gradient.getList(GRADIENT_COLORS, Color.class);
		var controlPoints = new ArrayList<ControlPoint>();
		
		for (int i = fractions.size() - 1; i >= 0; i--) {
			float f = fractions.get(i);
			var c = colors.get(i);
			
			f = 1.0f - f;
			f = Math.max(0.0f, Math.min(1.0f, f));
			
			controlPoints.add(new GradientEditor.ControlPoint(c, f));
		}
		
		grEditor.setPoints(controlPoints);
	}
	
	private void onGradientUpdated() {
		gradient.set(GRADIENT_FRACTIONS, getGrEditor().getPositions());
		gradient.set(GRADIENT_COLORS, getGrEditor().getColors());
		
		if (currentPalette != null) {
			var newScheme = new ColorScheme(currentPalette);
			gradient.set(COLOR_SCHEME, newScheme);
		}
		
		update();
	}
	
	protected void update() {
		updatePointButtons();
	}
	
	@SuppressWarnings("unchecked")
	private void updatePointButtons() {
		var selected = getGrEditor().getSelected();
		var controlPoints = getGrEditor().getControlPoints();
		
		((ColorIcon) getEditBtn().getIcon()).setColor(selected != null ? selected.getColor() : null);
		
		getEditBtn().setEnabled(selected != null);
		getRemoveBtn().setEnabled(selected != null
				&& !Objects.equal(selected, controlPoints.get(0))
				&& !Objects.equal(selected, controlPoints.get(controlPoints.size() - 1)));
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class ColorIcon implements Icon {

		private Color color;
		
		private final int width;
		private final int height;
		
		public ColorIcon(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
		@Override
		public int getIconHeight() {
			return width;
		}

		@Override
		public int getIconWidth() {
			return height;
		}
		
		public void setColor(Color color) {
			this.color = color;
			repaint();
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			int w = getIconWidth();
			int h = getIconHeight();
			
			if (c.isEnabled()) {
				g.setColor(color != null ? color : Color.WHITE);
				g.fillRect(x, y, w, h);
			}
			
			g.setColor(c.isEnabled() 
					? ColorUtil.getContrastingColor(c.getBackground())
					: UIManager.getColor("Button.disabledForeground")
			);
			g.drawRect(x, y, w, h);
			
			if (color == null && c.isEnabled()) {
				g.setColor(Color.RED);
				g.drawLine(x + 1, y + h - 1, x + w - 1, y + 1);
			}
		}
	}
}
