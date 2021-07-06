package org.cytoscape.cg.internal.charts.bar;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.cg.model.AbstractCustomGraphics2.ORIENTATION;
import static org.cytoscape.cg.model.ColorScheme.CONTRASTING;
import static org.cytoscape.cg.model.ColorScheme.CUSTOM;
import static org.cytoscape.cg.model.ColorScheme.MODULATED;
import static org.cytoscape.cg.model.ColorScheme.RAINBOW;
import static org.cytoscape.cg.model.ColorScheme.RANDOM;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.cg.internal.charts.AbstractChartEditor;
import org.cytoscape.cg.internal.charts.ColorSchemeEditor;
import org.cytoscape.cg.internal.charts.bar.BarChart.BarChartType;
import org.cytoscape.cg.internal.charts.util.ColorGradient;
import org.cytoscape.cg.model.ColorScheme;
import org.cytoscape.cg.model.Orientation;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class BarChartEditor extends AbstractChartEditor<BarChart> {

	private static final ColorScheme[] REGULAR_COLOR_SCHEMES = new ColorScheme[] {
		CONTRASTING, MODULATED, RAINBOW, RANDOM, CUSTOM
	};
	private static final ColorScheme[] HEAT_STRIP_COLOR_SCHEMES;
	private static final ColorScheme[] UP_DOWN_COLOR_SCHEMES;
	
	static {
		var heatStripSchemeList = new ArrayList<ColorScheme>();
		var upDownSchemeList = new ArrayList<ColorScheme>();
		
		for (var cg : ColorGradient.values()) {
			if (cg.getColors().size() == 2)
				upDownSchemeList.add(new ColorScheme(cg));
			else if (cg.getColors().size() == 3)
				heatStripSchemeList.add(new ColorScheme(cg));
		}
		
		heatStripSchemeList.add(CUSTOM);
		upDownSchemeList.add(CUSTOM);
		
		HEAT_STRIP_COLOR_SCHEMES = heatStripSchemeList.toArray(new ColorScheme[heatStripSchemeList.size()]);
		UP_DOWN_COLOR_SCHEMES = upDownSchemeList.toArray(new ColorScheme[upDownSchemeList.size()]);
	}
	
	private ButtonGroup typeGrp;
	private JRadioButton groupedRd;
	private JRadioButton stackedRd;
	private JRadioButton heatStripsRd;
	private JRadioButton upDownRd;
	private JLabel separationLbl;
	private JTextField separationTxt;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BarChartEditor(BarChart chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, Number.class, false, true, true, true, true, false, true, true, serviceRegistrar);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		super.createLabels();
		separationLbl = new JLabel("Separation (0.0-0.5):");
	}
	
	@Override
	protected ColorScheme[] getColorSchemes() {
		var type = chart.get(BarChart.TYPE, BarChartType.class, BarChartType.GROUPED);
		
		return type == BarChartType.HEAT_STRIPS ? 
				HEAT_STRIP_COLOR_SCHEMES : 
				(type == BarChartType.UP_DOWN ? UP_DOWN_COLOR_SCHEMES : REGULAR_COLOR_SCHEMES);
	}
	
	@Override
	protected JPanel getOtherBasicOptionsPnl() {
		var p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		var layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		var sep = new JSeparator();
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getGroupedRd())
						.addComponent(getStackedRd())
						.addComponent(getHeatStripsRd())
						.addComponent(getUpDownRd())
				)
				.addComponent(sep)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getGroupedRd())
						.addComponent(getStackedRd())
						.addComponent(getHeatStripsRd())
						.addComponent(getUpDownRd())
				)
				.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		return p;
	}
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		var p = super.getOtherBasicOptionsPnl();
		
		var layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(separationLbl)
				.addComponent(getSeparationTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
				.addComponent(separationLbl)
				.addComponent(getSeparationTxt())
		);
		
		return p;
	}
	
	private ButtonGroup getTypeGrp() {
		if (typeGrp == null) {
			typeGrp = new ButtonGroup();
			typeGrp.add(getGroupedRd());
			typeGrp.add(getStackedRd());
			typeGrp.add(getHeatStripsRd());
			typeGrp.add(getUpDownRd());
		}
		
		return typeGrp;
	}
	
	private JRadioButton getGroupedRd() {
		if (groupedRd == null) {
			groupedRd = new JRadioButton("Grouped");
			groupedRd.addActionListener(evt -> setType());
		}
		
		return groupedRd;
	}
	
	private JRadioButton getStackedRd() {
		if (stackedRd == null) {
			stackedRd = new JRadioButton("Stacked");
			stackedRd.addActionListener(evt -> setType());
		}
		
		return stackedRd;
	}
	
	public JRadioButton getHeatStripsRd() {
		if (heatStripsRd == null) {
			heatStripsRd = new JRadioButton("Heat Strips");
			heatStripsRd.addActionListener(evt -> setType());
		}
		
		return heatStripsRd;
	}
	
	public JRadioButton getUpDownRd() {
		if (upDownRd == null) {
			upDownRd = new JRadioButton("Up-Down");
			upDownRd.addActionListener(evt -> setType());
		}
		
		return upDownRd;
	}
	
	private JTextField getSeparationTxt() {
		if (separationTxt == null) {
			separationTxt = new JTextField("" + chart.get(BarChart.SEPARATION, Double.class, 0.0));
			separationTxt.setToolTipText("Percentage of the available space for all bars (0.1 is 10%)");
			separationTxt.setInputVerifier(new DoubleInputVerifier());
			separationTxt.setPreferredSize(new Dimension(60, separationTxt.getMinimumSize().height));
			separationTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			separationTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					try {
			            double sep = Double.parseDouble(separationTxt.getText());
			            chart.set(BarChart.SEPARATION, sep);
			        } catch (NumberFormatException nfe) {
			        }
				}
			});
		}
		
		return separationTxt;
	}
	
	private void setType() {
		final BarChartType type;
		
		if (getStackedRd().isSelected())
			type = BarChartType.STACKED;
		else if (getHeatStripsRd().isSelected())
			type = BarChartType.HEAT_STRIPS;
		else if (getUpDownRd().isSelected())
			type = BarChartType.UP_DOWN;
		else
			type = BarChartType.GROUPED;
		
		chart.set(BarChart.TYPE, type);
		updateRangeMinMax(true);
		getColorSchemeEditor().reset();
	}
	
	protected void updateType() {
		var type = chart.get(BarChart.TYPE, BarChartType.class, BarChartType.GROUPED);
		final JRadioButton typeRd;
		
		if (type == BarChartType.STACKED)
			typeRd = getStackedRd();
		else if (type == BarChartType.HEAT_STRIPS)
			typeRd = getHeatStripsRd();
		else if (type == BarChartType.UP_DOWN)
			typeRd = getUpDownRd();
		else
			typeRd = getGroupedRd();
		
		getTypeGrp().setSelected(typeRd.getModel(), true);
	}
	
	@Override
	protected void update(boolean recalculateRange) {
		updateType();
		super.update(recalculateRange);
	}
	
	@Override
	protected void updateOptions() {
		super.updateOptions();
		
		// Hide options that would just make table "sparklines" too cramped
		boolean sparklines = targetType == CyColumn.class;
		
		getOtherAdvancedOptionsPnl().setVisible(!sparklines);
		
		if (sparklines) {
			chart.set(ORIENTATION, Orientation.AUTO); // auto-orientation, to be decided by the BarChart
			chart.set(BarChart.SEPARATION, 0.1); // 10% of separation between bars is a good default for sparklines
		}
	}
	
	@Override
	protected double[] minMax(double min, double max, List<? extends Number> values) {
		if (values != null) {
			boolean stacked = getStackedRd().isSelected();
			double sum = 0;
			
			for (var v : values) {
				if (v != null) {
					double dv = v.doubleValue();
					
					if (stacked) {
						sum += dv;
					} else {
						min = Math.min(min, dv);
						max = Math.max(max, dv);
					}
				}
			}
			
			if (stacked) {
				min = Math.min(min, sum);
				max = Math.max(max, sum);
			}
		}
		
		return new double[]{ min, max };
	}
	
	@Override
	protected ColorSchemeEditor<BarChart> getColorSchemeEditor() {
		if (colorSchemeEditor == null) {
			colorSchemeEditor = new BarColorSchemeEditor(
					chart,
					getColorSchemes(),
					serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork(),
					serviceRegistrar
			);
		}
		
		return colorSchemeEditor;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class BarColorSchemeEditor extends ColorSchemeEditor<BarChart> {

		public BarColorSchemeEditor(
				BarChart chart,
				ColorScheme[] colorSchemes,
				CyNetwork network,
				CyServiceRegistrar serviceRegistrar
		) {
			super(chart, colorSchemes, false, network, serviceRegistrar);
		}

		@Override
		protected int getTotal() {
			var type = chart.get(BarChart.TYPE, BarChartType.class, BarChartType.GROUPED);
			
			if (type == BarChartType.HEAT_STRIPS)
				return total = 3;
			if (type == BarChartType.UP_DOWN)
				return total = 2;
			
			return super.getTotal();
		}
		
		@Override
		protected void style(ColorPanel cp, int index) {
			super.style(cp, index);
			var type = chart.get(BarChart.TYPE, BarChartType.class, BarChartType.GROUPED);
			
			if (type == BarChartType.HEAT_STRIPS || type == BarChartType.UP_DOWN) {
				cp.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(11));
				String label = "";
				String toolTip = null;
				
				if (index == 0) {
					label = IconManager.ICON_ARROW_UP;
					toolTip = "Positive Numbers";
				} else if (index == 1) {
					label = type == BarChartType.UP_DOWN ? IconManager.ICON_ARROW_DOWN : "";
					toolTip = type == BarChartType.UP_DOWN ? "Negative Numbers" : "Zero";
				} else if (index == 2 && type == BarChartType.HEAT_STRIPS) {
					label = IconManager.ICON_ARROW_DOWN;
					toolTip = "Negative Numbers";
				}
				
				cp.setText(label);
				cp.setToolTipText(toolTip);
			}
		}
	}
}
