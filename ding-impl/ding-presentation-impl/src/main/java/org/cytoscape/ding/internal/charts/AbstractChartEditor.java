package org.cytoscape.ding.internal.charts;

import static org.cytoscape.ding.internal.charts.AbstractEnhancedCustomGraphics.*;

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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public abstract class AbstractChartEditor<T extends AbstractEnhancedCustomGraphics<?>> extends JPanel {

	private static final long serialVersionUID = 2022740799541917592L;
	
	private static final String CUSTOM = "custom";

	protected static final String[] COLOR_SCHEMES = new String[] {
		ColorUtil.CONTRASTING, ColorUtil.MODULATED, ColorUtil.RAINBOW, ColorUtil.RANDOM, CUSTOM
	};
	
	private JTabbedPane optionsTpn;
	private JPanel basicOptionsPnl;
	private JPanel advancedOptionsPnl;
	private DataPanel dataPnl;
	private JPanel rangePnl;
	private JPanel otherBasicOptionsPnl;
	private JPanel otherAdvancedOptionsPnl;
	protected JLabel dataColumnLbl;
	protected JLabel labelColumnLbl;
	private JComboBox labelColumnCmb;
	private JLabel colorSchemeLbl;
	private JComboBox colorSchemeCmb;
	private JCheckBox globalRangeCkb;
	private JCheckBox autoRangeCkb;
	private JLabel rangeMinLbl;
	private JTextField rangeMinTxt;
	private JButton refreshRangeBtn;
	private JLabel rangeMaxLbl;
	private JTextField rangeMaxTxt;
	private JCheckBox labelsVisibleCkb;
	
	protected final int maxDataColumns; 
	protected final boolean setRange;
	protected final Map<String, CyColumn> columns;
	protected final T chart;
	protected final CyApplicationManager appMgr;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	protected AbstractChartEditor(final T chart,
								  final int maxDataColumns,
								  final boolean setRange,
								  final CyApplicationManager appMgr) {
		if (chart == null)
			throw new IllegalArgumentException("'chart' argument must not be null.");
		if (appMgr == null)
			throw new IllegalArgumentException("'appMgr' argument must not be null.");
		
		this.chart = chart;
		this.maxDataColumns = maxDataColumns;
		this.setRange = setRange;
		this.appMgr = appMgr;
		
		final Collator collator = Collator.getInstance(Locale.getDefault());
		columns = new TreeMap<String, CyColumn>(new Comparator<String>() {
			@Override
			public int compare(final String s1, final String s2) {
				return collator.compare(s1, s2);
			}
		});
		
		final CyNetwork net = appMgr.getCurrentNetwork();
		
		if (net != null) {
			final CyTable table = net.getDefaultNodeTable(); // TODO only node table for now, but may get edge table in the future
			final Collection<CyColumn> cols = table.getColumns();
			
			for (final CyColumn c : cols) {
				if (Collection.class.isAssignableFrom(c.getType()))
					columns.put(c.getName(), c);
			}
		}
		
		init();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	protected void init() {
		dataColumnLbl = new JLabel("Data Column" + (maxDataColumns > 1 ? "s" : ""));
		labelColumnLbl = new JLabel("Label Column");
		colorSchemeLbl = new JLabel("Color Scheme");
		rangeMinLbl = new JLabel("Min");
		rangeMaxLbl = new JLabel("Max");
		
		setOpaque(false);
		setLayout(new BorderLayout());
		add(getOptionsTpn(), BorderLayout.CENTER);
		
		update(false);
	}
	
	protected JTabbedPane getOptionsTpn() {
		if (optionsTpn == null) {
			optionsTpn = new JTabbedPane(JTabbedPane.LEFT);
			
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
			
			final JSeparator sep1 = new JSeparator();
			final JSeparator sep2 = new JSeparator();
			final JSeparator sep3 = new JSeparator();
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					// Data columns
					.addComponent(getDataPnl())
					.addComponent(sep1)
					// Data range
					.addComponent(getRangePnl())
					.addComponent(sep2)
					// Labels
					.addGroup(layout.createSequentialGroup()
							.addComponent(labelColumnLbl)
							.addComponent(getLabelColumnCmb(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
							          GroupLayout.PREFERRED_SIZE))
					.addComponent(sep3)
					.addComponent(getOtherBasicOptionsPnl())
					);
			layout.setVerticalGroup(layout.createSequentialGroup()
					// Data columns
					.addComponent(getDataPnl(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					          GroupLayout.PREFERRED_SIZE)
					.addComponent(sep1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					          GroupLayout.PREFERRED_SIZE)
					// Data range
					.addComponent(getRangePnl(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					          GroupLayout.PREFERRED_SIZE)
					.addComponent(sep2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					          GroupLayout.PREFERRED_SIZE)
					// Labels
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(labelColumnLbl)
							.addComponent(getLabelColumnCmb()))
					.addComponent(sep3)
					.addComponent(getOtherBasicOptionsPnl())
					);
			
			getDataPnl().setVisible(maxDataColumns > 0);
			sep1.setVisible(maxDataColumns > 0);
			getRangePnl().setVisible(setRange);
			sep2.setVisible(setRange);
			sep3.setVisible(getOtherBasicOptionsPnl().isVisible());
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
			
			final JSeparator sep1 = new JSeparator();
			final JSeparator sep2 = new JSeparator();
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(getLabelsVisibleCkb())
					.addComponent(sep1)
					.addGroup(layout.createSequentialGroup()
							.addComponent(colorSchemeLbl)
							.addComponent(getColorSchemeCmb(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
							          GroupLayout.PREFERRED_SIZE))
					.addComponent(sep2)
					.addComponent(getOtherAdvancedOptionsPnl())
					);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getLabelsVisibleCkb())
					.addComponent(sep1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					          GroupLayout.PREFERRED_SIZE)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(colorSchemeLbl)
							.addComponent(getColorSchemeCmb()))
					.addComponent(sep2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					          GroupLayout.PREFERRED_SIZE)
					.addComponent(getOtherAdvancedOptionsPnl())
					);
			
			sep2.setVisible(getOtherAdvancedOptionsPnl().isVisible());
		}
		
		return advancedOptionsPnl;
	}
	
	protected DataPanel getDataPnl() {
		if (dataPnl == null) {
			dataPnl = new DataPanel();
			dataPnl.setOpaque(false);
			dataPnl.refresh();
		}
		
		return dataPnl;
	}
	
	protected JPanel getRangePnl() {
		if (rangePnl == null) {
			rangePnl = new JPanel();
			rangePnl.setOpaque(false);
			
			final GroupLayout layout = new GroupLayout(rangePnl);
			rangePnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			
			if (setRange) {
				layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addComponent(getGlobalRangeCkb())
						.addGroup(layout.createSequentialGroup()
								.addComponent(getAutoRangeCkb())
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(rangeMinLbl)
								.addComponent(getRangeMinTxt())
								.addComponent(rangeMaxLbl)
								.addComponent(getRangeMaxTxt())
								.addComponent(getRefreshRangeBtn())));
				layout.setVerticalGroup(layout.createSequentialGroup()
						.addComponent(getGlobalRangeCkb())
						.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
								.addComponent(getAutoRangeCkb())
								.addComponent(rangeMinLbl)
								.addComponent(getRangeMinTxt())
								.addComponent(rangeMaxLbl)
								.addComponent(getRangeMaxTxt())
								.addComponent(getRefreshRangeBtn())));
			}
		}
		
		return rangePnl;
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
	
	protected JComboBox getLabelColumnCmb() {
		if (labelColumnCmb == null) {
			labelColumnCmb = new JComboBox(columns.values().toArray());
			selectCyColumnItem(labelColumnCmb, chart.get(LABELS_COLUMN, String.class));
			
			labelColumnCmb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final CyColumn column = (CyColumn) labelColumnCmb.getSelectedItem();
					chart.set(LABELS_COLUMN, column != null ? column.getName() : null);
				}
			});
		}

		return labelColumnCmb;
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
			
			rangeMinTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					setGlobalRange();
				}
			});
			// TODO set value from chart first. If null, calculate from data. If calculated range different from chart's range, enable "update" button
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
			
			rangeMaxTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					setGlobalRange();
				}
			});
		}
		
		return rangeMaxTxt;
	}
	
	public JButton getRefreshRangeBtn() {
		if (refreshRangeBtn == null) {
			refreshRangeBtn = new JButton();
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
	
	protected JComboBox getColorSchemeCmb() {
		if (colorSchemeCmb == null) {
			colorSchemeCmb = new JComboBox(COLOR_SCHEMES);
			
			final String scheme = chart.get(COLOR_SCHEME, String.class, "");
			
			if (Arrays.binarySearch(COLOR_SCHEMES, scheme) >= 0)
				colorSchemeCmb.setSelectedItem(scheme);
			else
				colorSchemeCmb.setSelectedItem(null);
			
			colorSchemeCmb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final String newScheme = (String) colorSchemeCmb.getSelectedItem();
					chart.set(COLOR_SCHEME, newScheme);
					updateColorList();
				}
			});
		}
		
		return colorSchemeCmb;
	}
	
	protected void updateColorList() {
		final String scheme = chart.get(COLOR_SCHEME, String.class, "");
		final List<Color> colors = chart.getList(COLORS, Color.class);
		
		if (!CUSTOM.equalsIgnoreCase(scheme)) {
			// Update list of colors (add new ones if there are more values now)
		}
		
		// TODO build list or table of editable colors
		
		// TODO: user change any color in the list => set scheme to "custom"
		// TODO always set COLORS to charts, even if not "custom" to guarantee the data-color match?
		
	}

	public JCheckBox getLabelsVisibleCkb() {
		if (labelsVisibleCkb == null) {
			labelsVisibleCkb = new JCheckBox("Show Item Labels");
			labelsVisibleCkb.setSelected(chart.get(SHOW_LABELS, Boolean.class, false));
			labelsVisibleCkb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chart.set(SHOW_LABELS, labelsVisibleCkb.isSelected());
				}
			});
		}
		
		return labelsVisibleCkb;
	}
	
	@SuppressWarnings("unchecked")
	protected DoubleRange calculateAutoRange() {
		DoubleRange range = null;
		final CyNetwork net = appMgr.getCurrentNetwork();
		
		if (net != null) {
			final boolean stacked = chart.get(STACKED, Boolean.class, false);
			final List<CyNode> nodes = net.getNodeList();
			final Set<CyColumn> dataColumns = getDataPnl().getDataColumns();
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			
			for (final CyColumn column : dataColumns) {
				if (column != null
						&& List.class.isAssignableFrom(column.getType())
						&& Number.class.isAssignableFrom(column.getListElementType())) {
					for (final CyNode n : nodes) {
						final List<? extends Number> list =
								(List<? extends Number>) net.getRow(n).getList(column.getName(), column.getListElementType());
						
						if (list != null) {
							double sum = 0;
							
							for (final Number value : list) {
								final double dv = value.doubleValue();
								
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
					}
					
					if (min != Double.POSITIVE_INFINITY && max != Double.NEGATIVE_INFINITY)
						range = new DoubleRange(min, max);
				}
			}
		}
		
		return range;
	}
	
	protected void update(final boolean recalculateRange) {
		updateGlobalRange();
		updateRangeMinMax(recalculateRange);
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
			
			if (auto && recalculate) {
				range = calculateAutoRange();
				getRefreshRangeBtn().setEnabled(false);
			}
			
			if (range != null) {
				chart.set(RANGE, range);
				getRangeMinTxt().setText(""+range.min);
				getRangeMaxTxt().setText(""+range.max);
			}
		}
		
		updateRefreshRangeBtn();
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
	
	protected static void selectCyColumnItem(final JComboBox cmb, final String columnName) {
		if (columnName != null) {
			for (int i = 0; i < cmb.getItemCount(); i++) {
				final CyColumn column = (CyColumn) cmb.getItemAt(i);
				
				if (column.getName().equals(columnName)) {
					cmb.setSelectedItem(column);
					break;
				}
			}
		}
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	protected class DataPanel extends JPanel {
		
		private static final long serialVersionUID = 3695410711435506554L;

		private final int maxColumns;
		private final Set<CyColumn> dataColumns;
		private final Set<DataColumnSelector> columnSelectors;
		private JButton addDataColumnBtn;

		protected DataPanel() {
			dataColumns = new LinkedHashSet<CyColumn>();
			columnSelectors = new LinkedHashSet<DataColumnSelector>();
			
			// Filter all columns that are list of numbers
			for (final CyColumn c : columns.values()) {
				if (List.class.isAssignableFrom(c.getType()) && Number.class.isAssignableFrom(c.getListElementType()))
					dataColumns.add(c);
			}
			
			maxColumns = Math.min(maxDataColumns, dataColumns.size());
			
			final BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
			setLayout(layout);
			
			add(dataColumnLbl);
			add(getAddDataColumnBtn());
		}
		
		protected void refresh() {
			final LinkedHashSet<DataColumnSelector> selectors = new LinkedHashSet<DataColumnSelector>(columnSelectors);
			
			for (final DataColumnSelector sel : selectors)
				removeDataColumnSelector(sel);
			
			if (maxColumns > 0) {
				final List<String> dataColumns = chart.getList(DATA_COLUMNS, String.class);
				int count = 0;
				
				if (dataColumns != null) {
					for (final String name : dataColumns) {
						if (count++ < maxColumns)
							addDataColumnSelector(name);
					}
				}
				
				if (count == 0) // Add at least one selector to begin with
					addDataColumnSelector(null);
			}
		}
		
		protected JButton getAddDataColumnBtn() {
			if (addDataColumnBtn == null) {
				addDataColumnBtn = new JButton("+");
				addDataColumnBtn.setToolTipText("Add another data column");
				addDataColumnBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
				addDataColumnBtn.setVisible(maxColumns > 1);
				updateAddDataColumnBtn();
				
				addDataColumnBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						addDataColumnSelector(null);
					}
				});
			}
			
			return addDataColumnBtn;
		}
		
		protected void addDataColumnSelector(final String columnName) {
			final DataColumnSelector selector = new DataColumnSelector(columnName);
			columnSelectors.add(selector);
			
			final int index = getComponentCount() > 0 ? getComponentCount() - 1 : 0;
			add(selector, index);
			
			chart.set(DATA_COLUMNS, getDataColumnNames());
			updateAddDataColumnBtn();
			updateRangeMinMax(true);
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
		}
		
		protected Set<CyColumn> getDataColumns() {
			final Set<CyColumn> dataColumns = new LinkedHashSet<CyColumn>();
			
			for (final DataColumnSelector selector : columnSelectors) {
				final CyColumn selectedColumn = (CyColumn) selector.cmb.getSelectedItem();
				
				if (selectedColumn != null)
					dataColumns.add(selectedColumn);
			}
			
			return dataColumns;
		}
		
		protected List<String> getDataColumnNames() {
			final List<String> names = new ArrayList<String>();
			
			for (final DataColumnSelector selector : columnSelectors) {
				final CyColumn selectedColumn = (CyColumn) selector.cmb.getSelectedItem();
				
				if (selectedColumn != null)
					names.add(selectedColumn.getName());
			}
			
			return names;
		}
		
		private String getNextDefaultColumnName() {
			final Set<CyColumn> set = new LinkedHashSet<CyColumn>(dataColumns);
			
			for (final DataColumnSelector selector : columnSelectors) {
				final CyColumn selectedColumn = (CyColumn) selector.cmb.getSelectedItem();
				
				if (selectedColumn != null)
					set.remove(selectedColumn);
			}
			
			return set.isEmpty() ? null : set.iterator().next().getName();
		}
		
		private void updateAddDataColumnBtn() {
			getAddDataColumnBtn().setEnabled(columnSelectors.size() < maxColumns);
		}
		
		private class DataColumnSelector extends JPanel {

			private static final long serialVersionUID = 753659806235431081L;
			
			final JComboBox cmb;
			final JButton delBtn;
			
			DataColumnSelector(String columnName) {
				setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
				setOpaque(false);
				setAlignmentX(Component.LEFT_ALIGNMENT);
				
				cmb = new JComboBox(dataColumns.toArray());
				cmb.setSelectedItem(null);
				cmb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						chart.set(DATA_COLUMNS, getDataColumnNames());
						updateRangeMinMax(true);
					}
				});
				
				delBtn = new JButton("-");
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
				
				if (columnName == null)
					columnName = getNextDefaultColumnName();
				
				selectCyColumnItem(cmb, columnName);
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
}
