package org.cytoscape.ding.internal.charts;

import static org.cytoscape.ding.internal.charts.AbstractEnhancedCustomGraphics.COLORS;
import static org.cytoscape.ding.internal.charts.AbstractEnhancedCustomGraphics.COLOR_SCHEME;
import static org.cytoscape.ding.internal.charts.AbstractEnhancedCustomGraphics.DATA_COLUMNS;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.Border;

import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.ding.internal.util.IconUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class ColorSchemeEditor<T extends AbstractEnhancedCustomGraphics<?>> extends JPanel {

	private static final long serialVersionUID = 4652060176779329556L;
	
	private static final Color DEFAULT_COLOR = Color.LIGHT_GRAY;
	private static final JColorChooser colorChooser = new JColorChooser();
	
	private final Border COLOR_BORDER;
	private final Border COLOR_HOVER_BORDER;
	
	private JLabel colorSchemeLbl;
	private JComboBox colorSchemeCmb;
	private JPanel colorListPnl;
	
	protected final T chart;
	protected final String[] colorSchemes;
	protected final boolean columnIsSeries;
	protected final CyNetwork network;
	protected final IconManager iconMgr;
	
	protected int total = 0;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public ColorSchemeEditor(final T chart, final String[] colorSchemes, final boolean columnIsSeries,
			final CyNetwork network, final IconManager iconMgr) {
		this.chart = chart;
		this.colorSchemes = colorSchemes;
		this.columnIsSeries = columnIsSeries;
		this.network = network;
		this.iconMgr = iconMgr;
		
		JList tmpList = new JList();
		COLOR_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.WHITE, 2),
				BorderFactory.createLineBorder(Color.GRAY, 1));
		COLOR_HOVER_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(tmpList.getSelectionBackground(), 2),
				BorderFactory.createLineBorder(tmpList.getSelectionForeground(), 1));
		
		init();
		updateColorList(false);
	}

	// ==[ PUBLIC METHODS ]=============================================================================================

	public void reset() {
		total = 0;
		updateColorList(false);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================

	private void init() {
		colorSchemeLbl = new JLabel("Color Scheme");
		
		setOpaque(false);
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		
		final JSeparator sep = new JSeparator();
		final JScrollPane colorListScr = new JScrollPane(getColorListPnl(),
				JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		colorListScr.setPreferredSize(new Dimension(160, 50));
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(colorSchemeLbl)
						.addComponent(getColorSchemeCmb(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						          GroupLayout.PREFERRED_SIZE))
				.addComponent(colorListScr)
				.addComponent(sep)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(colorSchemeLbl)
						.addComponent(getColorSchemeCmb()))
				.addComponent(colorListScr, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
		);
	}
	
	protected JComboBox getColorSchemeCmb() {
		if (colorSchemeCmb == null) {
			colorSchemeCmb = new JComboBox(colorSchemes);
			
			String scheme = chart.get(COLOR_SCHEME, String.class, "");
			
			if (Arrays.asList(colorSchemes).contains(scheme)) {
				colorSchemeCmb.setSelectedItem(scheme);
			} else {
				scheme = ColorUtil.CUSTOM;
				colorSchemeCmb.setSelectedItem(scheme);
				chart.set(COLOR_SCHEME, scheme);
			}
			
			colorSchemeCmb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final String newScheme = (String) colorSchemeCmb.getSelectedItem();
					chart.set(COLOR_SCHEME, newScheme);
					updateColorList(true);
				}
			});
		}
		
		return colorSchemeCmb;
	}
	
	protected JPanel getColorListPnl() {
		if (colorListPnl == null) {
			colorListPnl = new JPanel();
			colorListPnl.setOpaque(false);
			colorListPnl.setLayout(new BoxLayout(colorListPnl, BoxLayout.X_AXIS));
		}
		
		return colorListPnl;
	}
	
	private void updateColorList(final boolean newScheme) {
		List<Color> colors = chart.getList(COLORS, Color.class);
		final String scheme = chart.get(COLOR_SCHEME, String.class, "");
		final int nColors =
				ColorUtil.UP_DOWN.equals(scheme) && Arrays.asList(colorSchemes).contains(scheme) ? 2 : getTotal();
		
		if (nColors > 0) {
			if (newScheme || colors.isEmpty()) {
				if (ColorUtil.CUSTOM.equalsIgnoreCase(scheme)) {
					int newSize = Math.max(colors.size(), nColors);
					colors = new ArrayList<Color>(newSize);
					
					for (int i = 0; i < newSize; i++)
						colors.add(DEFAULT_COLOR);
				} else {
					colors = ColorUtil.getColors(scheme, nColors);
				}
			} else if (colors.size() < nColors) {
				// Just update existing list of colors (add new ones if there are more values now)
				final List<Color> newColors = ColorUtil.getColors(scheme, nColors);
				
				if (newColors.size() > colors.size())
					colors.addAll(newColors.subList(colors.size(), newColors.size()));
			}
		}
		
		// Build list of editable colors
		getColorListPnl().removeAll();
		int count = 0;
		
		for (final Color c : colors) {
			final ColorPanel cp = new ColorPanel(c, "");
			String label = "";
			
			if (ColorUtil.UP_DOWN.equals(scheme) && count < 2) {
				label = count == 0 ? IconManager.ICON_ARROW_UP : IconManager.ICON_ARROW_DOWN;
				cp.setFont(iconMgr.getIconFont(11));
			} else {
				label = "" + (count + 1);
			}
			
			cp.setText(label);
			
			getColorListPnl().add(cp);
			count++;
		}
		
		getColorListPnl().repaint();
	
		if (!colors.isEmpty())
			chart.set(COLORS, colors);
	}
	
	protected int getTotal() {
		if (total <= 0) {
			final List<String> columns = chart.getList(DATA_COLUMNS, String.class);
			
			if (columnIsSeries) {
				// Each column represents a data series
				total = columns.size();
			} else if (network != null) {
				// Columns represent data categories--each list element is an item/color
				int nColors = 0;
				
				final List<CyNode> allNodes = network.getNodeList();
				final CyTable table = network.getDefaultNodeTable();
				
				for (final String columnName : columns) {
					final CyColumn column = table.getColumn(columnName);
					
					if (column != null && column.getType() == List.class) {
						for (final CyNode node : allNodes) {
							final CyRow row = network.getRow(node);
							final List<?> values = row.getList(columnName, column.getListElementType());
							
							if (values != null)
								nColors = Math.max(nColors, values.size());
						}
					}
				}
				
				total = nColors;
			}
		}
		
		return total;
	}
	
	// ==[ CLASSES ]====================================================================================================

	class ColorPanel extends JLabel {

		private static final long serialVersionUID = -4506612946805038646L;
		
		private Color color;

		ColorPanel(final Color color, final String label) {
			super(label, IconUtil.emptyIcon(20, 20), JLabel.CENTER);
			
			this.color = color != null ? color : Color.LIGHT_GRAY;
			
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
			final JDialog dialog = JColorChooser.createDialog(
					ColorSchemeEditor.this,
					"Please pick a color",
					true,
					colorChooser, 
					new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							color = colorChooser.getColor();
							ColorPanel.this.setBackground(color);
							ColorPanel.this.setForeground(ColorUtil.getContrastingColor(color));
						}
					}, null);
			dialog.setVisible(true);
		}
		
		private void onColorsUpdated() {
			final Component[] rows = getColorListPnl().getComponents();
			final List<Color> newColors = new ArrayList<Color>();
			
			if (rows != null) {
				for (final Component c : rows) {
					if (c instanceof ColorSchemeEditor.ColorPanel) {
						final Color color = ((ColorSchemeEditor.ColorPanel)c).getColor();
						newColors.add(color);
					}
				}
			}
			
			chart.set(COLORS, newColors);
		}
	}
}
