package org.cytoscape.ding.internal.charts;

import static org.cytoscape.ding.customgraphics.AbstractCustomGraphics2.ORIENTATION;
import static org.cytoscape.ding.customgraphics.ColorScheme.CONTRASTING;
import static org.cytoscape.ding.customgraphics.ColorScheme.CUSTOM;
import static org.cytoscape.ding.customgraphics.ColorScheme.MODULATED;
import static org.cytoscape.ding.customgraphics.ColorScheme.RAINBOW;
import static org.cytoscape.ding.customgraphics.ColorScheme.RANDOM;
import static org.cytoscape.ding.internal.charts.AbstractChart.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2;
import org.cytoscape.ding.customgraphics.ColorScheme;
import org.cytoscape.ding.customgraphics.Orientation;
import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.cytoscape.ding.internal.charts.heatmap.HeatMapChart;
import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public abstract class AbstractChartEditor<T extends AbstractCustomGraphics2<?>> extends JPanel {

	private static final long serialVersionUID = 2022740799541917592L;
	
	private static final ColorScheme[] BASIC_COLOR_SCHEMES = new ColorScheme[] {
		CONTRASTING, MODULATED, RAINBOW, RANDOM, CUSTOM
	};
	
	protected static Double[] ANGLES = new Double[] { 0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0 };
	
	protected static final JColorChooser colorChooser = new JColorChooser();
	
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
	protected JLabel dataColumnLbl;
	protected JLabel itemLabelsColumnLbl;
	protected JLabel domainLabelsColumnLbl;
	protected JLabel rangeLabelsColumnLbl;
	private JComboBox<CyColumnIdentifier> itemLabelsColumnCmb;
	private JComboBox<CyColumnIdentifier> domainLabelsColumnCmb;
	private JComboBox<CyColumnIdentifier> rangeLabelsColumnCmb;
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
	private JLabel axisWidthLbl;
	private JTextField axisWidthTxt;
	private JLabel axisColorLbl;
	private ColorButton axisColorBtn;
	private ButtonGroup orientationGrp;
	private JRadioButton verticalRd;
	private JRadioButton horizontalRd;
	private JLabel borderWidthLbl;
	private JTextField borderWidthTxt;
	private JLabel borderColorLbl;
	private ColorButton borderColorBtn;
	
	protected final boolean columnIsSeries;
	protected final int maxDataColumns; 
	protected final boolean setRange;
	protected final boolean setOrientation;
	protected final boolean setItemLabels;
	protected final boolean setDomainLabels;
	protected final boolean setRangeLabels;
	protected final boolean hasAxes;
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
								  final int maxDataColumns,
								  final boolean setRange,
								  final boolean setOrientation,
								  final boolean setItemLabels,
								  final boolean setDomainLabels,
								  final boolean setRangeLabels,
								  final boolean hasAxes,
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
		this.maxDataColumns = maxDataColumns;
		this.setRange = setRange;
		this.setOrientation = setOrientation;
		this.setItemLabels = setItemLabels;
		this.setDomainLabels = setDomainLabels;
		this.setRangeLabels = setRangeLabels;
		this.hasAxes = hasAxes;
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
		this.colIdFactory = colIdFactory;
		
		final Collator collator = Collator.getInstance(Locale.getDefault());
		final Comparator<CyColumnIdentifier> columnComparator = new Comparator<CyColumnIdentifier>() {
			@Override
			public int compare(final CyColumnIdentifier c1, final CyColumnIdentifier c2) {
				return collator.compare(c1.getColumnName(), c2.getColumnName());
			}
		};
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
			setOpaque(false);
			setLayout(new BorderLayout());
			add(getOptionsTpn(), BorderLayout.CENTER);
		} finally {
			initializing = false;
		}
		
		update(false);
	}
	
	protected void createLabels() {
		dataColumnLbl = new JLabel("Data Column" + (maxDataColumns > 1 ? "s" : ""));
		itemLabelsColumnLbl = new JLabel("Column");
		domainLabelsColumnLbl = new JLabel("Domain Labels Column");
		rangeLabelsColumnLbl = new JLabel("Range Labels Column");
		rangeMinLbl = new JLabel("Min");
		rangeMaxLbl = new JLabel("Max");
		axisWidthLbl = new JLabel("Axis Width");
		axisColorLbl = new JLabel("Axis Color");
		borderWidthLbl = new JLabel("Border Width");
		borderColorLbl = new JLabel("Border Color");
	}

	protected JTabbedPane getOptionsTpn() {
		if (optionsTpn == null) {
			optionsTpn = new JTabbedPane(JTabbedPane.LEFT);
			optionsTpn.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			
			JScrollPane scr1 = new JScrollPane(getBasicOptionsPnl(), 
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scr1.setBorder(BorderFactory.createEmptyBorder());
			scr1.setOpaque(false);
			scr1.getViewport().setOpaque(false);
			
			JScrollPane scr2 = new JScrollPane(getAdvancedOptionsPnl(), 
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scr2.setBorder(BorderFactory.createEmptyBorder());
			scr2.setOpaque(false);
			scr2.getViewport().setOpaque(false);
			
			optionsTpn.addTab("Data", scr1);
			optionsTpn.addTab("Options", scr2);
		}
		
		return optionsTpn;
	}
	
	protected JPanel getBasicOptionsPnl() {
		if (basicOptionsPnl == null) {
			basicOptionsPnl = new JPanel();
			basicOptionsPnl.setOpaque(false);
			
			final GroupLayout layout = new GroupLayout(basicOptionsPnl);
			basicOptionsPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(getOtherBasicOptionsPnl())
					.addComponent(getDataPnl())
					.addComponent(getRangePnl())
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getOtherBasicOptionsPnl(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					          GroupLayout.PREFERRED_SIZE)
					.addComponent(getDataPnl(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					          GroupLayout.PREFERRED_SIZE)
					.addComponent(getRangePnl(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					          GroupLayout.PREFERRED_SIZE)
			);
		}
		
		return basicOptionsPnl;
	}
	
	protected JPanel getAdvancedOptionsPnl() {
		if (advancedOptionsPnl == null) {
			advancedOptionsPnl = new JPanel();
			advancedOptionsPnl.setOpaque(false);
			
			final GroupLayout layout = new GroupLayout(advancedOptionsPnl);
			advancedOptionsPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			
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
			dataPnl.setOpaque(false);
			dataPnl.setVisible(maxDataColumns > 0);
			dataPnl.refresh();
		}
		
		return dataPnl;
	}
	
	protected JPanel getRangePnl() {
		if (rangePnl == null) {
			rangePnl = new JPanel();
			rangePnl.setOpaque(false);
			rangePnl.setVisible(setRange);
			
			if (!rangePnl.isVisible())
				return rangePnl;
			
			final GroupLayout layout = new GroupLayout(rangePnl);
			rangePnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			
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
							.addComponent(getAutoRangeCkb())
							.addComponent(rangeMinLbl)
							.addComponent(getRangeMinTxt())
							.addComponent(rangeMaxLbl)
							.addComponent(getRangeMaxTxt())
							.addComponent(getRefreshRangeBtn())
					).addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
				          GroupLayout.PREFERRED_SIZE)
			);
		}
		
		return rangePnl;
	}
	
	protected JPanel getLabelsPnl() {
		if (labelsPnl == null) {
			labelsPnl = new JPanel();
			labelsPnl.setOpaque(false);
			labelsPnl.setVisible(setItemLabels || setDomainLabels || setRangeLabels);
			
			if (!labelsPnl.isVisible())
				return labelsPnl;
			
			final GroupLayout layout = new GroupLayout(labelsPnl);
			labelsPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			
			final ParallelGroup hGroup = layout.createParallelGroup(Alignment.LEADING, true);
			final SequentialGroup vGroup = layout.createSequentialGroup();
			layout.setHorizontalGroup(hGroup);
			layout.setVerticalGroup(vGroup);
			
			if (setItemLabels) {
				hGroup.addGroup(layout.createSequentialGroup()
						.addComponent(getItemLabelsVisibleCkb())
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(itemLabelsColumnLbl)
						.addComponent(getItemLabelsColumnCmb(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE));
				vGroup.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getItemLabelsVisibleCkb())
						.addComponent(itemLabelsColumnLbl)
						.addComponent(getItemLabelsColumnCmb()));
			}
			
			if (setDomainLabels) {
				hGroup.addGroup(layout.createSequentialGroup()
						.addComponent(domainLabelsColumnLbl)
						.addComponent(getDomainLabelsColumnCmb(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						          GroupLayout.PREFERRED_SIZE));
				vGroup.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(domainLabelsColumnLbl)
						.addComponent(getDomainLabelsColumnCmb()));
			}
			
			if (setRangeLabels) {
				hGroup.addGroup(layout.createSequentialGroup()
						.addComponent(rangeLabelsColumnLbl)
						.addComponent(getRangeLabelsColumnCmb(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						          GroupLayout.PREFERRED_SIZE));
				vGroup.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(rangeLabelsColumnLbl)
						.addComponent(getRangeLabelsColumnCmb()));
					
			}
			
			final JSeparator sep = new JSeparator();
			
			hGroup.addComponent(sep);
			vGroup.addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
		}
		
		return labelsPnl;
	}
	
	protected JPanel getOrientationPnl() {
		if (orientationPnl == null) {
			orientationPnl = new JPanel();
			orientationPnl.setOpaque(false);
			orientationPnl.setVisible(setOrientation);
			
			if (!orientationPnl.isVisible())
				return orientationPnl;
			
			final GroupLayout layout = new GroupLayout(orientationPnl);
			orientationPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			
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
					).addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					          GroupLayout.PREFERRED_SIZE)
			);
		}
		
		return orientationPnl;
	}
	
	protected JPanel getAxesPnl() {
		if (axesPnl == null) {
			axesPnl = new JPanel();
			axesPnl.setOpaque(false);
			axesPnl.setVisible(hasAxes);
			
			if (!axesPnl.isVisible())
				return axesPnl;
			
			final GroupLayout layout = new GroupLayout(axesPnl);
			axesPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			
			final JSeparator vsep = new JSeparator(JSeparator.VERTICAL);
			final JSeparator sep = new JSeparator();
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(getDomainAxisVisibleCkb())
							.addComponent(getRangeAxisVisibleCkb())
						)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(vsep, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						          GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addGroup(layout.createSequentialGroup()
								.addComponent(axisWidthLbl)
								.addComponent(getAxisWidthTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
							).addGroup(layout.createSequentialGroup()
								.addComponent(axisColorLbl)
								.addComponent(getAxisColorBtn(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
							)
						)
					).addComponent(sep)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addGroup(layout.createSequentialGroup()
							.addComponent(getDomainAxisVisibleCkb())
							.addComponent(getRangeAxisVisibleCkb())
						)
						.addComponent(vsep)
						.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
								.addComponent(axisWidthLbl)
								.addComponent(getAxisWidthTxt())
							).addGroup(layout.createParallelGroup(Alignment.CENTER, true)
								.addComponent(axisColorLbl)
								.addComponent(getAxisColorBtn())
							)
						)
					).addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					          GroupLayout.PREFERRED_SIZE)
			);
		}
		
		return axesPnl;
	}
	
	protected JPanel getBorderPnl() {
		if (borderPnl == null) {
			borderPnl = new JPanel();
			borderPnl.setOpaque(false);
			
			final GroupLayout layout = new GroupLayout(borderPnl);
			borderPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			
			final JSeparator sep = new JSeparator();
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(layout.createSequentialGroup()
						.addComponent(borderWidthLbl)
						.addComponent(getBorderWidthTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(layout.createSequentialGroup()
						.addComponent(borderColorLbl)
						.addComponent(getBorderColorBtn(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
					).addComponent(sep)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(borderWidthLbl)
						.addComponent(getBorderWidthTxt())
					).addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(borderColorLbl)
						.addComponent(getBorderColorBtn())
					).addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					          GroupLayout.PREFERRED_SIZE)
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
			otherBasicOptionsPnl.setOpaque(false);
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
			otherAdvancedOptionsPnl.setOpaque(false);
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
						updateRangeMinMax(chart.get(RANGE, DoubleRange.class) == null);
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
			itemLabelsVisibleCkb = new JCheckBox("Show Item Labels");
			itemLabelsVisibleCkb.setVisible(setItemLabels);
			
			if (setItemLabels) {
				itemLabelsVisibleCkb.setSelected(chart.get(SHOW_ITEM_LABELS, Boolean.class, false));
				itemLabelsVisibleCkb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						chart.set(SHOW_ITEM_LABELS, itemLabelsVisibleCkb.isSelected());
						
						itemLabelsColumnLbl.setEnabled(itemLabelsVisibleCkb.isSelected());
						getItemLabelsColumnCmb().setEnabled(itemLabelsVisibleCkb.isSelected());
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
				domainAxisVisibleCkb.setSelected(chart.get(HeatMapChart.SHOW_DOMAIN_AXIS, Boolean.class, false));
				domainAxisVisibleCkb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						chart.set(HeatMapChart.SHOW_DOMAIN_AXIS, domainAxisVisibleCkb.isSelected());
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
				rangeAxisVisibleCkb.setSelected(chart.get(HeatMapChart.SHOW_RANGE_AXIS, Boolean.class, false));
				rangeAxisVisibleCkb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						chart.set(HeatMapChart.SHOW_RANGE_AXIS, rangeAxisVisibleCkb.isSelected());
					}
				});
			}
		}
		
		return rangeAxisVisibleCkb;
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
	protected DoubleRange calculateAutoRange() {
		DoubleRange range = null;
		final CyNetwork net = appMgr.getCurrentNetwork();
		
		if (net != null) {
			final List<CyNode> nodes = net.getNodeList();
			final Set<CyColumnIdentifier> dataColumns = getDataPnl().getDataColumns();
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
					
					if (min != Double.POSITIVE_INFINITY && max != Double.NEGATIVE_INFINITY)
						range = new DoubleRange(min, max);
				}
			}
		}
		
		return range;
	}
	
	protected double[] minMax(double min, double max, final List<? extends Number> values) {
		if (values != null) {
			for (final Number v : values) {
				final double dv = v.doubleValue();
				min = Math.min(min, dv);
				max = Math.max(max, dv);
			}
		}
		
		return new double[]{ min, max };
	}
	
	protected void update(final boolean recalculateRange) {
		if (setOrientation)
			updateOrientation();
		updateGlobalRange();
		updateRangeMinMax(recalculateRange);
		
		final boolean showItemLabels = chart.get(SHOW_ITEM_LABELS, Boolean.class, Boolean.FALSE);
		itemLabelsColumnLbl.setEnabled(showItemLabels);
		getItemLabelsColumnCmb().setEnabled(showItemLabels);
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

	protected void updateRangeMinMax(final boolean recalculate) {
		final boolean global = chart.get(GLOBAL_RANGE, Boolean.class, Boolean.TRUE);
		
		if (global && setRange) {
			final boolean auto = chart.get(AUTO_RANGE, Boolean.class, Boolean.TRUE);
			DoubleRange range = chart.get(RANGE, DoubleRange.class);
			
			if (auto) {
				if (recalculate) {
					range = calculateAutoRange();
					getRefreshRangeBtn().setEnabled(false);
				} else {
					updateRefreshRangeBtn();
				}
			}
			
			if (range != null) {
				chart.set(RANGE, range);
				getRangeMinTxt().setText(""+range.min);
				getRangeMaxTxt().setText(""+range.max);
			}
		}
	}
	
	private void updateRefreshRangeBtn() {
		if (setRange) {
			boolean b = chart.get(GLOBAL_RANGE, Boolean.class, Boolean.TRUE);
			b = b && chart.get(AUTO_RANGE, Boolean.class, Boolean.TRUE);
			
			if (b) {
				final DoubleRange range = chart.get(RANGE, DoubleRange.class);
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
	            chart.set(RANGE, new DoubleRange(min, max));
	        } catch (NumberFormatException e) {
	        }
		}
	}
	
	protected JComboBox<CyColumnIdentifier> createDataColumnComboBox(final Collection<CyColumnIdentifier> columns,
			final boolean acceptsNull) {
		final JComboBox<CyColumnIdentifier> cmb = new CyColumnComboBox(columns, false);
		cmb.setSelectedItem(null);
		cmb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chart.set(DATA_COLUMNS, getDataPnl().getDataColumnNames());
				
				if (!initializing) {
					updateRangeMinMax(true);
					getColorSchemeEditor().reset();
				}
			}
		});
		
		return cmb;
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

		private int maxColumns;
		private final Set<CyColumnIdentifier> dataColumns;
		private final Set<DataColumnSelector> columnSelectors;
		private JButton addDataColumnBtn;

		protected DataPanel() {
			dataColumns = new LinkedHashSet<CyColumnIdentifier>();
			columnSelectors = new LinkedHashSet<DataColumnSelector>();
			
			// Filter all columns that are list of numbers
			for (final CyColumnIdentifier colId : columns.keySet()) {
				final CyColumn c = columns.get(colId);
				
				if (isDataColumn(c))
					dataColumns.add(colId);
			}
			
			maxColumns = Math.min(maxDataColumns, dataColumns.size());
			
			final BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
			setLayout(layout);
			
			add(dataColumnLbl);
			add(getAddDataColumnBtn());
			add(new JSeparator());
		}
		
		protected void refresh() {
			final LinkedHashSet<DataColumnSelector> selectors = new LinkedHashSet<DataColumnSelector>(columnSelectors);
			
			for (final DataColumnSelector sel : selectors)
				removeDataColumnSelector(sel);
			
			if (maxColumns > 0) {
				final List<CyColumnIdentifier> dataColumns = chart.getList(DATA_COLUMNS, CyColumnIdentifier.class);
				int count = 0;
				
				if (dataColumns != null) {
					for (final CyColumnIdentifier colId : dataColumns) {
						if (count++ < maxColumns)
							addDataColumnSelector(colId, false);
					}
				}
				
				if (count == 0) // Add at least one selector to begin with
					addDataColumnSelector(null, false);
				
				getColorSchemeEditor().reset();
			}
		}
		
		protected JButton getAddDataColumnBtn() {
			if (addDataColumnBtn == null) {
				addDataColumnBtn = new JButton(IconManager.ICON_PLUS);
				addDataColumnBtn.setFont(iconMgr.getIconFont(12.0f));
				addDataColumnBtn.setToolTipText("Add another data column");
				addDataColumnBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
				addDataColumnBtn.setVisible(maxColumns > 1);
				updateAddDataColumnBtn();
				
				addDataColumnBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						addDataColumnSelector(null, true);
					}
				});
			}
			
			return addDataColumnBtn;
		}
		
		protected void addDataColumnSelector(final CyColumnIdentifier columnId, final boolean resetColorScheme) {
			final DataColumnSelector selector = new DataColumnSelector(columnId);
			columnSelectors.add(selector);
			
			final int index = getComponentCount() > 0 ? getComponentCount() - 2 : 0;
			add(selector, index);
			
			chart.set(DATA_COLUMNS, getDataColumnNames());
			updateAddDataColumnBtn();
			
			if (!initializing)
				updateRangeMinMax(true);
			
			if (!initializing && resetColorScheme)
				getColorSchemeEditor().reset();
			
			invalidate();
		}
		
		protected void removeDataColumnSelector(final DataColumnSelector sel) {
			if (columnSelectors.size() > 1) {
				remove(sel);
				columnSelectors.remove(sel);
				chart.set(DATA_COLUMNS, getDataColumnNames());
				updateAddDataColumnBtn();
				invalidate();
			} else {
				sel.cmb.setSelectedItem(null);
			}
			
			updateRangeMinMax(true);
			getColorSchemeEditor().reset();
		}
		
		protected Set<CyColumnIdentifier> getDataColumns() {
			final Set<CyColumnIdentifier> dataColumns = new LinkedHashSet<CyColumnIdentifier>();
			
			for (final DataColumnSelector selector : columnSelectors) {
				final CyColumnIdentifier selectedColumn = (CyColumnIdentifier) selector.cmb.getSelectedItem();
				
				if (selectedColumn != null)
					dataColumns.add(selectedColumn);
			}
			
			return dataColumns;
		}
		
		public List<CyColumnIdentifier> getDataColumnNames() {
			final List<CyColumnIdentifier> names = new ArrayList<CyColumnIdentifier>();
			
			for (final DataColumnSelector selector : columnSelectors) {
				final CyColumnIdentifier selectedColumn = (CyColumnIdentifier) selector.cmb.getSelectedItem();
				
				if (selectedColumn != null)
					names.add(selectedColumn);
			}
			
			return names;
		}
		
		private CyColumnIdentifier getNextDefaultColumnId() {
			final Set<CyColumnIdentifier> set = new LinkedHashSet<CyColumnIdentifier>(dataColumns);
			
			for (final DataColumnSelector selector : columnSelectors) {
				final CyColumnIdentifier selectedColumn = (CyColumnIdentifier) selector.cmb.getSelectedItem();
				
				if (selectedColumn != null)
					set.remove(selectedColumn);
			}
			
			return set.isEmpty() ? null : set.iterator().next();
		}
		
		private void updateAddDataColumnBtn() {
			getAddDataColumnBtn().setEnabled(columnSelectors.size() < maxColumns);
		}
		
		private class DataColumnSelector extends JPanel {

			private static final long serialVersionUID = 753659806235431081L;
			
			final JComboBox<CyColumnIdentifier> cmb;
			final JButton delBtn;
			
			DataColumnSelector(CyColumnIdentifier columnId) {
				setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
				setOpaque(false);
				setAlignmentX(Component.LEFT_ALIGNMENT);
				
				cmb = createDataColumnComboBox(dataColumns, false);
				
				delBtn = new JButton(IconManager.ICON_REMOVE);
				delBtn.setFont(iconMgr.getIconFont(12.0f));
				delBtn.setToolTipText("Remove this column's data from chart");
				delBtn.setVisible(maxColumns > 1);
				delBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						removeDataColumnSelector(DataColumnSelector.this);
					}
				});
				
				add(cmb);
				add(delBtn);
				
				if (columnId == null)
					columnId = getNextDefaultColumnId();
				
				selectColumnIdItem(cmb, columnId);
			}
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
			
			DefaultComboBoxModel<CyColumnIdentifier> model = new DefaultComboBoxModel(values.toArray());
			this.setModel(model);
			this.setRenderer(new CyColumnComboBoxRenderer());
		}
	}
	
	protected static class CyColumnComboBoxRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 840896421390898632L;

		@Override
		public Component getListCellRendererComponent(final JList list, final Object value, final int index,
				final boolean isSelected, final boolean cellHasFocus) {
			final DefaultListCellRenderer c = (DefaultListCellRenderer) super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);
			
			if (value == null)
				c.setText("-- none --");
			else if (value instanceof CyColumnIdentifier)
				c.setText(((CyColumnIdentifier)value).getColumnName());
			else
				c.setText("[ invalid column ]"); // Should never happen
				
			return c;
		}
	}
	
	protected static class ColorButton extends JButton {

		private static final long serialVersionUID = 893040611762412640L;
		
		private Color color;
		private Color borderColor;

		public ColorButton(final Color color) {
			super(" ");
			setHorizontalTextPosition(JButton.CENTER);
			setVerticalTextPosition(JButton.CENTER);
			borderColor = ColorUtil.getContrastingColor(getBackground());
			setIcon(new ColorIcon());
			setColor(color);
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Open color chooser
					final JDialog dialog = JColorChooser.createDialog(
							ColorButton.this,
							"Please pick a color",
							true,
							colorChooser, 
							new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									final Color c = colorChooser.getColor();
									ColorButton.this.setColor(c);
								}
							}, null);
					dialog.setVisible(true);
				}
			});
		}

		public void setColor(final Color color) {
			final Color oldColor = this.color;
			this.color = color;
			repaint();
			firePropertyChange("color", oldColor, color);
		}
		
		public Color getColor() {
			return color;
		}
		
		class ColorIcon implements Icon {

			@Override
			public int getIconHeight() {
				return 16;
			}

			@Override
			public int getIconWidth() {
				return 44;
			}

			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				int w = getIconWidth();
				int h = getIconHeight();
				
				g.setColor(color);
				g.fillRect(x, y, w, h);
				g.setColor(borderColor);
				g.drawRect(x, y, w, h);
			}
		}
	}
}
