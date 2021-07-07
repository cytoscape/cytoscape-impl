package org.cytoscape.cg.internal.charts;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.cg.internal.charts.AbstractChart.DATA_COLUMNS;
import static org.cytoscape.cg.model.AbstractCustomGraphics2.COLORS;
import static org.cytoscape.cg.model.AbstractCustomGraphics2.COLOR_SCHEME;
import static org.cytoscape.cg.model.ColorScheme.CUSTOM;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.cytoscape.cg.internal.util.ColorUtil;
import org.cytoscape.cg.internal.util.IconUtil;
import org.cytoscape.cg.model.AbstractCustomGraphics2;
import org.cytoscape.cg.model.ColorScheme;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.util.color.PaletteType;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;

@SuppressWarnings("serial")
public class ColorSchemeEditor<T extends AbstractCustomGraphics2<?>> extends JPanel {

	private static final Color DEFAULT_COLOR = Color.LIGHT_GRAY;
	
	private final Border COLOR_BORDER;
	private final Border COLOR_HOVER_BORDER;
	
	private JLabel colorSchemeLbl;
    private JButton colorPaletteBtn;
	private JPanel colorListPnl;
	
	private Palette palette;
	private PaletteType paletteType;
	private String defaultPaletteName;
	private ColorScheme defaultColorScheme;
	
	protected final T chart;
	protected final boolean columnIsSeries;
	protected final CyNetwork network;
	protected final CyServiceRegistrar serviceRegistrar;
	
	protected int total = 0;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public ColorSchemeEditor(
			T chart,
			boolean columnIsSeries,
			PaletteType paletteType,
			String defaultPaletteName,
			CyNetwork network,
			CyServiceRegistrar serviceRegistrar
	) {
		this.chart = chart;
		this.columnIsSeries = columnIsSeries;
		this.network = network;
		this.serviceRegistrar = serviceRegistrar;
		
		setPaletteType(paletteType);
		setDefaultPaletteName(defaultPaletteName);
		palette = getDefaultPalette();
		
		COLOR_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UIManager.getColor("TextField.background"), 2),
				BorderFactory.createLineBorder(UIManager.getColor("TextField.inactiveForeground"), 1));
		COLOR_HOVER_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UIManager.getColor("Focus.color"), 2),
				BorderFactory.createLineBorder(UIManager.getColor("TextField.background"), 1));
		
		init();
		updateColorList(true);
		updateColorPaletteBtn();
	}

	// ==[ PUBLIC METHODS ]=============================================================================================

	public void setPaletteType(PaletteType paletteType) {
		if (paletteType == null)
			throw new IllegalArgumentException("'paletteType' must not be null.");
		
		if (this.paletteType != paletteType) {
			this.paletteType = paletteType;
			
			if (palette != null && !paletteType.equals(palette.getType()))
				palette = null;
		}
	}
	
	public void setDefaultPaletteName(String defaultPaletteName) {
		if (defaultPaletteName == null)
			throw new IllegalArgumentException("'defaultPaletteName' must not be null.");
		
		this.defaultPaletteName = defaultPaletteName;
	}
	
	public void reset(boolean resetColorScheme) {
		if (resetColorScheme) {
			defaultColorScheme = null;
			palette = getDefaultPalette();
			chart.set(COLOR_SCHEME, getDefaultColorScheme());
		}
			
		total = 0;
		updateColorList(true);
		updateColorPaletteBtn();
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================

	private void init() {
		colorSchemeLbl = new JLabel("Color Palette:");
		
		setOpaque(false);
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!isAquaLAF());
		
		var sep = new JSeparator();
		var colorListScr = new JScrollPane(getColorListPnl(),
				JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		colorListScr.setPreferredSize(new Dimension(160, 50));
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(colorSchemeLbl)
						.addComponent(getColorPaletteBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(colorListScr)
				.addComponent(sep)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(colorSchemeLbl)
						.addComponent(getColorPaletteBtn())
				)
				.addComponent(colorListScr, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		makeSmall(getColorPaletteBtn());
	}
	
	protected JButton getColorPaletteBtn() {
		if (colorPaletteBtn == null) {
			colorPaletteBtn = new JButton();

			colorPaletteBtn.addActionListener(evt -> {
				// Open color chooser
				var chooserFactory = serviceRegistrar.getService(CyColorPaletteChooserFactory.class);
				var chooser = chooserFactory.getColorPaletteChooser(paletteType, true);
				var title = "Palettes";
				int size = getTotal();
				
				chooser.showDialog(this, title, palette, size);
				palette = chooser.getSelectedPalette();

				if (palette != null) {
					var newScheme = new ColorScheme(palette);
					chart.set(COLOR_SCHEME, newScheme);
					updateColorList(true);
				}
				
				updateColorPaletteBtn();
			});
		}

		return colorPaletteBtn;
	}
	
	protected JPanel getColorListPnl() {
		if (colorListPnl == null) {
			colorListPnl = new JPanel();
			colorListPnl.setOpaque(false);
			colorListPnl.setLayout(new BoxLayout(colorListPnl, BoxLayout.X_AXIS));
		}
		
		return colorListPnl;
	}
	
	private Palette getDefaultPalette() {
		var scheme = getDefaultColorScheme();
		
		return scheme != null ? scheme.getPalette() : null;
	}
	
	protected ColorScheme getDefaultColorScheme() {
		if (defaultColorScheme == null) {
			var ppManager = serviceRegistrar.getService(PaletteProviderManager.class);
			var providers = ppManager.getPaletteProviders();
			
			for (var pp : providers) {
				var p = pp.getPalette(defaultPaletteName);
				
				if (p != null) {
					defaultColorScheme = new ColorScheme(p);
					break;
				}
			}
			
			if (defaultColorScheme == null)
				defaultColorScheme = ColorScheme.CONTRASTING;
		}
		
		return defaultColorScheme;
	}
	
	private void updateColorPaletteBtn() {
		getColorPaletteBtn().setText(palette != null ? palette.getName() : "None");
	}
	
	protected void updateColorList(boolean newScheme) {
		List<Color> colors = new ArrayList<>(chart.getList(COLORS, Color.class));
		var scheme = chart.get(COLOR_SCHEME, ColorScheme.class);
		
		if (scheme == null) {
			palette = getDefaultPalette();
			scheme = getDefaultColorScheme();
			updateColorPaletteBtn();
		} else if (scheme.getPalette() != null && !scheme.getPalette().getType().equals(palette.getType())) {
			palette = getDefaultPalette();
			scheme = getDefaultColorScheme();
			chart.set(COLOR_SCHEME, scheme);
			updateColorPaletteBtn();
		}
			
		int nColors = getTotal();
		
		if (nColors > 0) {
			if (newScheme || colors.isEmpty()) {
				if (CUSTOM.equals(scheme)) {
					int newSize = Math.max(colors.size(), nColors);
					colors = new ArrayList<Color>(newSize);
					
					for (int i = 0; i < newSize; i++)
						colors.add(i%2 == 0 ? DEFAULT_COLOR : DEFAULT_COLOR.darker());
				} else {
					colors = scheme.getColors(nColors);
				}
			} else if (colors.size() < nColors) {
				// Just update existing list of colors (add new ones if there are more values now)
				var newColors = scheme.getColors(nColors);
				
				if (newColors.size() > colors.size())
					colors.addAll(newColors.subList(colors.size(), newColors.size()));
			}
		}
		
		// Build list of editable colors
		getColorListPnl().removeAll();
		
		for (int i = 0; i < total; i++) {
			var c = colors.size() > i ? colors.get(i) : Color.GRAY;
			var cp = new ColorPanel(c, "");
			style(cp, i);
			
			getColorListPnl().add(cp);
		}
		
		getColorListPnl().repaint();
	
		if (!colors.isEmpty())
			chart.set(COLORS, colors);
	}
	
	protected void style(ColorPanel cp, int index) {
		var label = "" + (index + 1);
		cp.setText(label);
	}
	
	protected int getTotal() {
		if (total <= 0) {
			var dataColumns = chart.getList(DATA_COLUMNS, CyColumnIdentifier.class);
			
			if (columnIsSeries) {
				// Each column represents a data series
				total = dataColumns.size();
			} else if (network != null) {
				// Columns represent data categories--each list element is an item/color
				int nColors1 = 0;
				int nColors2 = 0;
				
				var allNodes = network.getNodeList();
				var table = network.getDefaultNodeTable();
				
				for (var colId : dataColumns) {
					var column = table.getColumn(colId.getColumnName());
					if (column == null) continue;
					
					if (column.getType() == List.class) {
						for (var node : allNodes) {
							var row = network.getRow(node);
							var values = row.getList(column.getName(), column.getListElementType());
							
							if (values != null)
								nColors1 = Math.max(nColors1, values.size());
						}
					} else {
						nColors2++;
					}
				}
				
				total = Math.max(nColors1, nColors2);
			}
		}
		
		return total;
	}
	
	// ==[ CLASSES ]====================================================================================================

	protected class ColorPanel extends JLabel {

		private Color color;

		ColorPanel(Color color, String label) {
			super(label, IconUtil.emptyIcon(20, 20), JLabel.CENTER);
			
			if (color == null)
				color = Color.LIGHT_GRAY;
			
			this.color = color;
			
			this.setFont(this.getFont().deriveFont(10.0f));
			this.setHorizontalTextPosition(JLabel.CENTER);
			this.setOpaque(true);
			this.setBackground(color);
			this.setForeground(ColorUtil.getContrastingColor(color));
			this.setBorder(COLOR_BORDER);
			this.setToolTipText(String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
			
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					chooseColor();
					onColorsUpdated();
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					ColorPanel.this.setBorder(COLOR_HOVER_BORDER);
				}
				@Override
				public void mouseExited(MouseEvent e) {
					ColorPanel.this.setBorder(COLOR_BORDER);
				}
			});
		}
		
		public Color getColor() {
			return color;
		}
		
		private void chooseColor() {
			var chooserFactory = serviceRegistrar.getService(CyColorPaletteChooserFactory.class);
			var chooser = chooserFactory.getColorPaletteChooser(BrewerType.ANY, false);
			int size = getTotal();
			
			if (size < 2)
				size = 5;
			
			var newColor = chooser.showDialog(this, "Colors", palette, color, size);
			
			if (newColor != null) {
				color = newColor;
				setBackground(newColor);
				setForeground(ColorUtil.getContrastingColor(newColor));
			}
		}
		
		@SuppressWarnings("rawtypes")
		private void onColorsUpdated() {
			var rows = getColorListPnl().getComponents();
			var newColors = new ArrayList<Color>();
			
			if (rows != null) {
				for (var c : rows) {
					if (c instanceof ColorSchemeEditor.ColorPanel) {
						var color = ((ColorSchemeEditor.ColorPanel) c).getColor();
						newColors.add(color);
					}
				}
			}
			
			chart.set(COLORS, newColors);
		}
	}
}
