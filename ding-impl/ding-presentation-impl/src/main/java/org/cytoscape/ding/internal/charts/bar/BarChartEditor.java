package org.cytoscape.ding.internal.charts.bar;

import static org.cytoscape.ding.internal.charts.ColorScheme.CONTRASTING;
import static org.cytoscape.ding.internal.charts.ColorScheme.CUSTOM;
import static org.cytoscape.ding.internal.charts.ColorScheme.MODULATED;
import static org.cytoscape.ding.internal.charts.ColorScheme.RAINBOW;
import static org.cytoscape.ding.internal.charts.ColorScheme.RANDOM;
import static org.cytoscape.ding.internal.charts.ColorScheme.UP_DOWN;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JTextField;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.charts.ColorScheme;
import org.cytoscape.ding.internal.charts.ColorSchemeEditor;
import org.cytoscape.ding.internal.charts.bar.BarChart.BarChartType;
import org.cytoscape.ding.internal.charts.util.ColorGradient;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class BarChartEditor extends AbstractChartEditor<BarChart> {

	private static final long serialVersionUID = 2428987302044041051L;
	
	private static final ColorScheme[] GROUPED_COLOR_SCHEMES = new ColorScheme[] {
		CONTRASTING, MODULATED, RAINBOW, RANDOM, UP_DOWN, CUSTOM
	};
	
	private static final ColorScheme[] STACKED_COLOR_SCHEMES = new ColorScheme[] {
		CONTRASTING, MODULATED, RAINBOW, RANDOM, CUSTOM
	};
	
	private static final ColorScheme[] HEAT_STRIP_COLOR_SCHEMES;
	
	static {
		final List<ColorScheme> heatStripSchemeList = new ArrayList<ColorScheme>();
		
		for (final ColorGradient cg : ColorGradient.values()) {
			if (cg.getColors().size() == 3)
				heatStripSchemeList.add(new ColorScheme(cg));
		}
		
		heatStripSchemeList.add(CUSTOM);
		
		HEAT_STRIP_COLOR_SCHEMES = heatStripSchemeList.toArray(new ColorScheme[heatStripSchemeList.size()]);
	}
	
	private JLabel typeLbl;
	private ButtonGroup typeGrp;
	private JRadioButton groupedRd;
	private JRadioButton stackedRd;
	private JRadioButton heatStripsRd;
	private JLabel separationLbl;
	private JTextField separationTxt;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BarChartEditor(final BarChart chart, final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		super(chart, Number.class, false, 10, true, true, true, true, false, true, appMgr, iconMgr, colIdFactory);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		super.createLabels();
		typeLbl = new JLabel("Type");
		separationLbl = new JLabel("Separation (0.0-0.5)");
	}
	
	@Override
	protected ColorScheme[] getColorSchemes() {
		final BarChartType type = chart.get(BarChart.TYPE, BarChartType.class, BarChartType.GROUPED);
		
		return type == BarChartType.HEAT_STRIPS ? 
				HEAT_STRIP_COLOR_SCHEMES : 
				(type == BarChartType.STACKED ? STACKED_COLOR_SCHEMES : GROUPED_COLOR_SCHEMES);
	}
	
	@Override
	protected JPanel getOtherBasicOptionsPnl() {
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(typeLbl)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getGroupedRd())
						.addComponent(getStackedRd())
						.addComponent(getHeatStripsRd()))
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(typeLbl)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getGroupedRd())
						.addComponent(getStackedRd())
						.addComponent(getHeatStripsRd()))
		);
		
		return p;
	}
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		final JPanel p = super.getOtherBasicOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(separationLbl)
					.addComponent(getSeparationTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
				          GroupLayout.PREFERRED_SIZE)
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
		}
		
		return typeGrp;
	}
	
	private JRadioButton getGroupedRd() {
		if (groupedRd == null) {
			groupedRd = new JRadioButton("Grouped");
			
			groupedRd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setType();
				}
			});
		}
		
		return groupedRd;
	}
	
	private JRadioButton getStackedRd() {
		if (stackedRd == null) {
			stackedRd = new JRadioButton("Stacked");
			
			stackedRd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setType();
				}
			});
		}
		
		return stackedRd;
	}
	
	public JRadioButton getHeatStripsRd() {
		if (heatStripsRd == null) {
			heatStripsRd = new JRadioButton("Heat Strips");
			
			heatStripsRd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setType();
				}
			});
		}
		
		return heatStripsRd;
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
				public void focusLost(final FocusEvent e) {
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
		else
			type = BarChartType.GROUPED;
		
		chart.set(BarChart.TYPE, type);
		updateRangeMinMax(true);
		getColorSchemeEditor().setColorSchemes(getColorSchemes());
	}
	
	protected void updateType() {
		final BarChartType type = chart.get(BarChart.TYPE, BarChartType.class, BarChartType.GROUPED);
		final JRadioButton typeRd;
		
		if (type == BarChartType.STACKED)
			typeRd = getStackedRd();
		else if (type == BarChartType.HEAT_STRIPS)
			typeRd = getHeatStripsRd();
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
	protected double[] minMax(double min, double max, final List<? extends Number> values) {
		if (values != null) {
			final boolean stacked = getStackedRd().isSelected();
			double sum = 0;
			
			for (final Number v : values) {
				final double dv = v.doubleValue();
				
				if (stacked) {
					sum += dv;
				} else {
					min = Math.min(min, dv);
					max = Math.max(max, dv);
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
			colorSchemeEditor = new BarColorSchemeEditor(chart, getColorSchemes(), appMgr.getCurrentNetwork(), iconMgr);
		}
		
		return colorSchemeEditor;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class BarColorSchemeEditor extends ColorSchemeEditor<BarChart> {

		private static final long serialVersionUID = 1174473101447051638L;

		public BarColorSchemeEditor(final BarChart chart, final ColorScheme[] colorSchemes, final CyNetwork network,
				final IconManager iconMgr) {
			super(chart, colorSchemes, false, network, iconMgr);
		}

		@Override
		protected int getTotal() {
			final BarChartType type = chart.get(BarChart.TYPE, BarChartType.class, BarChartType.GROUPED);
			
			return type == BarChartType.HEAT_STRIPS ? 3 : super.getTotal();
		}
	}
}
