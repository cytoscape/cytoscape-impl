package org.cytoscape.view.vizmap.gui.internal.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.Collator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.TableCellEditor;

import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.model.LockedValueState;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.VisualPropertyUtil;
import org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor.ContinuousMappingEditorPanel;
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

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class VisualPropertySheetItem<T> extends JPanel implements Comparable<VisualPropertySheetItem<?>> {

	public enum MessageType { INFO, WARNING, ERROR	}

	private static final String DISCRETE_ICON = IconManager.ICON_ELLIPSIS_V + " " + IconManager.ICON_ELLIPSIS_V;
	private static final String CONTINUOUS_ICON = IconManager.ICON_ELLIPSIS_V + " " + IconManager.ICON_ARROWS_V;
	private static final String PASSTHROUGH_ICON = IconManager.ICON_ELLIPSIS_V + " " + IconManager.ICON_ANGLE_RIGHT;
	
	private static final int HEIGHT = 32;
	private static final int PROP_SHEET_ROW_HEIGHT = 24;
	private static final int MAPPING_IMG_ROW_HEIGHT = 90;
	
	private static final int VALUE_ICON_WIDTH = 24;
	private static final int VALUE_ICON_HEIGHT = 24;
	private static final int BUTTON_V_PAD = 2;
	private static final int BUTTON_H_PAD = 4;
	
	private static final int MSG_ICON_WIDTH = 18;
	private static final int MSG_ICON_HEIGHT = 15;
	
	static final int BTN_H_MARGIN = 1;
	static final int BTN_BORDER_WIDTH = 1;
	
	private JPanel topPnl;
	private JPanel mappingPnl;
	private PropertySheetPanel propSheetPnl;
	private ExpandCollapseButton showMappingBtn;
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
	private final ServicesUtil servicesUtil;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VisualPropertySheetItem(final VisualPropertySheetItemModel<T> model,
								   final VizMapPropertyBuilder vizMapPropertyBuilder,
								   final ServicesUtil servicesUtil) {
		if (model == null)
			throw new IllegalArgumentException("'model' must not be null");
		if (vizMapPropertyBuilder == null)
			throw new IllegalArgumentException("'vizMapPropertyBuilder' must not be null");
		if (servicesUtil == null)
			throw new IllegalArgumentException("'servicesUtil' must not be null");
		
		this.model = model;
		this.vizMapPropertyBuilder = vizMapPropertyBuilder;
		this.servicesUtil = servicesUtil;
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
		
		if (model.getVisualPropertyDependency() == null) {
			updateDefaultButton();
			updateBypassButton();
			updateMapping();
		} else {
			updateDependencyCkb();
		}
	}
	
	public void updateDefaultButton() {
		if (defaultBtn != null) {
			defaultBtn.setIcon(getIcon(model.getDefaultValue(), VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT));
			defaultBtn.setToolTipText(model.getDefaultValue() == null ?
					null : "Default Value: " + VisualPropertyUtil.getDisplayString(model.getDefaultValue()));
		}
	}
	
	public void updateBypassButton() {
		if (bypassBtn != null) {
			var state = model.getLockedValueState();
			bypassBtn.setEnabled(isEnabled() && state != LockedValueState.DISABLED);
			
			if (state == LockedValueState.ENABLED_MULTIPLE_VALUES) {
				var iconManager = servicesUtil.get(IconManager.class);
				
				bypassBtn.setForeground(LookAndFeelUtil.getInfoColor());
				bypassBtn.setFont(iconManager.getIconFont(19.0f));
				bypassBtn.setText(IconManager.ICON_QUESTION_CIRCLE);
			} else {
				bypassBtn.setForeground(getForegroundColor());
				bypassBtn.setFont(UIManager.getFont("Button.font"));
				bypassBtn.setText("");
			}
			
			bypassBtn.setIcon(getIcon(model.getLockedValue(), VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT));
			
			var elementsStr = model.getTargetDataType() == CyNode.class ? "nodes" : "edges";
			var toolTipText = "No bypass";
			
			if (state == LockedValueState.DISABLED)
				toolTipText = "To bypass the visual property, first select one or more " + elementsStr;
			else if (state == LockedValueState.ENABLED_UNIQUE_VALUE)
				toolTipText = "Bypass: " + VisualPropertyUtil.getDisplayString(model.getLockedValue());
			else if (state == LockedValueState.ENABLED_MULTIPLE_VALUES)
				toolTipText = "The selected " + elementsStr + " have different bypass values";
			
			bypassBtn.setToolTipText(toolTipText);
		}
	}
	
	public void updateMapping() {
		if (!model.isVisualMappingAllowed())
			return;
		
		final VisualMappingFunction<?, T> mapping = getModel().getVisualMappingFunction();
		final VisualMappingFunctionFactory mappingFactory = vizMapPropertyBuilder.getMappingFactory(mapping);
		
		final VizMapperProperty<VisualProperty<?>, String, VisualMappingFunctionFactory> columnProp = 
				vizMapPropertyBuilder.getColumnProperty(getPropSheetPnl());
		columnProp.setValue(mapping == null ? null : mapping.getMappingColumnName());
		columnProp.setInternalValue(mappingFactory);
		
		final VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<?, ?>> mappingProp =
				vizMapPropertyBuilder.getMappingTypeProperty(getPropSheetPnl());
		mappingProp.setValue(mappingFactory);
		mappingProp.setInternalValue(mapping);
		
		if (mapping == null) {
			vizMapPropertyBuilder.removeMappingProperties(getPropSheetPnl());
		} else {
			final int rowCount = getPropSheetTbl().getRowCount();
			final int[] selectedRows = getPropSheetTbl().getSelectedRows();
			vizMapPropertyBuilder.createMappingProperties(mapping, getPropSheetPnl(), mappingFactory);
			
			if (selectedRows != null && selectedRows.length > 0 && getPropSheetTbl().getRowCount() == rowCount) {
				// Keep the same rows selected
				final ListSelectionModel selModel = getPropSheetTbl().getSelectionModel();
				
				for (final int r : selectedRows)
					selModel.addSelectionInterval(r, r);
			}
		}
		
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
		
		getTitleLbl().setEnabled(enabled);
		
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
		
		super.setEnabled(enabled);
	}
	
	@Override
	public int compareTo(final VisualPropertySheetItem<?> other) {
		final VisualPropertySheetItemModel<?> m1 = this.getModel();
		final VisualPropertySheetItemModel<?> m2 = other.getModel();
		String title1 = m1.getTitle();
		String title2 = m2.getTitle();
		
		final VisualPropertyDependency<?> dep1 = m1.getVisualPropertyDependency();
		final VisualPropertyDependency<?> dep2 = m2.getVisualPropertyDependency();
		
		// Put dependencies in the end of the sorted list
		if (dep1 == null && dep2 != null)
			return -1;
		if (dep1 != null && dep2 == null)
			return 1;
		
		if (dep1 != null && dep2 != null) {
			title1 = dep1.getDisplayName();
			title2 = dep2.getDisplayName();
		}
		
		// Locale-specific sorting
		final Collator collator = Collator.getInstance(Locale.getDefault());
		collator.setStrength(Collator.PRIMARY);
		
		return collator.compare(title1, title2);
	}
	
	@Override
	public String toString() {
		return model != null ? model.getTitle() : "?";
	}

	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void init() {
		setBackground(getBackgroundColor());
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, getButtonBorderColor()));
		setLayout(new BorderLayout());
		
		add(getTopPnl(), BorderLayout.NORTH);
		add(getMappingPnl(), BorderLayout.CENTER);
		
		model.addPropertyChangeListener("defaultValue", evt -> {
			updateDefaultButton();
		});
		model.addPropertyChangeListener("lockedValue", evt -> {
			updateBypassButton();
		});
		model.addPropertyChangeListener("lockedValueState", evt -> {
			updateBypassButton();
		});
		model.addPropertyChangeListener("renderingEngine", evt -> {
			updateDefaultButton();
			updateBypassButton();
		});
		model.addPropertyChangeListener("visualMappingFunction", evt -> {
			// Update our tracer
			if (!model.isVisualMappingAllowed())
				return;
			// Note that if this isn't a Continuous mapper, this will just return
			ContinuousMappingEditorPanel.resetTracer(model.getVisualProperty());
			updateMapping();
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
			mappingPnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
			mappingPnl.setLayout(new BorderLayout());
			mappingPnl.setVisible(false);
			
			mappingPnl.add(getPropSheetPnl(), BorderLayout.CENTER);
			
			final JPanel bottomPnl = new JPanel();
			bottomPnl.setLayout(new BoxLayout(bottomPnl, BoxLayout.X_AXIS));
			bottomPnl.add(Box.createHorizontalGlue());
			bottomPnl.add(getRemoveMappingBtn());
			bottomPnl.add(Box.createRigidArea(new Dimension(0, PROP_SHEET_ROW_HEIGHT)));
			bottomPnl.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
			
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
		if (showMappingBtn == null) {
			showMappingBtn = new ExpandCollapseButton(false, evt -> {
				if (getMappingPnl().isShowing())
					collapse();
				else
					expand();
			});
			getMappingPnl().addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(final ComponentEvent ce) {
					if (showMappingBtn != null && !showMappingBtn.isSelected())
						showMappingBtn.setSelected(true);
				}
				@Override
				public void componentHidden(final ComponentEvent ce) {
					if (showMappingBtn != null && showMappingBtn.isSelected())
						showMappingBtn.setSelected(false);
				}
			});
		}
		
		return showMappingBtn;
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
			dependencyCkb.setOpaque(false);
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
			
			mappingBtn.addActionListener(evt -> {
				if (getMappingPnl().isShowing())
					collapse();
				else
					expand();
				
				updateMappingIcon();
			});
			getMappingPnl().addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(final ComponentEvent ce) {
					if (mappingBtn != null)
						mappingBtn.setSelected(true);
					
					updateMappingIcon();
				}
				@Override
				public void componentHidden(final ComponentEvent ce) {
					if (mappingBtn != null)
						mappingBtn.setSelected(false);
					
					updateMappingIcon();
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
			final IconManager iconManager = servicesUtil.get(IconManager.class);
			
			removeMappingBtn = new JButton(IconManager.ICON_TRASH_O); // icon-trash
			removeMappingBtn.setToolTipText("Remove Mapping");
			removeMappingBtn.setBorderPainted(false);
			removeMappingBtn.setContentAreaFilled(false);
			removeMappingBtn.setOpaque(false);
			removeMappingBtn.setFocusable(false);
			removeMappingBtn.setFont(iconManager.getIconFont(18.0f));
			removeMappingBtn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
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
			final IconManager iconManager = servicesUtil.get(IconManager.class);
			
			msgIconLbl = new JLabel(" ");
			msgIconLbl.setHorizontalTextPosition(SwingConstants.CENTER);
			msgIconLbl.setPreferredSize(new Dimension(MSG_ICON_WIDTH, MSG_ICON_HEIGHT));
			msgIconLbl.setFont(iconManager.getIconFont(16.0f));
			
			// Hack to prolong a tooltip’s visible delay
			// Thanks to: http://tech.chitgoks.com/2010/05/31/disable-tooltip-delay-in-java-swing/
			msgIconLbl.addMouseListener(new MouseAdapter() {
			    final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
			    final int dismissDelayMinutes = (int) TimeUnit.MINUTES.toMillis(1); // 1 minute
			    
			    @Override
			    public void mouseEntered(final MouseEvent e) {
			        ToolTipManager.sharedInstance().setDismissDelay(dismissDelayMinutes);
			    }
			 
			    @Override
			    public void mouseExited(final MouseEvent e) {
			        ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
			    }
			});
		}
		
		return msgIconLbl;
	}
	
	static Color getBackgroundColor() {
		return UIManager.getColor("Table.background");
	}
	
	static Color getSelectedBackgroundColor() {
		return UIManager.getColor("Table.selectionBackground");
	}
	
	static Color getForegroundColor() {
		return UIManager.getColor("Table.foreground");
	}
	
	static Color getButtonBorderColor() {
		return UIManager.getColor("Separator.foreground");
	}
	
	static Color getDisabledButtonBorderColor() {
		return UIManager.getColor("Table.background");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Icon getIcon(T value, int width, int height) {// TODO should not be part of this class
		Icon icon = null;
		var engine = model.getRenderingEngine();
		
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
		getTopPnl().setBackground(selected ? getSelectedBackgroundColor() : getBackgroundColor());
		repaint();
	}
	
	private void updateMessageIcon(final MessageType type) {
		String text = null;
		Color fg = null;
		
		if (type == MessageType.INFO) {
			text = IconManager.ICON_INFO_CIRCLE;
			fg = LookAndFeelUtil.getInfoColor();
		} else if (type == MessageType.WARNING) {
			text = IconManager.ICON_WARNING;
			fg = LookAndFeelUtil.getWarnColor();
		} else if (type == MessageType.ERROR) {
			text = IconManager.ICON_MINUS_CIRCLE;
			fg = LookAndFeelUtil.getErrorColor();
		}
		
		getMsgIconLbl().setText(text);
		getMsgIconLbl().setForeground(fg);
	}
	
	private void updateMappingIcon() {
		if (!model.isVisualMappingAllowed())
			return;
		
		final JToggleButton btn = getMappingBtn();
		final VisualMappingFunction<?, T> mapping = model.getVisualMappingFunction();
		final String colName = mapping != null ? mapping.getMappingColumnName() : null;
		
		final IconManager iconManager = servicesUtil.get(IconManager.class);
		btn.setFont(iconManager.getIconFont(16.0f));
		
		if (btn.isSelected())
			btn.setForeground(UIManager.getColor("Table.focusCellForeground"));
		else
			btn.setForeground(getForegroundColor());
		
		if (mapping == null) {
			btn.setText("");
			btn.setToolTipText("No Mapping");
		} else if (mapping instanceof DiscreteMapping) {
			btn.setText(DISCRETE_ICON);
			btn.setToolTipText("Discrete Mapping for column \"" + colName + "\"");
		} else if (mapping instanceof ContinuousMapping) {
			btn.setText(CONTINUOUS_ICON);
			btn.setToolTipText("Continuous Mapping for column \"" + colName + "\"");
		} else if (mapping instanceof PassthroughMapping) {
			btn.setText(PASSTHROUGH_ICON);
			btn.setToolTipText("Passthrough Mapping for column \"" + colName + "\"");
		}
	}
	
	private void updateRemoveMappingBtn() {
		if (removeMappingBtn != null)
			removeMappingBtn.setEnabled(model.getVisualMappingFunction() != null);
	}
	
	private void updateMappingRowHeight() {
		if (model.isVisualMappingAllowed()) {
			if (model.getVisualMappingFunction() instanceof ContinuousMapping && getPropSheetTbl().getRowCount() > 2)
				getPropSheetTbl().setRowHeight(2, MAPPING_IMG_ROW_HEIGHT);
			else
				getPropSheetTbl().setRowHeight(PROP_SHEET_ROW_HEIGHT);
		}
	}
	
	private void updateDependencyCkb() {
		if (model.getVisualPropertyDependency() != null)
			getDependencyCkb().setSelected(model.isDependencyEnabled());
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

				if (prop == null)
					return null;

				final String displayName = prop.getDisplayName();
				
				if (displayName.equals(VizMapPropertyBuilder.GRAPHICAL_MAP_VIEW))
					return "Click to edit this mapping...";

				if (displayName.equals(VizMapPropertyBuilder.COLUMN) || displayName.equals(VizMapPropertyBuilder.MAPPING_TYPE))
					return prop.getDisplayName() + ": " + prop.getValue();
				else
					return "<html>" + prop.getDisplayName() + ": "
							+ (prop.getValue() != null ? 
									VisualPropertyUtil.getDisplayString(prop.getValue()) : "<i>default value</i>")
							+ "</html>";
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
	
	private class ExpandCollapseButton extends JButton {
		
		private Icon expandedIcon;
		private Icon collapsedIcon;
		
	    public ExpandCollapseButton(final boolean selected, final ActionListener al) {
	    	expandedIcon = UIManager.getIcon("Tree.expandedIcon");
	    	collapsedIcon = UIManager.getIcon("Tree.rightToLeftCollapsedIcon");
    		
    		if (collapsedIcon == null) {
    			collapsedIcon = new MirrorIcon(UIManager.getIcon("Tree.collapsedIcon"));
    			expandedIcon = new MirrorIcon(expandedIcon); // Mirror this one as well, so they can better align
    		}
	    	
	        setRequestFocusEnabled(true);
			setContentAreaFilled(false);
			setOpaque(false);
			setFocusPainted(false);
			setHorizontalAlignment(RIGHT);
			
			Dimension d = new Dimension(
					expandedIcon.getIconWidth() + 2 * BUTTON_H_PAD,
					expandedIcon.getIconHeight() + 2 * BUTTON_V_PAD
			);
			setMinimumSize(d);
			setPreferredSize(d);
			setMaximumSize(d);
			setBorder(BorderFactory.createEmptyBorder(BUTTON_V_PAD, BUTTON_H_PAD, BUTTON_V_PAD, BUTTON_H_PAD));
			
			addActionListener(al);
			setSelected(selected);
	    }
	    
	    @Override
	    public void setSelected(final boolean b) {
	    	super.setSelected(b);
	    	updateIcon();
	    }
	    
	    @Override
	    public void setEnabled(boolean b) {
	    	super.setEnabled(b);
	    	updateIcon();
	    }
	    
	    private void updateIcon() {
			if (isEnabled())
				setIcon(isSelected() ? expandedIcon : collapsedIcon);
			else
				setIcon(null);
		}
	}

	static class VizMapperButton extends JButton {

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
				p = new GradientPaint(
						new Point(0, 0), UIManager.getColor("Button.background"), 
						new Point(0, btn.getHeight()), UIManager.getColor("Table.background"));
			else
				p = UIManager.getColor("Button.background");
			
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
		
		final Color BG_OVER_COLOR = UIManager.getColor("Table.selectionBackground");
		final Color BG_SELECTED_COLOR = UIManager.getColor("Table.focusCellBackground");
		
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
			btn.setBackground(getBackgroundColor());
			btn.setForeground(getForegroundColor());
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
																getButtonBorderColor());
				borderEnabled =  BorderFactory.createCompoundBorder(border, padBorder);
			}
			{
				Border border = BorderFactory.createMatteBorder(anchor == NORTH ? 0 : BTN_BORDER_WIDTH,
																BTN_BORDER_WIDTH,
																anchor == SOUTH ? 0: BTN_BORDER_WIDTH,
																BTN_BORDER_WIDTH,
																getDisabledButtonBorderColor());
				borderDisabled =  BorderFactory.createCompoundBorder(border, padBorder);
			}
			
			btn.setBorder(btn.isEnabled() ? borderEnabled : borderDisabled);
			
			btn.addPropertyChangeListener("enabled", evt -> {
				btn.setBorder(Boolean.TRUE.equals(evt.getNewValue()) ? borderEnabled : borderDisabled);
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
	
	private class MirrorIcon implements Icon {

		private final Icon originalIcon;
		
	    public MirrorIcon(Icon originalIcon) {
	        this.originalIcon = originalIcon;
	    }

	    @Override
	    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
	        Graphics2D g2 = (Graphics2D) g.create();
	        g2.translate(getIconWidth(), 0);
	        g2.scale(-1, 1);
	        originalIcon.paintIcon(c, g2, x, y);
	        g2.dispose();
	    }

		@Override
		public int getIconWidth() {
			return originalIcon.getIconWidth();
		}

		@Override
		public int getIconHeight() {
			return originalIcon.getIconHeight();
		}
	}
}
