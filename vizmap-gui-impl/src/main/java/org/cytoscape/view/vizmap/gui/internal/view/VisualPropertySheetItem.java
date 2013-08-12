package org.cytoscape.view.vizmap.gui.internal.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.TableCellEditor;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.model.LockedValueState;
import org.cytoscape.view.vizmap.gui.internal.theme.IconManager;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.jdesktop.swingx.icon.EmptyIcon;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

@SuppressWarnings("serial")
public class VisualPropertySheetItem<T> extends JPanel {

	public enum MessageType { INFO, WARNING, ERROR	}

	private static final int HEIGHT = 32;
	private static final int PROP_SHEET_ROW_HEIGHT = 24;
	private static final int MAPPING_IMG_ROW_HEIGHT = 90;
	
	private static final int VALUE_ICON_WIDTH = 24;
	private static final int VALUE_ICON_HEIGHT = 24;
	private static final int BUTTON_V_PAD = 2;
	private static final int BUTTON_H_PAD = 4;
	
	private static final int MSG_ICON_WIDTH = 18;
	private static final int MSG_ICON_HEIGHT = 15;
	
	static final Color FG_COLOR = new Color(115, 115, 115);
	static final Color BG_COLOR = Color.WHITE;
	static final Color SELECTED_BG_COLOR = new Color(222, 234, 252);
	
	static final Color BTN_BORDER_COLOR = new Color(200, 200, 200);
	static final Color BTN_BORDER_DISABLED_COLOR = new Color(248, 248, 248);
	static final int BTN_H_MARGIN = 1;
	static final int BTN_BORDER_WIDTH = 1;
	
	private JPanel topPnl;
	private JPanel mappingPnl;
	private PropertySheetPanel propSheetPnl;
	private ExpandCollapseButton expandCollapseBtn;
	private JButton defaultBtn;
	private JToggleButton mappingBtn;
	private JButton bypassBtn;
	private JCheckBox dependencyCkb;
	private JLabel titleLbl;
	private JLabel msgIconLbl;
	private PropertySheetTable propSheetTbl;
	private JButton removeMappingBtn;
	
	private final Icon disabledBtnIcon;
	
	private boolean selected;
	
	private final VisualPropertySheetItemModel<T> model;
	private final VizMapPropertyBuilder vizMapPropertyBuilder;
	private final IconManager iconMgr;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VisualPropertySheetItem(final VisualPropertySheetItemModel<T> model,
								   final VizMapPropertyBuilder vizMapPropertyBuilder,
								   final IconManager iconMgr) {
		if (model == null)
			throw new IllegalArgumentException("'model' must not be null");
		if (vizMapPropertyBuilder == null)
			throw new IllegalArgumentException("'vizMapPropertyBuilder' must not be null");
		if (iconMgr == null)
			throw new IllegalArgumentException("'iconMgr' must not be null");
		
		this.model = model;
		this.vizMapPropertyBuilder = vizMapPropertyBuilder;
		this.iconMgr = iconMgr;
		disabledBtnIcon = getIcon(null, VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT);
		
		init();
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public VisualPropertySheetItemModel<T> getModel() {
		return model;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(final boolean selected) {
		this.selected = selected;
		requestFocus();
		
		if (!selected && getModel().isVisualMappingAllowed())
			getPropSheetTbl().clearSelection(); // This prevents some bugs when editing discrete mappings!
		
		updateSelection();
	}
	
	public void expand() {
		if (!isExpanded() && model.isVisualMappingAllowed()) {
			getMappingPnl().setVisible(true);
			firePropertyChange("expanded", false, true);
		}
	}
	
	public void collapse() {
		if (isExpanded() && model.isVisualMappingAllowed()) {
			getMappingPnl().setVisible(false);
			firePropertyChange("expanded", true, false);
		}
	}
	
	public boolean isExpanded() {
		return model.isVisualMappingAllowed() && getMappingPnl().isVisible();
	}
	
	public void fitToWidth(final int width) {
		if (getModel().isVisualMappingAllowed()) {
			updateMappingPanelSize();	
			final Dimension prefSize = getPropSheetPnl().getPreferredSize();
			// Set new preferred width to the mapping panel
			getPropSheetPnl().setPreferredSize(new Dimension(width, prefSize.height));
			
			if (getMappingPnl().isVisible()) {
				getPropSheetPnl().repaint();
				getPropSheetPnl().revalidate();
				revalidate();
			}
		}
	}
	
	public void update() {
		updateSelection();
		updateDefaultButton();
		updateBypassButton();
		updateMapping();
	}
	
	public void updateDefaultButton() {
		if (defaultBtn != null) {
			defaultBtn.setIcon(getIcon(model.getDefaultValue(), VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT));
			defaultBtn.setToolTipText(model.getDefaultValue() == null ?
					null : "Default Value: " + getDisplayString(model.getDefaultValue()));
		}
	}
	
	public void updateBypassButton() {
		if (bypassBtn != null) {
			final LockedValueState state = model.getLockedValueState();
			bypassBtn.setEnabled(isEnabled() && state != LockedValueState.DISABLED);
			
			// TODO: create better icons
			bypassBtn.setIcon(getIcon(model.getLockedValue(), VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT));
			
			if (state == LockedValueState.ENABLED_UNIQUE_VALUE)
				bypassBtn.setText(null);
			else if (state == LockedValueState.ENABLED_MULTIPLE_VALUES)
				bypassBtn.setText("?");
			else
				bypassBtn.setText("");
			
			final String elementsStr = model.getTargetDataType() == CyNode.class ? "nodes" : "edges";
			String toolTipText = "No bypass";
			
			if (state == LockedValueState.DISABLED)
				toolTipText = "To bypass the visual property, first select one or more " + elementsStr;
			else if (state == LockedValueState.ENABLED_UNIQUE_VALUE)
				toolTipText = "Bypass: " + getDisplayString(model.getLockedValue());
			else if (state == LockedValueState.ENABLED_MULTIPLE_VALUES)
				toolTipText = "The selected " + elementsStr + " have different bypass values";
			
			bypassBtn.setToolTipText(toolTipText);
		}
	}
	
	public void updateMapping() {
		updateMappingIcon();
		updateRemoveMappingBtn();
		updateMappingRowHeight();
		
		if (mappingPnl != null && mappingPnl.isVisible())
			updateMappingPanelSize();
	}
	
	public void setMessage(final String text, final MessageType type) {
		if (type == null) {
			// If no message icon, just set the tooltip to the item itself
			setToolTipText(text);
			getMsgIconLbl().setToolTipText(null);
		} else {
			// If the message icon will be displayed, set the tooltip to the icon
			setToolTipText(null);
			getMsgIconLbl().setToolTipText(text);
		}
		
		updateMessageIcon(type);
	}
	
	@Override
	public void setEnabled(final boolean enabled) {
		if (enabled == isEnabled())
			return;
		
		if (model.getVisualPropertyDependency() != null) {
			getDependencyCkb().setEnabled(enabled);
		} else {
			getDefaultBtn().setEnabled(enabled);
			getBypassBtn().setEnabled(enabled && model.getLockedValueState() != LockedValueState.DISABLED);
			
			if (model.isVisualMappingAllowed()) {
				if (!enabled && getMappingPnl().isVisible())
					getMappingBtn().doClick();
				
				getMappingBtn().setEnabled(enabled);
				getShowMappingBtn().setEnabled(enabled);
				getRemoveMappingBtn().setEnabled(enabled);
				getPropSheetPnl().setEnabled(enabled);
				getPropSheetTbl().setEnabled(enabled);
			}
		}
		
		getTitleLbl().setForeground(UIManager.getColor(enabled ? "Label.foreground" : "Label.disabledForeground"));
		super.setEnabled(enabled);
	}

	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void init() {
		setBackground(BG_COLOR);
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BTN_BORDER_COLOR));
		setLayout(new BorderLayout());
		
		add(getTopPnl(), BorderLayout.NORTH);
		add(getMappingPnl(), BorderLayout.CENTER);
		
		model.addPropertyChangeListener("defaultValue", new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent e) {
				updateDefaultButton();
			}
		});
		model.addPropertyChangeListener("lockedValue", new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent e) {
				updateBypassButton();
			}
		});
		model.addPropertyChangeListener("lockedValueState", new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent e) {
				updateBypassButton();
			}
		});
		model.addPropertyChangeListener("renderingEngine", new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent e) {
				updateDefaultButton();
				updateBypassButton();
			}
		});
		model.addPropertyChangeListener("visualMappingFunction", new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent e) {
				final VisualMappingFunction<?, T> mapping = (VisualMappingFunction<?, T>) e.getNewValue();
				final VisualMappingFunctionFactory mappingFactory = vizMapPropertyBuilder.getMappingFactory(mapping);
				
				final VizMapperProperty<VisualProperty<?>, String, VisualMappingFunctionFactory> columnProp = 
						vizMapPropertyBuilder.getColumnProperty(propSheetPnl);
				columnProp.setValue(mapping == null ? null : mapping.getMappingColumnName());
				columnProp.setInternalValue(mappingFactory);
				
				final VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<?, ?>> mappingProp =
						vizMapPropertyBuilder.getMappingTypeProperty(propSheetPnl);
				mappingProp.setValue(mappingFactory);
				mappingProp.setInternalValue(mapping);
				
				if (mapping == null)
					vizMapPropertyBuilder.removeMappingProperties(getPropSheetPnl());
				
				updateMapping();
			}
		});
	}

	private JPanel getTopPnl() {
		if (topPnl == null) {
			topPnl = new JPanel();
			topPnl.setBorder(BorderFactory.createEmptyBorder(1, 2, 0, 2));
			topPnl.setLayout(new BoxLayout(topPnl, BoxLayout.X_AXIS));
			
			topPnl.add(Box.createHorizontalStrut(BTN_H_MARGIN));
			
			if (model.getVisualPropertyDependency() == null)
				topPnl.add(getDefaultBtn());
			else
				topPnl.add(getDependencyCkb());
			
			topPnl.add(Box.createHorizontalStrut(BTN_H_MARGIN));
			
			if (model.isVisualMappingAllowed()) {
				topPnl.add(Box.createHorizontalStrut(BTN_H_MARGIN));
				topPnl.add(getMappingBtn());
				topPnl.add(Box.createHorizontalStrut(BTN_H_MARGIN));
			}
			
			if (model.isLockedValueAllowed()) {
				topPnl.add(Box.createHorizontalStrut(BTN_H_MARGIN));
				topPnl.add(getBypassBtn());
				topPnl.add(Box.createHorizontalStrut(BTN_H_MARGIN));
			}
			
			topPnl.add(Box.createHorizontalStrut(4));
			topPnl.add(getTitleLbl());
			topPnl.add(Box.createHorizontalGlue());
			topPnl.add(Box.createHorizontalStrut(2));
			topPnl.add(getMsgIconLbl());
			
			if (model.isVisualMappingAllowed())
				topPnl.add(getShowMappingBtn()); // Network view properties don't have visual mappings
			
			topPnl.add(Box.createRigidArea(new Dimension(0, HEIGHT)));
			
			updateSelection();
		}
		
		return topPnl;
	}
	
	protected JPanel getMappingPnl() {
		if (mappingPnl == null) {
			mappingPnl = new JPanel();
			mappingPnl.setBackground(new Color(125, 125, 125));
			mappingPnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(125, 125, 125)));
			mappingPnl.setLayout(new BorderLayout());
			mappingPnl.setVisible(false);
			
			mappingPnl.add(getPropSheetPnl(), BorderLayout.CENTER);
			
			final JPanel bottomPnl = new JPanel();
			bottomPnl.setLayout(new BoxLayout(bottomPnl, BoxLayout.X_AXIS));
			bottomPnl.add(Box.createHorizontalGlue());
			bottomPnl.add(Box.createHorizontalGlue());
			bottomPnl.add(getRemoveMappingBtn());
			bottomPnl.add(Box.createHorizontalGlue());
			bottomPnl.add(Box.createVerticalStrut(PROP_SHEET_ROW_HEIGHT));
			bottomPnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
			
			mappingPnl.add(bottomPnl, BorderLayout.SOUTH);
			
			mappingPnl.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(final ComponentEvent e) {
					updateMappingPanelSize();
				}
			});
		}
		
		return mappingPnl;
	}

	public PropertySheetPanel getPropSheetPnl() {
		if (propSheetPnl == null) {
			propSheetPnl = new PropertySheetPanel();
			propSheetPnl.setToolBarVisible(false);
			propSheetPnl.setMode(PropertySheetPanel.VIEW_AS_FLAT_LIST);
			propSheetPnl.setTable(getPropSheetTbl());
			propSheetPnl.getTable().setEditorFactory(new PropertyEditorRegistry());
			propSheetPnl.getTable().setRendererFactory(new PropertyRendererRegistry());
			
			final VisualMappingFunction<?, T> mapping = model.getVisualMappingFunction();

			if (mapping == null) {
				// Create the properties for a new visual mapping
				final VisualProperty<?> vp = (VisualProperty<?>) model.getVisualProperty();
				vizMapPropertyBuilder.buildProperty(vp, propSheetPnl);
			} else {
				// There is already a visual mapping for this style's property
				final VisualMappingFunctionFactory mappingFactory = vizMapPropertyBuilder.getMappingFactory(mapping);
				vizMapPropertyBuilder.buildProperty(mapping, propSheetPnl, mappingFactory);
				updateMappingRowHeight();
			}
			
			// This is necessary because the Property Sheet Table steals the mouse wheel event
			// which prevents the parent scroll pane from receiving it when the mouse is over the property sheet panel.
			final Container c = propSheetPnl.getTable().getParent().getParent();
			
			if (c instanceof JScrollPane)
				c.addMouseWheelListener(new CustomMouseWheelListener((JScrollPane)c));
		}
		
		return propSheetPnl;
	}
	
	protected PropertySheetTable getPropSheetTbl() {
		if (propSheetTbl == null) {
			propSheetTbl = new VizMapPropertySheetTable();
			propSheetTbl.setRowHeight(PROP_SHEET_ROW_HEIGHT);
			propSheetTbl.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			
			propSheetTbl.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(final ComponentEvent e) {
					updateMappingPanelSize();
				}
			});
		}
		
		return propSheetTbl;
	}
	
	protected ExpandCollapseButton getShowMappingBtn() {
		if (expandCollapseBtn == null) {
			expandCollapseBtn = new ExpandCollapseButton(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent ae) {
					if (getMappingPnl().isShowing())
						collapse();
					else
						expand();
				}
			});
			
			final Dimension d = new Dimension(VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT);
			expandCollapseBtn.setMinimumSize(d);
			expandCollapseBtn.setPreferredSize(d);
			expandCollapseBtn.setMaximumSize(d);
			expandCollapseBtn.setForeground(FG_COLOR);
			expandCollapseBtn.setBackground(BG_COLOR);
			expandCollapseBtn.setBorder(
					BorderFactory.createEmptyBorder(BUTTON_V_PAD, BUTTON_H_PAD, BUTTON_V_PAD, BUTTON_H_PAD));
			expandCollapseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			getMappingPnl().addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(final ComponentEvent ce) {
					if (expandCollapseBtn != null && !expandCollapseBtn.isSelected())
						expandCollapseBtn.setSelected(true);
				}
				@Override
				public void componentHidden(final ComponentEvent ce) {
					if (expandCollapseBtn != null && expandCollapseBtn.isSelected())
						expandCollapseBtn.setSelected(false);
				}
			});
		}
		
		return expandCollapseBtn;
	}
	
	protected JButton getDefaultBtn() {
		if (defaultBtn == null) {
			defaultBtn = new VizMapperButton();
			defaultBtn.setUI(new VPButtonUI(VPButtonUI.SOUTH));
			defaultBtn.setDisabledIcon(disabledBtnIcon);
			updateDefaultButton();
		}
		
		return defaultBtn;
	}

	protected JCheckBox getDependencyCkb() {
		if (dependencyCkb == null) {
			dependencyCkb = new JCheckBox();
			dependencyCkb.setSelected(model.isDependencyEnabled());
		}
		
		return dependencyCkb;
	}
	
	protected JToggleButton getMappingBtn() {
		if (mappingBtn == null) {
			mappingBtn = new VizMapperToggleButton();
			mappingBtn.setIcon(getIcon(null, VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT));
			mappingBtn.setUI(new VPButtonUI(VPButtonUI.SOUTH));
			mappingBtn.setHorizontalAlignment(JLabel.CENTER);
			mappingBtn.setDisabledIcon(disabledBtnIcon);
			updateMappingIcon();
			
			mappingBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent ae) {
					if (getMappingPnl().isShowing())
						collapse();
					else
						expand();
				}
			});
			getMappingPnl().addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(final ComponentEvent ce) {
					if (mappingBtn != null)
						mappingBtn.setSelected(true);
				}
				@Override
				public void componentHidden(final ComponentEvent ce) {
					if (mappingBtn != null)
						mappingBtn.setSelected(false);
				}
			});
		}
		
		return mappingBtn;
	}
	
	protected JButton getBypassBtn() {
		if (bypassBtn == null) {
			bypassBtn = new VizMapperButton();
			bypassBtn.setIcon(getIcon(model.getLockedValue(), VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT));
			bypassBtn.setUI(new VPButtonUI(VPButtonUI.SOUTH));
			bypassBtn.setDisabledIcon(disabledBtnIcon);
			updateBypassButton();
		}
		
		return bypassBtn;
	}
	
	protected JButton getRemoveMappingBtn() {
		if (removeMappingBtn == null) {
			removeMappingBtn = new JButton("Remove Visual Mapping");
			removeMappingBtn.setUI(new VPButtonUI(VPButtonUI.CENTER));
			updateRemoveMappingBtn();
		}
		
		return removeMappingBtn;
	}
	
	private JLabel getTitleLbl() {
		if (titleLbl == null) {
			titleLbl = new JLabel(model.getTitle());
			titleLbl.setHorizontalAlignment(SwingConstants.LEFT);
		}
		
		return titleLbl;
	}
	
	public JLabel getMsgIconLbl() {
		if (msgIconLbl == null) {
			msgIconLbl = new JLabel(" ");
			msgIconLbl.setHorizontalTextPosition(SwingConstants.CENTER);
			msgIconLbl.setPreferredSize(new Dimension(MSG_ICON_WIDTH, MSG_ICON_HEIGHT));
		}
		
		return msgIconLbl;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Icon getIcon(final T value, final int width, final int height) {// TODO should not be part of this class
		Icon icon = null;
		final RenderingEngine<?> engine = model.getRenderingEngine();
		
		if (engine != null && value != null)
			icon = engine.createIcon((VisualProperty) model.getVisualProperty(), value, width, height);
		
		if (icon == null)
			icon = new EmptyIcon(width, height);
		
		return icon;
	}
	
	private void updateMappingPanelSize() {
		final PropertySheetTable tbl = getPropSheetPnl().getTable();
		tbl.repaint();
		int h = 0;
		
		for (int i = 0; i < tbl.getRowCount(); i++) {
			h += tbl.getRowHeight(i);
		}
		
		final int TOTAL_PAD = 6;
		getPropSheetPnl().setPreferredSize(new Dimension(tbl.getWidth()+TOTAL_PAD, h+TOTAL_PAD));
		tbl.repaint();
		getPropSheetPnl().repaint();
		getPropSheetPnl().revalidate();
		revalidate();
	}
	
	private void updateSelection() {
		getTopPnl().setBackground(selected ? SELECTED_BG_COLOR : BG_COLOR);
		
		if (model.isVisualMappingAllowed()) {
			getShowMappingBtn().setBackground(selected ? SELECTED_BG_COLOR : BG_COLOR);
			getShowMappingBtn().repaint();
		}
	}
	
	private void updateMessageIcon(final MessageType type) {
		String iconName = null;
		
		if (type == MessageType.INFO)
			iconName = IconManager.INFO_ICON;
		else if (type == MessageType.WARNING)
			iconName = IconManager.WARN_ICON;
		else if (type == MessageType.ERROR)
			iconName = IconManager.ERROR_ICON;
		
		getMsgIconLbl().setIcon(iconName != null ? iconMgr.getIcon(iconName) : null);
	}
	
	private void updateMappingIcon() {
		final JToggleButton btn = getMappingBtn();
		final VisualMappingFunction<?, T> mapping = model.getVisualMappingFunction();
		final String colName = mapping != null ? mapping.getMappingColumnName() : null;
		
		if (mapping == null) {
			btn.setText("");
			btn.setToolTipText("No Visual Mapping");
		} else if (mapping instanceof DiscreteMapping) {
			btn.setText("Dm");
			btn.setToolTipText("Discrete Mapping for column \"" + colName + "\"");
		} else if (mapping instanceof ContinuousMapping) {
			btn.setText("Cm");
			btn.setToolTipText("Continuous Mapping for column \"" + colName + "\"");
		} else if (mapping instanceof PassthroughMapping) {
			btn.setText("Pm");
			btn.setToolTipText("Passthrough Mapping for column \"" + colName + "\"");
		}
	}
	
	private void updateRemoveMappingBtn() {
		if (removeMappingBtn != null)
			removeMappingBtn.setEnabled(model.getVisualMappingFunction() != null);
	}
	
	private void updateMappingRowHeight() {
		if (model.getVisualMappingFunction() instanceof ContinuousMapping && getPropSheetTbl().getRowCount() > 2)
			getPropSheetTbl().setRowHeight(2, MAPPING_IMG_ROW_HEIGHT);
		else
			getPropSheetTbl().setRowHeight(PROP_SHEET_ROW_HEIGHT);
	}
	
	private String getDisplayString(final Object value) {
		String s = null;
		
		if (value instanceof VisualPropertyValue) {
			s = ((VisualPropertyValue)value).getDisplayName();
		} else if (value instanceof Font) {
			s  = ((Font)value).getFontName();
		} else if (value instanceof Color) {
			final Color c  = (Color)value;
			int r = c.getRed();
			int g = c.getGreen();
			int b = c.getBlue();
			s = String.format("R:%s G:%s B:%s - #%02x%02x%02x", r, g, b, r, g, b).toUpperCase();
		} else if (value != null) {
			s = value.toString();
		}
		
		return s;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class VizMapPropertySheetTable extends PropertySheetTable {

		private VizMapperProperty<?, ?, ?> editingVizMapperProperty;

		VizMapPropertySheetTable() {
			setKeyBindings();
			
			addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					if (!isEditing() && editingVizMapperProperty != null)
						firePropertyChange("editingVizMapperProperty", editingVizMapperProperty, 
								editingVizMapperProperty = null);
				}
			});
		}
		
		@Override
		public Component prepareEditor(final TableCellEditor editor, final  int row, final int column) {
			final Item selectedItem = (Item) getValueAt(row, 0);
			final VizMapperProperty<?, ?, ?> prop = (VizMapperProperty<?, ?, ?>) selectedItem.getProperty();	
			
			if (prop != editingVizMapperProperty)
				firePropertyChange("editingVizMapperProperty", editingVizMapperProperty, editingVizMapperProperty = prop);
			
			return super.prepareEditor(editor, row, column);
		}
		
		@Override
		public boolean isCellEditable(final int row, final int column) {
			return isRowSelected(row) && super.isCellEditable(row, column);
		}
		
		@Override
		public String getToolTipText(MouseEvent me) {
			final Point pt = me.getPoint();
			final int row = rowAtPoint(pt);

			if (row < 0) {
				return null;
			} else {
				final Property prop = ((Item) getValueAt(row, 0)).getProperty();
				final Color fontColor;

				if (prop != null && prop.getValue() != null && prop.getValue().getClass() == Color.class)
					fontColor = (Color) prop.getValue();
				else
					fontColor = Color.DARK_GRAY;

				final String colorString = Integer.toHexString(fontColor.getRGB());

				if (prop == null)
					return null;

				final String displayName = prop.getDisplayName();
				
				if (displayName.equals(VizMapPropertyBuilder.GRAPHICAL_MAP_VIEW))
					return "Click to edit this mapping...";

				if (displayName.equals(VizMapPropertyBuilder.COLUMN) || displayName.equals(VizMapPropertyBuilder.MAPPING_TYPE))
					return "<html><Body BgColor=\"white\"><font Size=\"4\" Color=\"#" + colorString.substring(2, 8)
							+ "\"><strong>" + prop.getDisplayName() + " = " + prop.getValue()
							+ "</font></strong></body></html>";
				else
					return "<html><Body BgColor=\"white\"><font Size=\"4\" Color=\"#" + colorString.substring(2, 8)
							+ "\"><strong>" + prop.getDisplayName() + ": "
							+ (prop.getValue() != null ? getDisplayString(prop.getValue()) : "<i>default value</i>")
							+ "</font></strong></body></html>";
			}
		}
		
		private void setKeyBindings() {
			final ActionMap actionMap = getActionMap();
			final InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), KeyAction.VK_SPACE);
			actionMap.put(KeyAction.VK_SPACE, new KeyAction(KeyAction.VK_SPACE));
		}

		private class KeyAction extends AbstractAction {

			final static String VK_SPACE = "VK_SPACE";
			
			KeyAction(final String actionCommand) {
				putValue(ACTION_COMMAND_KEY, actionCommand);
			}

			@Override
			public void actionPerformed(final ActionEvent e) {
				final int[] selectedRows = getSelectedRows();
				
				if (selectedRows == null || selectedRows.length == 0 || isEditing())
					return;
				
				final String cmd = e.getActionCommand();
				
				if (cmd.equals(VK_SPACE)) {
					// The default SPACE key action only works when the editable cell has focus,
					// but it is nicer to make it work whenever the row is selected.
					if (selectedRows.length == 1)
						editCellAt(selectedRows[0], 1);
				}
			}
		}
	}
	
	private static class ExpandCollapseButton extends JButton {
		
	    BufferedImage openImg, closedImg, disabledClosedImg;
	    final int PAD = 8;
	    
	    public ExpandCollapseButton(final ActionListener al) {
	        addActionListener(al);
	        setRequestFocusEnabled(true);
	    }
	    
	    @Override
	    protected void paintComponent(final Graphics g) {
	        super.paintComponent(g);
	        
	        if (openImg == null)
	        	createImages();
	        
	        final Graphics2D g2 = (Graphics2D)g;
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        
	        if (isSelected())
	            g2.drawImage(openImg, 0, 0, this);
	        else
	            g2.drawImage(isEnabled() ? closedImg : disabledClosedImg, 0, 0, this);
	    }
	    
	    @Override
	    public void repaint() {
	    	openImg = closedImg = null;
	    	super.repaint();
	    }
	    
	    private void createImages() {
	        int w = getSize().width;
	        int h = getSize().height;
	        
	        openImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	        Graphics2D g2 = openImg.createGraphics();
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g2.setPaint(getBackground());
	        g2.fillRect(0, 0, w, h);
	        int[] x = { PAD, w-PAD, w/2 };
	        int[] y = { PAD, PAD,   h-PAD };
	        Polygon p = new Polygon(x, y, 3);
	        g2.setPaint(getForeground());
	        g2.fill(p);
	        g2.dispose();
	        
	        closedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	        g2 = closedImg.createGraphics();
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g2.setPaint(getBackground());
	        g2.fillRect(0, 0, w, h);
	        x = new int[] { PAD, w-PAD, w-PAD };
	        y = new int[] { h/2, PAD,   h-PAD };
	        p = new Polygon(x, y, 3);
	        g2.setPaint(getForeground());
	        g2.fill(p);
	        g2.dispose();
	        
	        disabledClosedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	        g2 = disabledClosedImg.createGraphics();
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g2.setPaint(getBackground());
	        g2.fillRect(0, 0, w, h);
	        x = new int[] { PAD, w-PAD, w-PAD };
	        y = new int[] { h/2, PAD,   h-PAD };
	        p = new Polygon(x, y, 3);
	        g2.setPaint(getForeground().brighter().brighter());
	        g2.fill(p);
	        g2.dispose();
	    }
	}

	static class VizMapperButton extends JButton {

		static final Color BG_COLOR_1 = new Color(226, 226, 226);
		static final Color BG_COLOR_2 = Color.WHITE;
		static final Color BG_DISABLED_COLOR = new Color(248, 248, 248);
		
		VizMapperButton() {
			setContentAreaFilled(false);
		}

		@Override
		protected void paintComponent(final Graphics g) {
			paintBackground(g, this);
			super.paintComponent(g);
		}
		
		static void paintBackground(final Graphics g, final AbstractButton btn) {
			final Graphics2D g2 = (Graphics2D) g.create();
			final Paint p;
			
			if (btn.isEnabled())
				p = new GradientPaint(new Point(0, 0), BG_COLOR_1, new Point(0, btn.getHeight()), BG_COLOR_2);
			else
				p = BG_DISABLED_COLOR;
			
			g2.setPaint(p);
			g2.fillRect(0, 0, btn.getWidth(), btn.getHeight());
			g2.dispose();
		}
	}
	
	static class VizMapperToggleButton extends JToggleButton {
		
		VizMapperToggleButton() {
			setContentAreaFilled(false);
		}
		
		@Override
		protected void paintComponent(final Graphics g) {
			VizMapperButton.paintBackground(g, this);
			super.paintComponent(g);
		}
	}
	
	static class VPButtonUI extends BasicButtonUI {

		public static final int NORTH = 1;
		public static final int CENTER = 2;
		public static final int SOUTH = 3;
		
		static final Color BG_OVER_COLOR = new Color(224, 232, 246);
		static final Color BORDER_OVER_COLOR = new Color(152, 180, 226);
		static final Color BG_SELECTED_COLOR = new Color(193, 210, 238);
		static final Color BORDER_SELECTED_COLOR = new Color(125, 125, 125);
		
		private final int anchor;
		
		private Border borderEnabled;
		private Border borderDisabled;
		
		public VPButtonUI() {
			this(CENTER);
		}
		
		public VPButtonUI(final int anchor) {
			super();
			this.anchor = anchor;
		}

		@Override
		public void installUI(final JComponent c) {
			super.installUI(c);

			final AbstractButton btn = (AbstractButton) c;
			btn.setRolloverEnabled(true);
			btn.setVerticalTextPosition(SwingConstants.CENTER);
			btn.setHorizontalTextPosition(SwingConstants.CENTER);
			btn.setVerticalAlignment(SwingConstants.CENTER);
			btn.setHorizontalAlignment(SwingConstants.CENTER);
			btn.setFocusPainted(false);
			btn.setBackground(BG_COLOR);
			btn.setForeground(Color.DARK_GRAY);
			btn.setFocusable(false);
			btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			Border padBorder = BorderFactory.createEmptyBorder(BUTTON_V_PAD + (anchor == NORTH ? 1 : 0),
															   BUTTON_H_PAD,
															   BUTTON_V_PAD + (anchor == SOUTH ? 1 : 0),
															   BUTTON_H_PAD);
			{
				Border border = BorderFactory.createMatteBorder(anchor == NORTH ? 0 : BTN_BORDER_WIDTH,
																BTN_BORDER_WIDTH,
																anchor == SOUTH ? 0: BTN_BORDER_WIDTH,
																BTN_BORDER_WIDTH,
																BTN_BORDER_COLOR);
				borderEnabled =  BorderFactory.createCompoundBorder(border, padBorder);
			}
			{
				Border border = BorderFactory.createMatteBorder(anchor == NORTH ? 0 : BTN_BORDER_WIDTH,
																BTN_BORDER_WIDTH,
																anchor == SOUTH ? 0: BTN_BORDER_WIDTH,
																BTN_BORDER_WIDTH,
																BTN_BORDER_DISABLED_COLOR);
				borderDisabled =  BorderFactory.createCompoundBorder(border, padBorder);
			}
			
			btn.setBorder(btn.isEnabled() ? borderEnabled : borderDisabled);
			
			btn.addPropertyChangeListener("enabled", new PropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent evt) {
					btn.setBorder(evt.getNewValue() == Boolean.TRUE ? borderEnabled : borderDisabled);
				}
			});
		}

		@Override
		public void paint(final Graphics g, final JComponent c) {
			final AbstractButton btn = (AbstractButton) c;
			final ButtonModel btnModel = btn.getModel();
			
			if (btnModel.isRollover() || btnModel.isArmed() || btnModel.isSelected()) {
				final Color oldColor = g.getColor();
				
				if (btnModel.isSelected())
					g.setColor(BG_SELECTED_COLOR);
				else
					g.setColor(BG_OVER_COLOR);
				
				g.fillRect(0, 0, c.getWidth() - 1, c.getHeight());

//				if (btnModel.isSelected())
//					g.setColor(BORDER_SELECTED_COLOR);
//				else
//					g.setColor(BORDER_OVER_COLOR);
//				
//				if (btnModel.isSelected())
//					g.drawPolyline(new int[]{ 0,                 0, c.getWidth() - 1, c.getWidth() - 1 },
//								   new int[]{ c.getHeight() - 1, 0, 0,                c.getHeight() - 1 },
//								   4);
//				else
//					g.drawRect(0, 0, c.getWidth() - 1 , c.getHeight() - 1);
				
				g.setColor(oldColor);
			}
			
			super.paint(g, c);
		}
		
		static int getPreferredWidth() {
			return VALUE_ICON_WIDTH + 2*BTN_H_MARGIN + 2*BTN_BORDER_WIDTH + 2*BUTTON_H_PAD;
		}
	}
	
	private class CustomMouseWheelListener implements MouseWheelListener {

		private JScrollBar bar;
		private int previousValue;
		private JScrollPane parentScrollPane;
		private JScrollPane customScrollPane;

		CustomMouseWheelListener(final JScrollPane scrollPane) {
			this.customScrollPane = scrollPane;
			this.bar = this.customScrollPane.getVerticalScrollBar();
		}

		@Override
		public void mouseWheelMoved(final MouseWheelEvent e) {
			JScrollPane parent = getParentScrollPane();
			
			if (parent != null) {
				if (e.getWheelRotation() < 0) {
					if (this.bar.getValue() == 0 && this.previousValue == 0)
						parent.dispatchEvent(cloneEvent(e));
				} else {
					if (this.bar.getValue() == getMax() && this.previousValue == getMax())
						parent.dispatchEvent(cloneEvent(e));
				}
				
				this.previousValue = this.bar.getValue();
			} else {
				this.customScrollPane.removeMouseWheelListener(this);
			}
		}

		/**
		 * @return The maximum value of the scrollbar.
		 */
		private int getMax() {
			return this.bar.getMaximum() - this.bar.getVisibleAmount();
		}
		
		/** @return The parent scroll pane, or null if there is no parent. */
		private JScrollPane getParentScrollPane() {
			if (this.parentScrollPane == null) {
				Component parent = this.customScrollPane.getParent();
				
				while (!(parent instanceof JScrollPane) && parent != null)
					parent = parent.getParent();
				
				this.parentScrollPane = (JScrollPane) parent;
			}
			
			return this.parentScrollPane;
		}

		private MouseWheelEvent cloneEvent(final MouseWheelEvent e) {
			return new MouseWheelEvent(getParentScrollPane(), e.getID(),
					e.getWhen(), e.getModifiers(), 1, 1,
					e.getClickCount(), false, e.getScrollType(),
					e.getScrollAmount(), e.getWheelRotation());
		}
	}
}
