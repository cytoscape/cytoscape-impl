package org.cytoscape.view.table.internal.cg.sparkline.bar;

import static org.cytoscape.view.table.internal.cg.ColorScheme.CONTRASTING;
import static org.cytoscape.view.table.internal.cg.ColorScheme.CUSTOM;
import static org.cytoscape.view.table.internal.cg.ColorScheme.MODULATED;
import static org.cytoscape.view.table.internal.cg.ColorScheme.RAINBOW;
import static org.cytoscape.view.table.internal.cg.ColorScheme.RANDOM;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.table.internal.cg.ColorScheme;
import org.cytoscape.view.table.internal.cg.sparkline.AbstractSparklineEditor;
import org.cytoscape.view.table.internal.cg.sparkline.ColorSchemeEditor;
import org.cytoscape.view.table.internal.cg.sparkline.bar.BarSparkline.BarSparklineType;
import org.cytoscape.view.table.internal.util.ColorGradient;

@SuppressWarnings("serial")
public class BarSparklineEditor extends AbstractSparklineEditor<BarSparkline> {

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
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BarSparklineEditor(BarSparkline chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, Number.class, false, true, true, true, true, false, true, true, serviceRegistrar);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		super.createLabels();
	}
	
	@Override
	protected ColorScheme[] getColorSchemes() {
		var type = chart.get(BarSparkline.TYPE, BarSparklineType.class, BarSparklineType.GROUPED);
		
		return type == BarSparklineType.HEAT_STRIPS ? 
				HEAT_STRIP_COLOR_SCHEMES : 
				(type == BarSparklineType.UP_DOWN ? UP_DOWN_COLOR_SCHEMES : REGULAR_COLOR_SCHEMES);
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
				).addComponent(sep)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getGroupedRd())
						.addComponent(getStackedRd())
						.addComponent(getHeatStripsRd())
						.addComponent(getUpDownRd())
				).addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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
	
	public JRadioButton getUpDownRd() {
		if (upDownRd == null) {
			upDownRd = new JRadioButton("Up-Down");
			
			upDownRd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setType();
				}
			});
		}
		
		return upDownRd;
	}
	
	private void setType() {
		final BarSparklineType type;
		
		if (getStackedRd().isSelected())
			type = BarSparklineType.STACKED;
		else if (getHeatStripsRd().isSelected())
			type = BarSparklineType.HEAT_STRIPS;
		else if (getUpDownRd().isSelected())
			type = BarSparklineType.UP_DOWN;
		else
			type = BarSparklineType.GROUPED;
		
		chart.set(BarSparkline.TYPE, type);
		updateRangeMinMax(true);
		getColorSchemeEditor().setColorSchemes(getColorSchemes());
	}
	
	protected void updateType() {
		var type = chart.get(BarSparkline.TYPE, BarSparklineType.class, BarSparklineType.GROUPED);
		final JRadioButton typeRd;
		
		if (type == BarSparklineType.STACKED)
			typeRd = getStackedRd();
		else if (type == BarSparklineType.HEAT_STRIPS)
			typeRd = getHeatStripsRd();
		else if (type == BarSparklineType.UP_DOWN)
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
	protected double[] minMax(double min, double max, List<? extends Number> values) {
		if (values != null) {
			boolean stacked = getStackedRd().isSelected();
			double sum = 0;
			
			for (Number v : values) {
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
	protected ColorSchemeEditor<BarSparkline> getColorSchemeEditor() {
		if (colorSchemeEditor == null) {
			colorSchemeEditor = new BarColorSchemeEditor(
					chart,
					getColorSchemes(),
					serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork(),
					serviceRegistrar.getService(IconManager.class)
			);
		}
		
		return colorSchemeEditor;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class BarColorSchemeEditor extends ColorSchemeEditor<BarSparkline> {

		public BarColorSchemeEditor(BarSparkline chart, ColorScheme[] colorSchemes, CyNetwork network,
				IconManager iconMgr) {
			super(chart, colorSchemes, false, network, iconMgr);
		}

		@Override
		protected int getTotal() {
			var type = chart.get(BarSparkline.TYPE, BarSparklineType.class, BarSparklineType.GROUPED);
			
			if (type == BarSparklineType.HEAT_STRIPS)
				return total = 3;
			if (type == BarSparklineType.UP_DOWN)
				return total = 2;
			
			return super.getTotal();
		}
		
		@Override
		protected void style(ColorPanel cp, int index) {
			super.style(cp, index);
			var type = chart.get(BarSparkline.TYPE, BarSparklineType.class, BarSparklineType.GROUPED);
			
			if (type == BarSparklineType.HEAT_STRIPS || type == BarSparklineType.UP_DOWN) {
				cp.setFont(iconMgr.getIconFont(11));
				String label = "";
				String toolTip = null;
				
				if (index == 0) {
					label = IconManager.ICON_ARROW_UP;
					toolTip = "Positive Numbers";
				} else if (index == 1) {
					label = type == BarSparklineType.UP_DOWN ? IconManager.ICON_ARROW_DOWN : "";
					toolTip = type == BarSparklineType.UP_DOWN ? "Negative Numbers" : "Zero";
				} else if (index == 2 && type == BarSparklineType.HEAT_STRIPS) {
					label = IconManager.ICON_ARROW_DOWN;
					toolTip = "Negative Numbers";
				}
				
				cp.setText(label);
				cp.setToolTipText(toolTip);
			}
		}
	}
}
