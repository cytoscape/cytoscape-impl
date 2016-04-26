package org.cytoscape.ding.internal.charts;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.ding.customgraphics.AbstractCustomGraphics2.ORIENTATION;
import static org.cytoscape.ding.customgraphics.ColorScheme.CONTRASTING;
import static org.cytoscape.ding.customgraphics.ColorScheme.CUSTOM;
import static org.cytoscape.ding.customgraphics.ColorScheme.MODULATED;
import static org.cytoscape.ding.customgraphics.ColorScheme.RAINBOW;
import static org.cytoscape.ding.customgraphics.ColorScheme.RANDOM;
import static org.cytoscape.ding.internal.charts.AbstractChart.AUTO_RANGE;
import static org.cytoscape.ding.internal.charts.AbstractChart.AXIS_COLOR;
import static org.cytoscape.ding.internal.charts.AbstractChart.AXIS_LABEL_FONT_SIZE;
import static org.cytoscape.ding.internal.charts.AbstractChart.AXIS_WIDTH;
import static org.cytoscape.ding.internal.charts.AbstractChart.BORDER_COLOR;
import static org.cytoscape.ding.internal.charts.AbstractChart.BORDER_WIDTH;
import static org.cytoscape.ding.internal.charts.AbstractChart.DATA_COLUMNS;
import static org.cytoscape.ding.internal.charts.AbstractChart.DOMAIN_LABELS_COLUMN;
import static org.cytoscape.ding.internal.charts.AbstractChart.DOMAIN_LABEL_POSITION;
import static org.cytoscape.ding.internal.charts.AbstractChart.GLOBAL_RANGE;
import static org.cytoscape.ding.internal.charts.AbstractChart.ITEM_LABELS_COLUMN;
import static org.cytoscape.ding.internal.charts.AbstractChart.ITEM_LABEL_FONT_SIZE;
import static org.cytoscape.ding.internal.charts.AbstractChart.RANGE;
import static org.cytoscape.ding.internal.charts.AbstractChart.RANGE_LABELS_COLUMN;
import static org.cytoscape.ding.internal.charts.AbstractChart.SHOW_DOMAIN_AXIS;
import static org.cytoscape.ding.internal.charts.AbstractChart.SHOW_ITEM_LABELS;
import static org.cytoscape.ding.internal.charts.AbstractChart.SHOW_RANGE_AXIS;
import static org.cytoscape.ding.internal.charts.AbstractChart.SHOW_RANGE_ZERO_BASELINE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.isWinLAF;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2;
import org.cytoscape.ding.customgraphics.ColorScheme;
import org.cytoscape.ding.customgraphics.Orientation;
import org.cytoscape.ding.internal.util.SortedListModel;
import org.cytoscape.ding.internal.util.SortedListModel.SortOrder;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public abstract class AbstractChartEditor<T extends AbstractCustomGraphics2<?>> extends JPanel {

	private static final long serialVersionUID = 2022740799541917592L;
	
	private static final ColorScheme[] BASIC_COLOR_SCHEMES = new ColorScheme[] {
		CONTRASTING, MODULATED, RAINBOW, RANDOM, CUSTOM
	};
	
	protected static Double[] ANGLES = new Double[] { 0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0 };
	
	private JTabbedPane optionsTpn;
	private JPanel basicOptionsPnl;
	private JPanel advancedOptionsPnl;
	private DataPanel dataPnl;
	private JPanel rangePnl;
	private JPanel labelsPnl;
	private JPanel orientationPnl;
	private JPanel axesPnl;
	private JPanel borderPnl;
	protected ColorSchemeEditor<T> colorSchemeEditor;
	private JPanel otherBasicOptionsPnl;
	private JPanel otherAdvancedOptionsPnl;
	protected JLabel itemLabelsColumnLbl;
	protected JLabel domainLabelsColumnLbl;
	protected JLabel rangeLabelsColumnLbl;
	private JComboBox<CyColumnIdentifier> itemLabelsColumnCmb;
	private JComboBox<CyColumnIdentifier> domainLabelsColumnCmb;
	private JComboBox<CyColumnIdentifier> rangeLabelsColumnCmb;
	protected JLabel domainLabelPositionLbl;
	protected JComboBox<LabelPosition> domainLabelPositionCmb;
	private JCheckBox globalRangeCkb;
	private JCheckBox autoRangeCkb;
	private JLabel rangeMinLbl;
	private JTextField rangeMinTxt;
	private JButton refreshRangeBtn;
	private JLabel rangeMaxLbl;
	private JTextField rangeMaxTxt;
	private JCheckBox itemLabelsVisibleCkb;
	private JCheckBox domainAxisVisibleCkb;
	private JCheckBox rangeAxisVisibleCkb;
	private JCheckBox rangeZeroBaselineVisibleCkb;
	private JLabel itemFontSizeLbl;
	private JTextField itemFontSizeTxt;
	private JLabel axisWidthLbl;
	private JTextField axisWidthTxt;
	private JLabel axisColorLbl;
	private JLabel axisFontSizeLbl;
	private JTextField axisFontSizeTxt;
	private ColorButton axisColorBtn;
	private ButtonGroup orientationGrp;
	private JRadioButton verticalRd;
	private JRadioButton horizontalRd;
	private JLabel borderWidthLbl;
	private JTextField borderWidthTxt;
	private JLabel borderColorLbl;
	private ColorButton borderColorBtn;
	
	protected final boolean columnIsSeries;
	protected final boolean setRange;
	protected final boolean setOrientation;
	protected final boolean setItemLabels;
	protected final boolean setDomainLabels;
	protected final boolean setRangeLabels;
	protected final boolean hasAxes;
	protected final boolean hasZeroBaseline;
	protected final Map<CyColumnIdentifier, CyColumn> columns;
	protected final Map<CyColumnIdentifier, CyColumn> labelColumns;
	protected final T chart;
	protected final Class<?> dataType;
	protected final CyApplicationManager appMgr;
	protected final IconManager iconMgr;
	protected final CyColumnIdentifierFactory colIdFactory;

	protected boolean initializing;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	protected AbstractChartEditor(final T chart,
								  final Class<?> dataType,
								  final boolean columnIsSeries,
								  final boolean setRange,
								  final boolean setOrientation,
								  final boolean setItemLabels,
								  final boolean setDomainLabels,
								  final boolean setRangeLabels,
								  final boolean hasAxes,
								  final boolean hasZeroBaseline,
								  final CyApplicationManager appMgr,
								  final IconManager iconMgr,
								  final CyColumnIdentifierFactory colIdFactory) {
		if (chart == null)
			throw new IllegalArgumentException("'chart' argument must not be null.");
		if (dataType == null)
			throw new IllegalArgumentException("'dataType' argument must not be null.");
		if (appMgr == null)
			throw new IllegalArgumentException("'appMgr' argument must not be null.");
		if (iconMgr == null)
			throw new IllegalArgumentException("'iconMgr' argument must not be null.");
		if (colIdFactory == null)
			throw new IllegalArgumentException("'colIdFactory' argument must not be null.");
		
		this.chart = chart;
		this.columnIsSeries = columnIsSeries;
		this.dataType = dataType;
		this.setRange = setRange;
		this.setOrientation = setOrientation;
		this.setItemLabels = setItemLabels;
		this.setDomainLabels = setDomainLabels;
		this.setRangeLabels = setRangeLabels;
		this.hasAxes = hasAxes;
		this.hasZeroBaseline = hasZeroBaseline;
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
		this.colIdFactory = colIdFactory;
		
		final Comparator<CyColumnIdentifier> columnComparator = new ColumnComparator();
		columns = new TreeMap<CyColumnIdentifier, CyColumn>(columnComparator);
		labelColumns = new TreeMap<CyColumnIdentifier, CyColumn>(columnComparator);
		
		// TODO Move it to a shared "Chart Column Manager"
		final CyNetwork net = appMgr.getCurrentNetwork();
		
		if (net != null) {
			final CyTable table = net.getDefaultNodeTable(); // TODO only node table for now, but may get edge table in the future
			final Collection<CyColumn> cols = table.getColumns();
			
			for (final CyColumn c : cols) {
				if (c.getName() != CyIdentifiable.SUID) {
					final CyColumnIdentifier colId = colIdFactory.createColumnIdentifier(c.getName());
					columns.put(colId, c);
					
					if (List.class.isAssignableFrom(c.getType()))
						labelColumns.put(colId, c);
				}
			}
		}
		
		init();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	protected void init() {
		initializing = true;
		
		try {
			createLabels();
			setOpaque(!isAquaLAF()); // Transparent if Aqua
			setLayout(new BorderLayout());
			add(getOptionsTpn(), BorderLayout.CENTER);
		} finally {
			initializing = false;
		}
		
		update(false);
	}
	
	protected void createLabels() {
		itemLabelsColumnLbl = new JLabel("Column:");
		itemFontSizeLbl = new JLabel("Font Size:");
		domainLabelsColumnLbl = new JLabel("Domain Labels Column:");
		rangeLabelsColumnLbl = new JLabel("Range Labels Column:");
		domainLabelPositionLbl = new JLabel("Domain Label Position:");
		rangeMinLbl = new JLabel("Min:");
		rangeMaxLbl = new JLabel("Max:");
		axisWidthLbl = new JLabel("Axis Width:");
		axisColorLbl = new JLabel("Axis Color:");
		axisFontSizeLbl = new JLabel("Axis Font Size:");
		borderWidthLbl = new JLabel("Border Width:");
		borderColorLbl = new JLabel("Border Color:");
	}

	protected JTabbedPane getOptionsTpn() {
		if (optionsTpn == null) {
			optionsTpn = new JTabbedPane(JTabbedPane.LEFT);
			optionsTpn.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			
			JScrollPane scr1 = new JScrollPane(getBasicOptionsPnl(), 
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scr1.setBorder(BorderFactory.createEmptyBorder());
			scr1.setOpaque(!isAquaLAF()); // Transparent if Aqua
			scr1.getViewport().setOpaque(!isAquaLAF());
			
			JScrollPane scr2 = new JScrollPane(getAdvancedOptionsPnl(), 
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scr2.setBorder(BorderFactory.createEmptyBorder());
			scr2.setOpaque(!isAquaLAF());
			scr2.getViewport().setOpaque(!isAquaLAF());
			
			optionsTpn.addTab("Data", scr1);
			optionsTpn.addTab("Options", scr2);
		}
		
		return optionsTpn;
	}
	
	protected JPanel getBasicOptionsPnl() {
		if (basicOptionsPnl == null) {
			basicOptionsPnl = new JPanel();
			basicOptionsPnl.setOpaque(!isAquaLAF()); // Transparent if Aqua
			
			final GroupLayout layout = new GroupLayout(basicOptionsPnl);
			basicOptionsPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(getOtherBasicOptionsPnl())
					.addComponent(getDataPnl())
					.addComponent(getRangePnl())
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getOtherBasicOptionsPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDataPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getRangePnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return basicOptionsPnl;
	}
	
	protected JPanel getAdvancedOptionsPnl() {
		if (advancedOptionsPnl == null) {
			advancedOptionsPnl = new JPanel();
			advancedOptionsPnl.setOpaque(!isAquaLAF()); // Transparent if Aqua
			
			final GroupLayout layout = new GroupLayout(advancedOptionsPnl);
			advancedOptionsPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(getColorSchemeEditor())
					.addComponent(getLabelsPnl())
					.addComponent(getOrientationPnl())
					.addComponent(getAxesPnl())
					.addComponent(getBorderPnl())
					.addComponent(getOtherAdvancedOptionsPnl())
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getColorSchemeEditor())
					.addComponent(getLabelsPnl())
					.addComponent(getOrientationPnl())
					.addComponent(getAxesPnl())
					.addComponent(getBorderPnl())
					.addComponent(getOtherAdvancedOptionsPnl())
			);
		}
		
		return advancedOptionsPnl;
	}
	
	protected DataPanel getDataPnl() {
		if (dataPnl == null) {
			dataPnl = new DataPanel();
			dataPnl.setOpaque(!isAquaLAF()); // Transparent if Aqua
			dataPnl.refresh();
		}
		
		return dataPnl;
	}
	
	protected JPanel getRangePnl() {
		if (rangePnl == null) {
			rangePnl = new JPanel();
			rangePnl.setOpaque(!isAquaLAF()); // Transparent if Aqua
			rangePnl.setVisible(setRange);
			
			if (!rangePnl.isVisible())
				return rangePnl;
			
			final GroupLayout layout = new GroupLayout(rangePnl);
			rangePnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			final JSeparator sep = new JSeparator();
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(getGlobalRangeCkb())
					.addGroup(layout.createSequentialGroup()
							.addComponent(getAutoRangeCkb())
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(rangeMinLbl)
							.addComponent(getRangeMinTxt())
							.addComponent(rangeMaxLbl)
							.addComponent(getRangeMaxTxt())
							.addComponent(getRefreshRangeBtn())
					).addComponent(sep)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getGlobalRangeCkb())
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(getAutoRangeCkb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(rangeMinLbl)
							.addComponent(getRangeMinTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(rangeMaxLbl)
							.addComponent(getRangeMaxTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getRefreshRangeBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					).addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return rangePnl;
	}
	
	protected JPanel getLabelsPnl() {
		if (labelsPnl == null) {
			labelsPnl = new JPanel();
			labelsPnl.setOpaque(!isAquaLAF()); // Transparent if Aqua
			labelsPnl.setVisible(setItemLabels || setDomainLabels || setRangeLabels);
			
			if (!labelsPnl.isVisible())
				return labelsPnl;
			
			final GroupLayout layout = new GroupLayout(labelsPnl);
			labelsPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			final ParallelGroup hGroup = layout.createParallelGroup(Alignment.LEADING, true);
			final SequentialGroup vGroup = layout.createSequentialGroup();
			layout.setHorizontalGroup(hGroup);
			layout.setVerticalGroup(vGroup);
			
			if (setItemLabels) {
				hGroup.addGroup(layout.createSequentialGroup()
						.addComponent(getItemLabelsVisibleCkb())
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(itemLabelsColumnLbl)
						.addComponent(getItemLabelsColumnCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(itemFontSizeLbl)
						.addComponent(getItemFontSizeTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE));
				vGroup.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getItemLabelsVisibleCkb())
						.addComponent(itemLabelsColumnLbl)
						.addComponent(getItemLabelsColumnCmb())
						.addComponent(itemFontSizeLbl)
						.addComponent(getItemFontSizeTxt()));
			}
			
			if (setRangeLabels) {
				hGroup.addGroup(layout.createSequentialGroup()
						.addComponent(rangeLabelsColumnLbl)
						.addComponent(getRangeLabelsColumnCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE));
				vGroup.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(rangeLabelsColumnLbl)
						.addComponent(getRangeLabelsColumnCmb()));
					
			}
			
			if (setDomainLabels) {
				hGroup.addGroup(layout.createSequentialGroup()
						.addComponent(domainLabelsColumnLbl)
						.addComponent(getDomainLabelsColumnCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE));
				vGroup.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(domainLabelsColumnLbl)
						.addComponent(getDomainLabelsColumnCmb()));
			}
			
			hGroup.addGroup(layout.createSequentialGroup()
					.addComponent(domainLabelPositionLbl)
					.addComponent(getDomainLabelPositionCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE));
			vGroup.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
					.addComponent(domainLabelPositionLbl)
					.addComponent(getDomainLabelPositionCmb()));
			
			final JSeparator sep = new JSeparator();
			
			hGroup.addComponent(sep);
			vGroup.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		}
		
		return labelsPnl;
	}
	
	protected JPanel getOrientationPnl() {
		if (orientationPnl == null) {
			orientationPnl = new JPanel();
			orientationPnl.setOpaque(!isAquaLAF()); // Transparent if Aqua
			orientationPnl.setVisible(setOrientation);
			
			if (!orientationPnl.isVisible())
				return orientationPnl;
			
			final GroupLayout layout = new GroupLayout(orientationPnl);
			orientationPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			final JSeparator sep = new JSeparator();
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(getVerticalRd())
							.addComponent(getHorizontalRd())
					).addComponent(sep)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(getVerticalRd())
							.addComponent(getHorizontalRd())
					).addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return orientationPnl;
	}
	
	protected JPanel getAxesPnl() {
		if (axesPnl == null) {
			axesPnl = new JPanel();
			axesPnl.setOpaque(!isAquaLAF()); // Transparent if Aqua
			axesPnl.setVisible(hasAxes);
			
			if (!axesPnl.isVisible())
				return axesPnl;
			
			final GroupLayout layout = new GroupLayout(axesPnl);
			axesPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			final JSeparator vsep = new JSeparator(JSeparator.VERTICAL);
			final JSeparator sep = new JSeparator();
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(getDomainAxisVisibleCkb())
							.addComponent(getRangeAxisVisibleCkb())
							.addComponent(getRangeZeroBaselineVisibleCkb())
						)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(vsep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addGroup(layout.createSequentialGroup()
								.addComponent(axisWidthLbl)
								.addComponent(getAxisWidthTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							)
							.addGroup(layout.createSequentialGroup()
								.addComponent(axisColorLbl)
								.addComponent(getAxisColorBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							).addGroup(layout.createSequentialGroup()
								.addComponent(axisFontSizeLbl)
								.addComponent(getAxisFontSizeTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							)
						)
					)
					.addComponent(sep)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addGroup(layout.createSequentialGroup()
							.addComponent(getDomainAxisVisibleCkb())
							.addComponent(getRangeAxisVisibleCkb())
							.addComponent(getRangeZeroBaselineVisibleCkb())
						)
						.addComponent(vsep)
						.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
								.addComponent(axisWidthLbl)
								.addComponent(getAxisWidthTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							)
							.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
								.addComponent(axisColorLbl)
								.addComponent(getAxisColorBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							)
							.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
									.addComponent(axisFontSizeLbl)
									.addComponent(getAxisFontSizeTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							)
						)
					)
					.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return axesPnl;
	}
	
	protected JPanel getBorderPnl() {
		if (borderPnl == null) {
			borderPnl = new JPanel();
			borderPnl.setOpaque(!isAquaLAF()); // Transparent if Aqua
			
			final GroupLayout layout = new GroupLayout(borderPnl);
			borderPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			final JSeparator sep = new JSeparator();
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(layout.createSequentialGroup()
						.addComponent(borderWidthLbl)
						.addComponent(getBorderWidthTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createSequentialGroup()
						.addComponent(borderColorLbl)
						.addComponent(getBorderColorBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					).addComponent(sep)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(borderWidthLbl)
						.addComponent(getBorderWidthTxt())
					).addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(borderColorLbl)
						.addComponent(getBorderColorBtn())
					).addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return borderPnl;
	}
	
	protected ColorSchemeEditor<T> getColorSchemeEditor() {
		if (colorSchemeEditor == null) {
			colorSchemeEditor = new ColorSchemeEditor<T>(chart, getColorSchemes(), columnIsSeries,
					appMgr.getCurrentNetwork(), iconMgr);
		}
		
		return colorSchemeEditor;
	}
	
	/**
	 * Should be overridden by the concrete subclass if it provides extra fields.
	 * @return
	 */
	protected JPanel getOtherBasicOptionsPnl() {
		if (otherBasicOptionsPnl == null) {
			otherBasicOptionsPnl = new JPanel();
			otherBasicOptionsPnl.setOpaque(!isAquaLAF()); // Transparent if Aqua
			otherBasicOptionsPnl.setVisible(false);
		}
		
		return otherBasicOptionsPnl;
	}
	
	/**
	 * Should be overridden by the concrete subclass if it provides extra fields.
	 * @return
	 */
	protected JPanel getOtherAdvancedOptionsPnl() {
		if (otherAdvancedOptionsPnl == null) {
			otherAdvancedOptionsPnl = new JPanel();
			otherAdvancedOptionsPnl.setOpaque(!isAquaLAF()); // Transparent if Aqua
			otherAdvancedOptionsPnl.setVisible(false);
		}
		
		return otherAdvancedOptionsPnl;
	}
	
	protected JComboBox<CyColumnIdentifier> getItemLabelsColumnCmb() {
		if (itemLabelsColumnCmb == null) {
			itemLabelsColumnCmb = new CyColumnComboBox(labelColumns.keySet(), true);
			selectColumnIdItem(itemLabelsColumnCmb, chart.get(ITEM_LABELS_COLUMN, CyColumnIdentifier.class));
			
			itemLabelsColumnCmb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final CyColumnIdentifier colId = (CyColumnIdentifier) itemLabelsColumnCmb.getSelectedItem();
					chart.set(ITEM_LABELS_COLUMN, colId != null ? colId.getColumnName() : null);
				}
			});
		}

		return itemLabelsColumnCmb;
	}
	
	protected JComboBox<CyColumnIdentifier> getDomainLabelsColumnCmb() {
		if (domainLabelsColumnCmb == null) {
			domainLabelsColumnCmb = new CyColumnComboBox(labelColumns.keySet(), true);
			selectColumnIdItem(domainLabelsColumnCmb, chart.get(DOMAIN_LABELS_COLUMN, CyColumnIdentifier.class));
			
			domainLabelsColumnCmb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final CyColumnIdentifier colId = (CyColumnIdentifier) domainLabelsColumnCmb.getSelectedItem();
					chart.set(DOMAIN_LABELS_COLUMN, colId != null ? colId.getColumnName() : null);
				}
			});
		}
		
		return domainLabelsColumnCmb;
	}
	
	protected JComboBox<CyColumnIdentifier> getRangeLabelsColumnCmb() {
		if (rangeLabelsColumnCmb == null) {
			rangeLabelsColumnCmb = new CyColumnComboBox(labelColumns.keySet(), true);
			selectColumnIdItem(rangeLabelsColumnCmb, chart.get(RANGE_LABELS_COLUMN, CyColumnIdentifier.class));
			
			rangeLabelsColumnCmb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final CyColumnIdentifier colId = (CyColumnIdentifier) rangeLabelsColumnCmb.getSelectedItem();
					chart.set(RANGE_LABELS_COLUMN, colId != null ? colId.getColumnName() : null);
				}
			});
		}
		
		return rangeLabelsColumnCmb;
	}
	
	protected JCheckBox getGlobalRangeCkb() {
		if (globalRangeCkb == null) {
			globalRangeCkb = new JCheckBox("Network-Wide Axis Range");
			globalRangeCkb.setSelected(chart.get(GLOBAL_RANGE, Boolean.class, Boolean.TRUE));
			globalRangeCkb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final boolean selected = e.getStateChange() == ItemEvent.SELECTED;
					chart.set(GLOBAL_RANGE, selected);
					updateGlobalRange();
					
					if (selected)
						updateRangeMinMax(chart.getList(RANGE, Double.class).isEmpty());
				}
			});
		}
		
		return globalRangeCkb;
	}
	
	protected JCheckBox getAutoRangeCkb() {
		if (autoRangeCkb == null) {
			autoRangeCkb = new JCheckBox("Automatic Range");
			autoRangeCkb.setSelected(chart.get(AUTO_RANGE, Boolean.class, Boolean.TRUE));
			autoRangeCkb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final boolean selected = e.getStateChange() == ItemEvent.SELECTED;
					getRangeMinTxt().setEnabled(!selected);
					getRangeMaxTxt().setEnabled(!selected);
					getRangeMinTxt().requestFocus();
					chart.set(AUTO_RANGE, selected);
					
					if (selected)
						updateRangeMinMax(true);
				}
			});
		}
		
		return autoRangeCkb;
	}
	
	protected JTextField getRangeMinTxt() {
		if (rangeMinTxt == null) {
			rangeMinTxt = new JTextField();
			final boolean auto = chart.get(AUTO_RANGE, Boolean.class, Boolean.TRUE);
			rangeMinTxt.setEnabled(!auto);
			rangeMinTxt.setInputVerifier(new DoubleInputVerifier());
			rangeMinTxt.setMinimumSize(new Dimension(60, rangeMinTxt.getMinimumSize().height));
			rangeMinTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			rangeMinTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					setGlobalRange();
				}
			});
		}
		
		return rangeMinTxt;
	}
	
	protected JTextField getRangeMaxTxt() {
		if (rangeMaxTxt == null) {
			rangeMaxTxt = new JTextField();
			final boolean auto = chart.get(AUTO_RANGE, Boolean.class, Boolean.TRUE);
			rangeMaxTxt.setEnabled(!auto);
			rangeMaxTxt.setInputVerifier(new DoubleInputVerifier());
			rangeMaxTxt.setMinimumSize(new Dimension(60, rangeMaxTxt.getMinimumSize().height));
			rangeMaxTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			rangeMaxTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					setGlobalRange();
				}
			});
		}
		
		return rangeMaxTxt;
	}
	
	protected JButton getRefreshRangeBtn() {
		if (refreshRangeBtn == null) {
			refreshRangeBtn = new JButton(IconManager.ICON_REFRESH);
			refreshRangeBtn.setFont(iconMgr.getIconFont(12.0f));
			refreshRangeBtn.setToolTipText("Refresh automatic range values");
			
			refreshRangeBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					updateRangeMinMax(true);
					refreshRangeBtn.setEnabled(false);
				}
			});
		}
		
		return refreshRangeBtn;
	}
	
	protected JCheckBox getItemLabelsVisibleCkb() {
		if (itemLabelsVisibleCkb == null) {
			itemLabelsVisibleCkb = new JCheckBox("Show Value Labels");
			itemLabelsVisibleCkb.setVisible(setItemLabels);
			
			if (setItemLabels) {
				itemLabelsVisibleCkb.setSelected(chart.get(SHOW_ITEM_LABELS, Boolean.class, false));
				itemLabelsVisibleCkb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						chart.set(SHOW_ITEM_LABELS, itemLabelsVisibleCkb.isSelected());
						
						itemLabelsColumnLbl.setEnabled(itemLabelsVisibleCkb.isSelected());
						getItemLabelsColumnCmb().setEnabled(itemLabelsVisibleCkb.isSelected());
						
						itemFontSizeLbl.setEnabled(itemLabelsVisibleCkb.isSelected());
						getItemFontSizeTxt().setEnabled(itemLabelsVisibleCkb.isSelected());
					}
				});
			}
		}
		
		return itemLabelsVisibleCkb;
	}
	
	protected JCheckBox getDomainAxisVisibleCkb() {
		if (domainAxisVisibleCkb == null) {
			domainAxisVisibleCkb = new JCheckBox("Show Domain Axis");
			domainAxisVisibleCkb.setVisible(hasAxes);
			
			if (hasAxes) {
				domainAxisVisibleCkb.setSelected(chart.get(SHOW_DOMAIN_AXIS, Boolean.class, false));
				domainAxisVisibleCkb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						chart.set(SHOW_DOMAIN_AXIS, domainAxisVisibleCkb.isSelected());
					}
				});
			}
		}
		
		return domainAxisVisibleCkb;
	}
	
	protected JCheckBox getRangeAxisVisibleCkb() {
		if (rangeAxisVisibleCkb == null) {
			rangeAxisVisibleCkb = new JCheckBox("Show Range Axis");
			rangeAxisVisibleCkb.setVisible(hasAxes);
			
			if (hasAxes) {
				rangeAxisVisibleCkb.setSelected(chart.get(SHOW_RANGE_AXIS, Boolean.class, false));
				rangeAxisVisibleCkb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						chart.set(SHOW_RANGE_AXIS, rangeAxisVisibleCkb.isSelected());
					}
				});
			}
		}
		
		return rangeAxisVisibleCkb;
	}
	
	protected JCheckBox getRangeZeroBaselineVisibleCkb() {
		if (rangeZeroBaselineVisibleCkb == null) {
			rangeZeroBaselineVisibleCkb = new JCheckBox("Show Zero Baseline");
			rangeZeroBaselineVisibleCkb.setVisible(hasZeroBaseline);
			
			if (hasAxes) {
				rangeZeroBaselineVisibleCkb.setSelected(chart.get(SHOW_RANGE_ZERO_BASELINE, Boolean.class, false));
				rangeZeroBaselineVisibleCkb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						chart.set(SHOW_RANGE_ZERO_BASELINE, rangeZeroBaselineVisibleCkb.isSelected());
					}
				});
			}
		}
		
		return rangeZeroBaselineVisibleCkb;
	}
	
	@SuppressWarnings("serial")
	public JComboBox<LabelPosition> getDomainLabelPositionCmb() {
		if (domainLabelPositionCmb == null) {
			domainLabelPositionCmb = new JComboBox<>(LabelPosition.values());
			
			domainLabelPositionCmb.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					final JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
							cellHasFocus);
					if (value instanceof LabelPosition)
						lbl.setText(((LabelPosition)value).getLabel());
					
					return lbl;
				}
			});
			
			domainLabelPositionCmb.setSelectedItem(
					chart.get(DOMAIN_LABEL_POSITION, LabelPosition.class, LabelPosition.STANDARD));
			
			domainLabelPositionCmb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final LabelPosition position = (LabelPosition) domainLabelPositionCmb.getSelectedItem();
					chart.set(DOMAIN_LABEL_POSITION, position);
				}
			});
		}
		
		return domainLabelPositionCmb;
	}
	
	protected JTextField getAxisWidthTxt() {
		if (axisWidthTxt == null) {
			axisWidthTxt = new JTextField("" + chart.get(AXIS_WIDTH, Float.class, 0.25f));
			axisWidthTxt.setInputVerifier(new DoubleInputVerifier());
			axisWidthTxt.setPreferredSize(new Dimension(60, axisWidthTxt.getMinimumSize().height));
			axisWidthTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			axisWidthTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					try {
						float v = Float.parseFloat(axisWidthTxt.getText());
			            chart.set(AXIS_WIDTH, v);
			        } catch (NumberFormatException nfe) {
			        }
				}
			});
		}
		
		return axisWidthTxt;
	}
	
	protected ColorButton getAxisColorBtn() {
		if (axisColorBtn == null) {
			final Color color = chart.get(AXIS_COLOR, Color.class, Color.DARK_GRAY);
			axisColorBtn = new ColorButton(color);
			axisColorBtn.setVisible(hasAxes);
			
			axisColorBtn.addPropertyChangeListener("color", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent e) {
					final Color newColor = (Color) e.getNewValue();
					chart.set(AXIS_COLOR, newColor);
				}
			});
		}
		
		return axisColorBtn;
	}
	
	protected JTextField getItemFontSizeTxt() {
		if (itemFontSizeTxt == null) {
			itemFontSizeTxt = new JTextField("" + chart.get(ITEM_LABEL_FONT_SIZE, Integer.class, 1));
			itemFontSizeTxt.setInputVerifier(new IntInputVerifier());
			itemFontSizeTxt.setPreferredSize(new Dimension(40, itemFontSizeTxt.getMinimumSize().height));
			itemFontSizeTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			itemFontSizeTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					try {
						int v = Integer.parseInt(itemFontSizeTxt.getText());
			            chart.set(ITEM_LABEL_FONT_SIZE, v);
			        } catch (NumberFormatException nfe) {
			        }
				}
			});
		}
		
 		return itemFontSizeTxt;
	}
	
	protected JTextField getAxisFontSizeTxt() {
		if (axisFontSizeTxt == null) {
			axisFontSizeTxt = new JTextField("" + chart.get(AXIS_LABEL_FONT_SIZE, Integer.class, 1));
			axisFontSizeTxt.setInputVerifier(new IntInputVerifier());
			axisFontSizeTxt.setPreferredSize(new Dimension(60, axisFontSizeTxt.getMinimumSize().height));
			axisFontSizeTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			axisFontSizeTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					try {
						int v = Integer.parseInt(axisFontSizeTxt.getText());
			            chart.set(AXIS_LABEL_FONT_SIZE, v);
			        } catch (NumberFormatException nfe) {
			        }
				}
			});
		}
		
		return axisFontSizeTxt;
	}
	
	private ButtonGroup getOrientationGrp() {
		if (orientationGrp == null) {
			orientationGrp = new ButtonGroup();
			orientationGrp.add(getVerticalRd());
			orientationGrp.add(getHorizontalRd());
		}
		
		return orientationGrp;
	}
	
	protected JRadioButton getVerticalRd() {
		if (verticalRd == null) {
			verticalRd = new JRadioButton("Vertical Orientation");
			verticalRd.setVisible(setOrientation);
			
			if (setOrientation) {
				verticalRd.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setOrientation();
					}
				});
			}
		}
		
		return verticalRd;
	}
	
	protected JRadioButton getHorizontalRd() {
		if (horizontalRd == null) {
			horizontalRd = new JRadioButton("Horizontal Orientation");
			horizontalRd.setVisible(setOrientation);
			
			if (setOrientation) {
				horizontalRd.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setOrientation();
					}
				});
			}
		}
		
		return horizontalRd;
	}
	
	protected void setOrientation() {
		final Orientation orientation = getHorizontalRd().isSelected() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
		chart.set(ORIENTATION, orientation);
	}
	
	protected JTextField getBorderWidthTxt() {
		if (borderWidthTxt == null) {
			borderWidthTxt = new JTextField("" + chart.get(BORDER_WIDTH, Float.class, 0.25f));
			borderWidthTxt.setInputVerifier(new DoubleInputVerifier());
			borderWidthTxt.setPreferredSize(new Dimension(60, borderWidthTxt.getMinimumSize().height));
			borderWidthTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			borderWidthTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					try {
						float v = Float.parseFloat(borderWidthTxt.getText());
			            chart.set(BORDER_WIDTH, v);
			        } catch (NumberFormatException nfe) {
			        }
				}
			});
		}
		
		return borderWidthTxt;
	}
	
	protected ColorButton getBorderColorBtn() {
		if (borderColorBtn == null) {
			final Color color = chart.get(BORDER_COLOR, Color.class, Color.DARK_GRAY);
			borderColorBtn = new ColorButton(color);
			
			borderColorBtn.addPropertyChangeListener("color", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent e) {
					final Color newColor = (Color) e.getNewValue();
					chart.set(BORDER_COLOR, newColor);
				}
			});
		}
		
		return borderColorBtn;
	}
	
	@SuppressWarnings("unchecked")
	protected List<Double> calculateAutoRange() {
		final List<Double> range = new ArrayList<>(2);
		final CyNetwork net = appMgr.getCurrentNetwork();
		
		if (net != null) {
			final List<CyNode> nodes = net.getNodeList();
			final List<CyColumnIdentifier> dataColumns = getDataPnl().getDataColumns();
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			
			for (final CyColumnIdentifier colId : dataColumns) {
				final CyColumn column = columns.get(colId);
				
				if (column == null)
					continue;
				
				final Class<?> colType = column.getType();
				final Class<?> colListType = column.getListElementType();
				
				if (Number.class.isAssignableFrom(colType) ||
						(List.class.isAssignableFrom(colType) && Number.class.isAssignableFrom(colListType))) {
					for (final CyNode n : nodes) {
						List<? extends Number> values = null;
						final CyRow row = net.getRow(n);
						
						if (List.class.isAssignableFrom(colType))
							values = (List<? extends Number>) row.getList(column.getName(), colListType);
						else if (row.isSet(column.getName()))
							values = Collections.singletonList((Number)row.get(column.getName(), colType));
						
						double[] mm = minMax(min, max, values);
						min = mm[0];
						max = mm[1];
					}
				}
			}
			
			if (min != Double.POSITIVE_INFINITY && max != Double.NEGATIVE_INFINITY) {
				range.add(min);
				range.add(max);
			}
		}
		
		return range;
	}
	
	protected double[] minMax(double min, double max, final List<? extends Number> values) {
		if (values != null) {
			for (final Number v : values) {
				if (v != null) {
					final double dv = v.doubleValue();
					min = Math.min(min, dv);
					max = Math.max(max, dv);
				}
			}
		}
		
		return new double[]{ min, max };
	}
	
	protected void update(final boolean recalculateRange) {
		if (setOrientation)
			updateOrientation();
		
		updateGlobalRange();
		updateRangeMinMax(recalculateRange);
		updateItemLabel();
	}

	protected void updateOrientation() {
		final Orientation orientation = chart.get(ORIENTATION, Orientation.class, Orientation.VERTICAL);
		final JRadioButton orientRd = orientation == Orientation.HORIZONTAL ? getHorizontalRd() : getVerticalRd();
		getOrientationGrp().setSelected(orientRd.getModel(), true);
	}
	
	protected void updateGlobalRange() {
		final boolean global = chart.get(GLOBAL_RANGE, Boolean.class, Boolean.TRUE);
		getAutoRangeCkb().setVisible(global);
		rangeMinLbl.setVisible(global);
		rangeMaxLbl.setVisible(global);
		getRangeMinTxt().setVisible(global);
		getRangeMaxTxt().setVisible(global);
		getRefreshRangeBtn().setVisible(global);
	}
	
	protected void updateItemLabel() {
		final boolean showItemLabels = chart.get(SHOW_ITEM_LABELS, Boolean.class, Boolean.FALSE);
		itemLabelsColumnLbl.setEnabled(showItemLabels);
		getItemLabelsColumnCmb().setEnabled(showItemLabels);
		itemFontSizeLbl.setEnabled(showItemLabels);
		getItemFontSizeTxt().setEnabled(showItemLabels);
	}

	protected void updateRangeMinMax(final boolean recalculate) {
		final boolean global = chart.get(GLOBAL_RANGE, Boolean.class, Boolean.TRUE);
		
		if (global && setRange) {
			final boolean auto = chart.get(AUTO_RANGE, Boolean.class, Boolean.TRUE);
			List<Double> range = chart.getList(RANGE, Double.class);
			
			if (auto) {
				if (recalculate) {
					range = calculateAutoRange();
					getRefreshRangeBtn().setEnabled(false);
				} else {
					updateRefreshRangeBtn();
				}
			}
			
			if (range != null && range.size() >= 2) {
				chart.set(RANGE, range);
				getRangeMinTxt().setText(""+range.get(0));
				getRangeMaxTxt().setText(""+range.get(1));
			}
		}
	}
	
	private void updateRefreshRangeBtn() {
		if (setRange) {
			boolean b = chart.get(GLOBAL_RANGE, Boolean.class, Boolean.TRUE);
			b = b && chart.get(AUTO_RANGE, Boolean.class, Boolean.TRUE);
			
			if (b) {
				final List<Double> range = chart.getList(RANGE, Double.class);
				b = b && (range == null || !range.equals(calculateAutoRange()));
			}
			
			getRefreshRangeBtn().setEnabled(b);
		}
	}
	
	private void setGlobalRange() {
		final boolean global = chart.get(GLOBAL_RANGE, Boolean.class, Boolean.TRUE);
		
		if (global && setRange) {
			String minTxt = getRangeMinTxt().getText().trim();
			String maxTxt = getRangeMaxTxt().getText().trim();
			
			try {
	            double min = Double.parseDouble(minTxt);
	            double max = Double.parseDouble(maxTxt);
	            chart.set(RANGE, Arrays.asList(min, max));
	        } catch (NumberFormatException e) {
	        }
		}
	}
	
	protected ColorScheme[] getColorSchemes() {
		return BASIC_COLOR_SCHEMES;
	}
	
	protected boolean isDataColumn(final CyColumn c) {
		final Class<?> colType = c.getType();
		final Class<?> colListType = c.getListElementType();
		
		return dataType.isAssignableFrom(colType) ||
				(List.class.isAssignableFrom(colType) && dataType.isAssignableFrom(colListType));
	}
	
	protected static void selectColumnIdItem(final JComboBox<CyColumnIdentifier> cmb, final CyColumnIdentifier columnId) {
		if (columnId != null) {
			for (int i = 0; i < cmb.getItemCount(); i++) {
				final CyColumnIdentifier colId = cmb.getItemAt(i);
				
				if (colId != null && colId.equals(columnId)) {
					cmb.setSelectedItem(colId);
					break;
				}
			}
		}
	}
	
	protected JComboBox<Double> createAngleComboBox(final AbstractCustomGraphics2<?> cg2, final String propKey,
			Double[] values) {
		if (values == null)
			values = ANGLES;
		
		final JComboBox<Double> cmb = new JComboBox<>(values);
		cmb.setToolTipText("Starting from 3 o'clock and measuring clockwise (90\u00B0 = 6 o'clock)");
		cmb.setEditable(true);
		((JLabel)cmb.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		cmb.setSelectedItem(cg2.get(propKey, Double.class, 0.0));
		cmb.setInputVerifier(new DoubleInputVerifier());
		
		cmb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Object angle = cmb.getSelectedItem();
		        try {
		        	cg2.set(propKey, angle instanceof Number ? ((Number)angle).doubleValue() : 0.0);
		        } catch (NumberFormatException ex) {
		        }
			}
		});
		
		return cmb;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	protected class DataPanel extends JPanel {
		
		private static final long serialVersionUID = 3695410711435506554L;

		private final Set<CyColumnIdentifier> dataColumns;
		
		private JList<CyColumnIdentifier> allColumnsLs;
		private JList<CyColumnIdentifier> selColumnsLs;
		private final DefaultListModel<CyColumnIdentifier> allModel;
		private final DefaultListModel<CyColumnIdentifier> selModel;
		private JButton addBtn;
		private JButton addAllBtn;
		private JButton removeBtn;
		private JButton removeAllBtn;
		private JButton moveUpBtn;
		private JButton moveDownBtn;

		protected DataPanel() {
			dataColumns = new LinkedHashSet<CyColumnIdentifier>();
			allModel = new DefaultListModel<>();
			selModel = new DefaultListModel<>();
			
			// Filter all columns that are list of numbers
			for (final Map.Entry<CyColumnIdentifier, CyColumn> entry : columns.entrySet()) {
				final CyColumn c = entry.getValue();
				
				if (isDataColumn(c))
					dataColumns.add(entry.getKey());
			}
			
			final JLabel allColumnsLbl = new JLabel("Available Columns:");
			final JLabel selColumnsLbl = new JLabel("Selected Columns:");
			
			final JScrollPane listScr1 = new JScrollPane(getAllColumnsLs());
			listScr1.setPreferredSize(new Dimension(200, listScr1.getPreferredSize().height));
			final JScrollPane listScr2 = new JScrollPane(getSelColumnsLs());
			listScr2.setPreferredSize(new Dimension(200, listScr2.getPreferredSize().height));
			
			final JSeparator sep = new JSeparator();
			
			final GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(isWinLAF());
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
									.addComponent(allColumnsLbl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
									.addGroup(layout.createSequentialGroup()
											.addComponent(listScr1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
											.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
													.addComponent(getAddBtn(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
													.addComponent(getAddAllBtn(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
													.addComponent(getRemoveBtn(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
													.addComponent(getRemoveAllBtn(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
											)
									)
							)
							.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
									.addGroup(layout.createSequentialGroup()
											.addComponent(selColumnsLbl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
											.addComponent(getMoveUpBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(getMoveDownBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									)
									.addComponent(listScr2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							)
					)
					.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.BASELINE, false)
							.addComponent(allColumnsLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(selColumnsLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getMoveUpBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getMoveDownBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(Alignment.BASELINE, true)
							.addComponent(listScr1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addGroup(layout.createSequentialGroup()
									.addComponent(getAddBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									.addComponent(getAddAllBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									.addGap(0, 20, Short.MAX_VALUE)
									.addComponent(getRemoveBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									.addComponent(getRemoveAllBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							)
							.addComponent(listScr2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
					.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		protected void refresh() {
			final List<CyColumnIdentifier> chartDataColumns = chart.getList(DATA_COLUMNS, CyColumnIdentifier.class);
			
			for (final CyColumnIdentifier colId : chartDataColumns) {
				selModel.addElement(colId);
			}
			
			if (dataColumns != null) {
				for (final CyColumnIdentifier colId : dataColumns) {
					if (!chartDataColumns.contains(colId))
						allModel.addElement(colId);
				}
			}
			
			if (selModel.getSize() == 0 && allModel.getSize() > 0) {
				// Add at least one data column to begin with
				final CyColumnIdentifier colId = allModel.get(0);
				allModel.removeElement(colId);
				selModel.addElement(colId);
				
				chart.set(DATA_COLUMNS, getDataColumns());
				updateRangeMinMax(true);
			}
			
			updateButtons();
			getColorSchemeEditor().reset();
		}
		
		private JList<CyColumnIdentifier> getAllColumnsLs() {
			if (allColumnsLs == null) {
				allColumnsLs = new JList<>();
				allColumnsLs.setModel(
						new SortedListModel<CyColumnIdentifier>(allModel, SortOrder.ASCENDING, new ColumnComparator()));
				allColumnsLs.setCellRenderer(new CyColumnCellRenderer(true));
				
				allColumnsLs.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						updateButtons();
					}
				});
				
				allColumnsLs.getModel().addListDataListener(new ListDataListener() {
					@Override
					public void intervalRemoved(ListDataEvent e) {
						updateButtons();
					}
					@Override
					public void intervalAdded(ListDataEvent e) {
						updateButtons();
					}
					@Override
					public void contentsChanged(ListDataEvent e) {
					}
				});
			}
			
			return allColumnsLs;
		}
		
		private JList<CyColumnIdentifier> getSelColumnsLs() {
			if (selColumnsLs == null) {
				selColumnsLs = new JList<>();
				selColumnsLs.setModel(selModel);
				selColumnsLs.setCellRenderer(new CyColumnCellRenderer(true));
				
				selColumnsLs.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						updateButtons();
					}
				});
			}
			
			return selColumnsLs;
		}
		
		private JButton getAddBtn() {
			if (addBtn == null) {
				addBtn = new JButton(IconManager.ICON_ANGLE_RIGHT);
				addBtn.setFont(iconMgr.getIconFont(14.0f));
				addBtn.setToolTipText("Add Selected");
				
				addBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						moveDataColumns(getAllColumnsLs(), getSelColumnsLs(), false);
					}
				});
			}
			
			return addBtn;
		}
		
		private JButton getAddAllBtn() {
			if (addAllBtn == null) {
				addAllBtn = new JButton(IconManager.ICON_ANGLE_DOUBLE_RIGHT);
				addAllBtn.setFont(iconMgr.getIconFont(14.0f));
				addAllBtn.setToolTipText("Add All");
				
				addAllBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						moveDataColumns(getAllColumnsLs(), getSelColumnsLs(), true);
					}
				});
			}
			
			return addAllBtn;
		}
		
		private JButton getRemoveBtn() {
			if (removeBtn == null) {
				removeBtn = new JButton(IconManager.ICON_ANGLE_LEFT);
				removeBtn.setFont(iconMgr.getIconFont(14.0f));
				removeBtn.setToolTipText("Remove Selected");
				
				removeBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						moveDataColumns(getSelColumnsLs(), getAllColumnsLs(), false);
					}
				});
			}
			
			return removeBtn;
		}
		
		private JButton getRemoveAllBtn() {
			if (removeAllBtn == null) {
				removeAllBtn = new JButton(IconManager.ICON_ANGLE_DOUBLE_LEFT);
				removeAllBtn.setFont(iconMgr.getIconFont(14.0f));
				removeAllBtn.setToolTipText("Remove All");
				
				removeAllBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						moveDataColumns(getSelColumnsLs(), getAllColumnsLs(), true);
					}
				});
			}
			
			return removeAllBtn;
		}
		
		private JButton getMoveUpBtn() {
			if (moveUpBtn == null) {
				moveUpBtn = new JButton(IconManager.ICON_CARET_UP);
				moveUpBtn.setFont(iconMgr.getIconFont(17.0f));
				moveUpBtn.setToolTipText("Move Selected Up");
				moveUpBtn.setBorderPainted(false);
				moveUpBtn.setContentAreaFilled(false);
				moveUpBtn.setOpaque(false);
				moveUpBtn.setFocusPainted(false);
				moveUpBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
				
				moveUpBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						moveUp(getSelColumnsLs());
					}
				});
			}
			
			return moveUpBtn;
		}
		
		private JButton getMoveDownBtn() {
			if (moveDownBtn == null) {
				moveDownBtn = new JButton(IconManager.ICON_CARET_DOWN);
				moveDownBtn.setFont(iconMgr.getIconFont(17.0f));
				moveDownBtn.setToolTipText("Move Selected Down");
				moveDownBtn.setBorderPainted(false);
				moveDownBtn.setContentAreaFilled(false);
				moveDownBtn.setOpaque(false);
				moveDownBtn.setFocusPainted(false);
				moveDownBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
				
				moveDownBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						moveDown(getSelColumnsLs());
					}
				});
			}
			
			return moveDownBtn;
		}
		
		private void updateButtons() {
			getAddBtn().setEnabled(!getAllColumnsLs().getSelectionModel().isSelectionEmpty());
			getRemoveBtn().setEnabled(!getSelColumnsLs().getSelectionModel().isSelectionEmpty());
			
			getAddAllBtn().setEnabled(getAllColumnsLs().getModel().getSize() > 0);
			getRemoveAllBtn().setEnabled(getSelColumnsLs().getModel().getSize() > 0);
			
			final int[] selIndices = getSelColumnsLs().getSelectedIndices();
			int size = getSelColumnsLs().getModel().getSize();
			boolean b = selIndices != null && selIndices.length > 0;
			
			getMoveUpBtn().setEnabled(b && selIndices[0] > 0);
			getMoveDownBtn().setEnabled(b && selIndices[selIndices.length - 1] < size - 1);
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void moveDataColumns(final JList<CyColumnIdentifier> src,
									 final JList<CyColumnIdentifier> tgt,
									 final boolean all) {
			final Set<CyColumnIdentifier> set = new HashSet<>();
			
			final DefaultListModel<CyColumnIdentifier> srcModel;
			final DefaultListModel<CyColumnIdentifier> tgtModel;
			
			if (src.getModel() instanceof SortedListModel)
				srcModel = (DefaultListModel) ((SortedListModel)src.getModel()).getUnsortedModel();
			else
				srcModel = (DefaultListModel) src.getModel();
			
			if (tgt.getModel() instanceof SortedListModel)
				tgtModel = (DefaultListModel) ((SortedListModel)tgt.getModel()).getUnsortedModel();
			else
				tgtModel = (DefaultListModel) tgt.getModel();
				
			for (int i = 0; i < srcModel.getSize(); i++) {
				int index = i;
				
				if (src.getModel() instanceof SortedListModel)
					index = ((SortedListModel)src.getModel()).toUnsortedModelIndex(i);
				
				final CyColumnIdentifier colId = srcModel.get(index);
				
				if (all || src.isSelectedIndex(i)) {
					set.add(colId);
					
					if (!tgtModel.contains(colId))
						tgtModel.addElement(colId);
				}
			}
			
			for (final CyColumnIdentifier colId : set) {
				srcModel.removeElement(colId);
			}
			
			chart.set(DATA_COLUMNS, getDataColumns());
			
			if (!initializing) {
				updateRangeMinMax(true);
				getColorSchemeEditor().reset();
			}
		}
		
		private void moveUp(final JList<CyColumnIdentifier> list) {
			final DefaultListModel<CyColumnIdentifier> model = (DefaultListModel<CyColumnIdentifier>) list.getModel();

			final List<CyColumnIdentifier> all = new LinkedList<>();
			final int[] selIndices = new int[list.getSelectedIndices().length];
			int selCount = 0;
			boolean move = true;
			
			for (int i = 0; i < model.getSize(); i++) {
				final CyColumnIdentifier colId = model.get(i);
				
				if (list.isSelectedIndex(i)) {
					if (i == 0) {
						move = false;
						break;
					}
					
					all.add(i - 1, colId);
					selIndices[selCount++] = i - 1;
				} else {
					all.add(colId);
				}
			}
			
			if (move)
				replaceAll(list, all, selIndices);
		}

		private void moveDown(final JList<CyColumnIdentifier> list) {
			final DefaultListModel<CyColumnIdentifier> model = (DefaultListModel<CyColumnIdentifier>) list.getModel();
			final List<CyColumnIdentifier> all = new LinkedList<>();
			final int[] selIndices = new int[list.getSelectedIndices().length];
			int selCount = 0;
			boolean move = true;
			
			for (int i = model.getSize() - 1; i >= 0; i--) {
				final CyColumnIdentifier colId = model.get(i);
				
				if (list.isSelectedIndex(i)) {
					if (i == model.getSize() - 1) {
						move = false;
						break;
					}
					
					all.add(1, colId);
					selIndices[selCount++] = i + 1;
				} else {
					all.add(0, colId);
				}
			}
			
			if (move)
				replaceAll(list, all, selIndices);
		}
		
		private void replaceAll(final JList<CyColumnIdentifier> list, final List<CyColumnIdentifier> elements,
				final int[] selectedIndices) {
			final DefaultListModel<CyColumnIdentifier> model = (DefaultListModel<CyColumnIdentifier>) list.getModel();
			model.removeAllElements();
			int i = 0;
			
			for (final CyColumnIdentifier colId : elements) {
				model.add(i++, colId);
			}
			
			list.setSelectedIndices(selectedIndices);
			chart.set(DATA_COLUMNS, getDataColumns());
		}
		
		protected List<CyColumnIdentifier> getDataColumns() {
			final List<CyColumnIdentifier> columns = new ArrayList<>();
			final ListModel<CyColumnIdentifier> model = getSelColumnsLs().getModel();
			
			for (int i = 0; i < model.getSize(); i++) {
				final CyColumnIdentifier colId = model.getElementAt(i);
				columns.add(colId);
			}
			
			return columns;
		}
	}
	
	public class ColumnComparator implements Comparator<CyColumnIdentifier> {

		private final Collator collator = Collator.getInstance(Locale.getDefault());
		
		@Override
		public int compare(final CyColumnIdentifier c1, final CyColumnIdentifier c2) {
			return collator.compare(c1.getColumnName(), c2.getColumnName());
		}
	}
	
	public static class DoubleInputVerifier extends InputVerifier {

		@Override
		public boolean verify(final JComponent input) {
	        try {
	            Double.parseDouble(((JTextField) input).getText().trim());
	            return true; 
	        } catch (NumberFormatException e) {
	            return false;
	        }
		}
	}
	
	public static class IntInputVerifier extends InputVerifier {

		@Override
		public boolean verify(final JComponent input) {
	        try {
	            Integer.parseInt(((JTextField) input).getText().trim());
	            return true; 
	        } catch (NumberFormatException e) {
	            return false;
	        }
		}
	}
	
	protected static class CyColumnComboBox extends JComboBox<CyColumnIdentifier> {
		
		private static final long serialVersionUID = 8890884100875883324L;

		public CyColumnComboBox(final Collection<CyColumnIdentifier> columnIds, final boolean acceptsNull) {
			final List<CyColumnIdentifier> values = new ArrayList<>(columnIds);
			
			if (acceptsNull && !values.contains(null))
				values.add(0, null);
			
			final DefaultComboBoxModel<CyColumnIdentifier> model =
					new DefaultComboBoxModel<>(values.toArray(new CyColumnIdentifier[values.size()]));
			this.setModel(model);
			this.setRenderer(new CyColumnCellRenderer());
		}
	}
	
	protected static class CyColumnCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 840896421390898632L;
		
		private final boolean showCount;

		public CyColumnCellRenderer() {
			this(false);
		}
		
		public CyColumnCellRenderer(final boolean showCount) {
			this.showCount = showCount;
		}

		@Override
		public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
				final boolean isSelected, final boolean cellHasFocus) {
			final DefaultListCellRenderer c = (DefaultListCellRenderer) super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);
			if (value == null) {
				c.setText("-- none --");
			} else if (value instanceof CyColumnIdentifier) {
				if (showCount) {
					int totalLength = (int)(Math.log10(list.getModel().getSize()) + 1);
					int idxLength = (int)(Math.log10(index + 1) + 1);
					int dif = totalLength - idxLength;
					String count = "";
					
					while (dif-- > 0) count += "&nbsp;";
					count += (index + 1) + ". ";
					
					c.setText( "<html><font face='Monospaced'>" + count + "</font>" +
							   ((CyColumnIdentifier)value).getColumnName() + "</html>" );
				} else {
					c.setText(((CyColumnIdentifier)value).getColumnName());
				}
			} else {
				c.setText("[ invalid column ]"); // Should never happen
			}
				
			return c;
		}
	}
}
