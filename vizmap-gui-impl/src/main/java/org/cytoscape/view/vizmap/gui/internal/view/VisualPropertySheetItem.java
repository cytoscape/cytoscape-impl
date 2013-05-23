package org.cytoscape.view.vizmap.gui.internal.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.model.LockedValueState;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.CyComboBoxPropertyEditor;
import org.jdesktop.swingx.icon.EmptyIcon;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;
import com.l2fprod.common.swing.plaf.blue.BlueishButtonUI;

@SuppressWarnings("serial")
public class VisualPropertySheetItem<T> extends JPanel {

	private static final int HEIGHT = 32;
	private static final int PROP_SHEET_ROW_HEIGHT = 24;
	private static final int MAPPING_IMG_ROW_HEIGHT = 90;
	
	private static final int VALUE_ICON_WIDTH = 24;
	private static final int VALUE_ICON_HEIGHT = 24;
	private static final int BUTTON_V_PAD = 2;
	private static final int BUTTON_H_PAD = 4;
	private static final int BUTTON_MARGIN = 1;
	
	
	private Color fgColor = new Color(115, 115, 115);
	private Color bgColor = UIManager.getColor("Table.background");
	private Color selectedBgColor = new Color(222, 234, 252);
	private Color borderColor = UIManager.getColor("Separator.foreground");
	private Color disabledColor = UIManager.getColor("Panel.background");
	
	private JPanel topPnl;
	private JPanel mappingPnl;
	private PropertySheetPanel propSheetPnl;
	private ExpandCollapseButton showMappingBtn;
	private JButton defaultBtn;
	private JCheckBox dependencyCkb;
	private DropDownMenuButton bypassBtn;
	private JPopupMenu bypassMenu;
	private JLabel titleLbl;
	private PropertySheetTable propSheetTbl;
	
	private boolean selected;
	
	private final VisualPropertySheetItemModel<T> model;
	private final EditorManager editorManager;
	private final VizMapPropertyBuilder vizMapPropertyBuilder;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VisualPropertySheetItem(final VisualPropertySheetItemModel<T> model,
								   final EditorManager editorManager,
								   final VizMapPropertyBuilder vizMapPropertyBuilder) {
		if (model == null)
			throw new IllegalArgumentException("'model' must not be null");
		if (editorManager == null)
			throw new IllegalArgumentException("'editorManager' must not be null");
		if (vizMapPropertyBuilder == null)
			throw new IllegalArgumentException("'vizMapPropertyBuilder' must not be null");
		
		this.model = model;
		this.editorManager = editorManager;
		this.vizMapPropertyBuilder = vizMapPropertyBuilder;
		
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
		updateSelection();
	}
	
	public void update() {
		updateSelection();
		updateDefaultButton();
		updateBypassButton(getBypassBtn());
		updateMapping();
		
		if (mappingPnl != null && mappingPnl.isVisible())
			updateMappingPanelHeight();
	}
	
	public void updateDefaultButton() {
		getDefaultBtn().setIcon(getIcon(model.getDefaultValue(), VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT));
	}
	
	public void updateBypassButton() {
		updateBypassButton(getBypassBtn());
	}
	
	public void updateMapping() {
		// TODO
//		getMappingPnl().remove(getPropSheetPnl());
//		propSheetPnl = null;
//		propSheetTbl = null;
//		getMappingPnl().add(getPropSheetPnl(), BorderLayout.CENTER);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void init() {
		setBackground(bgColor);
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));
		setLayout(new BorderLayout());
		
		add(getTopPnl(), BorderLayout.NORTH);
		add(getMappingPnl(), BorderLayout.CENTER);
		
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
	}

	private JPanel getTopPnl() {
		if (topPnl == null) {
			topPnl = new JPanel();
			topPnl.setLayout(new BoxLayout(topPnl, BoxLayout.X_AXIS));
			
			if (model.isVisualMappingAllowed())
				topPnl.add(getShowMappingBtn()); // Network view properties don't have visual mappings
			else
				topPnl.add(Box.createRigidArea(new Dimension(0, HEIGHT)));
			
			if (model.getVisualPropertyDependency() == null)
				topPnl.add(getDefaultBtn());
			else
				topPnl.add(getDependencyCkb());
			
			if (model.isLockedValueAllowed())
				topPnl.add(getBypassBtn());
			
			topPnl.add(Box.createHorizontalStrut(4));
			topPnl.add(getTitleLbl());
			topPnl.add(Box.createHorizontalGlue());
			
			updateSelection();
		}
		
		return topPnl;
	}
	
	protected JPanel getMappingPnl() {
		if (mappingPnl == null) {
			mappingPnl = new JPanel();
			mappingPnl.setLayout(new BorderLayout());
			mappingPnl.setVisible(false);
			mappingPnl.add(getPropSheetPnl(), BorderLayout.CENTER);
			mappingPnl.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(final ComponentEvent e) {
					updateMappingPanelHeight();
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
// TODO			
//			propSheetPnl.addPropertySheetChangeListener(new PropertyChangeListener() {
//				@Override
//				public void propertyChange(final PropertyChangeEvent e) {System.out.println("\n--> propertyChange...");
//					updateMappingPanelHeight(); // FIXME
//				}
//			});
			
//			propSheetPnl.getTable().addPropertyChangeListener(new PropertyChangeListener() {
//				@Override
//				public void propertyChange(PropertyChangeEvent e) {
//					System.out.println("\n[ TABLE change ] - " + e.getPropertyName()+": " + e.getNewValue() +"|"+ e.getNewValue());
//					updateMappingPanelHeight();
//				}
//			});
			
//			propSheetPnl.getTable().getModel().addTableModelListener(new TableModelListener() {
//				@Override
//				public void tableChanged(final TableModelEvent e) {
////					if (e.getType() == TableModelEvent.INSERT) {
//						System.out.println("\n@**** [ tableChanged ] ****@\n");
//						for (int row = e.getFirstRow(); row <= e.getLastRow(); row++) {
//							if (propSheetTbl.getRowCount() <= row) break;
//							
//							final Object val = propSheetTbl.getValueAt(row, 0);
//							
//							if (val instanceof Item) {
//								final VizMapperProperty<?, ?, ?> prop =
//										(VizMapperProperty<?, ?, ?>) ((Item) val).getProperty();
//								
//								if (prop != null && prop.getCellType() == CellType.CONTINUOUS)
//									propSheetTbl.setRowHeight(row, MAPPING_IMG_ROW_HEIGHT); // FIXME
//							}
//						}
//						
//						updateMappingPanelHeight(); // FIXME
////					}
//				}
//			});
			
			final VisualMappingFunction<?, T> mapping = model.getVisualMappingFunction();

			if (mapping == null) {
				// Create the properties for a new visual mapping
				final VisualProperty<?> vp = (VisualProperty<?>) model.getVisualProperty();
				vizMapPropertyBuilder.buildProperty(vp, propSheetPnl);
			} else {
				// There is already a visual mapping for this style's property
				final CyComboBoxPropertyEditor mappingSelector = (CyComboBoxPropertyEditor) editorManager
						.getDefaultComboBoxEditor("mappingTypeEditor");
				final Set<Object> factories = mappingSelector.getAvailableValues();
				VisualMappingFunctionFactory mappingFactory = null;
				
				for (final Object f : factories) {
					final VisualMappingFunctionFactory factory = (VisualMappingFunctionFactory) f;
					final Class<?> type = factory.getMappingFunctionType();
					
					if (type.isAssignableFrom(mapping.getClass())) {
						mappingFactory = factory;
						break;
					}
				}
				
				vizMapPropertyBuilder.buildProperty(mapping, propSheetPnl, mappingFactory);
			}
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
					updateMappingPanelHeight();
				}
			});
		}
		
		return propSheetTbl;
	}
	
	protected ExpandCollapseButton getShowMappingBtn() {
		if (showMappingBtn == null) {
			showMappingBtn = new ExpandCollapseButton(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent ae) {
					getMappingPnl().setVisible(!getMappingPnl().isShowing());
					showMappingBtn.toggle();
				}
			});
			
			final Dimension d = new Dimension(VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT);
			showMappingBtn.setMinimumSize(d);
			showMappingBtn.setPreferredSize(d);
			showMappingBtn.setMaximumSize(d);
			showMappingBtn.setForeground(fgColor);
			showMappingBtn.setBackground(bgColor);
			showMappingBtn.setBorder(
					BorderFactory.createEmptyBorder(BUTTON_V_PAD, BUTTON_H_PAD, BUTTON_V_PAD, BUTTON_H_PAD));
			showMappingBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
		
		return showMappingBtn;
	}
	
	protected JButton getDefaultBtn() {
		if (defaultBtn == null) {
			final Icon icon = getIcon(model.getDefaultValue(), VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT);
			defaultBtn = new JButton(icon);
			setButtonLookAndFeel(defaultBtn);
			final Object value = model.getDefaultValue();
			
			if (value != null)
				defaultBtn.setToolTipText("Default value: " + value.toString());
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
	
	protected DropDownMenuButton getBypassBtn() {
		if (bypassBtn == null) {
			bypassBtn = new DropDownMenuButton(getBypassMenu(), false);
			bypassBtn.setIcon(getIcon(model.getLockedValue(), VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT));
			setButtonLookAndFeel(bypassBtn);
			updateBypassButton(bypassBtn);
		}
		
		return bypassBtn;
	}
	
	protected JPopupMenu getBypassMenu() {
		if (bypassMenu == null) {
			bypassMenu = new JPopupMenu();
		}
		
		return bypassMenu;
	}
	
	private JLabel getTitleLbl() {
		if (titleLbl == null) {
			titleLbl = new JLabel(model.getTitle());
			titleLbl.setHorizontalAlignment(SwingConstants.LEFT);
		}
		
		return titleLbl;
	}
	
	private void updateBypassButton(final JButton btn) {
		final LockedValueState state = model.getLockedValueState();
		btn.setEnabled(state != LockedValueState.DISABLED);
		
		// TODO: create better icons
		btn.setIcon(getIcon(model.getLockedValue(), VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT));
		
		if (state == LockedValueState.ENABLED_UNIQUE_VALUE) {
			btn.setText(null);
		} else if (state == LockedValueState.ENABLED_MULTIPLE_VALUES) {
			btn.setText("?");
		} else {
			btn.setText("");
		}
		
		String toolTipText = "No locked values";
		
		if (state == LockedValueState.DISABLED)
			toolTipText = "To set locked values, first select one or more nodes and edges";
		else if (state == LockedValueState.ENABLED_UNIQUE_VALUE)
			toolTipText = "Locked value: " + model.getLockedValue();
		else if (state == LockedValueState.ENABLED_MULTIPLE_VALUES)
			toolTipText = "The selected elements have different locked values";
		
		btn.setToolTipText(toolTipText);
//		btn.setBackground(state == LockedValueState.DISABLED ? DISABLED_COLOR : BG_COLOR);
		btn.setVisible(state != LockedValueState.DISABLED);
	}
	
	private void setButtonLookAndFeel(final JButton btn) {
		btn.setUI(new BlueishButtonUI());
		btn.setVerticalTextPosition(SwingConstants.CENTER);
		btn.setHorizontalTextPosition(SwingConstants.CENTER);
		btn.setVerticalAlignment(SwingConstants.CENTER);
		btn.setHorizontalAlignment(SwingConstants.CENTER);
		btn.setFocusPainted(false);
		btn.setBackground(bgColor);
		btn.setForeground(Color.DARK_GRAY);
		btn.setFocusable(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		final Border b1 = BorderFactory.createLineBorder(bgColor, BUTTON_MARGIN); // margin
		final Border b2 = BorderFactory.createLineBorder(borderColor, 1); // actual border
		final Border b3 = BorderFactory.createEmptyBorder(BUTTON_V_PAD, BUTTON_H_PAD, BUTTON_V_PAD, BUTTON_H_PAD); // padding
		btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(b1, b2), b3));
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
	
	private void updateMappingPanelHeight() {
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
		getTopPnl().setBackground(selected ? selectedBgColor : bgColor);
		
		if (model.isVisualMappingAllowed()) {
			getShowMappingBtn().setBackground(selected ? selectedBgColor : bgColor);
			getShowMappingBtn().repaint();
		}
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private static class VizMapPropertySheetTable extends PropertySheetTable {

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

				if (prop.getDisplayName().equals(VizMapPropertyBuilder.GRAPHICAL_MAP_VIEW))
					return "Click to edit this mapping...";

				if (prop.getDisplayName() == "Controlling Attribute" || prop.getDisplayName() == "Mapping Type")
					return "<html><Body BgColor=\"white\"><font Size=\"4\" Color=\"#" + colorString.substring(2, 8)
							+ "\"><strong>" + prop.getDisplayName() + " = " + prop.getValue()
							+ "</font></strong></body></html>";
				else if (prop.getSubProperties() == null || prop.getSubProperties().length == 0)
					return "<html><Body BgColor=\"white\"><font Size=\"4\" Color=\"#" + colorString.substring(2, 8)
							+ "\"><strong>" + prop.getDisplayName() + "</font></strong></body></html>";

				return null;
			}
		}
// TODO
//		@Override
//		public int getRowHeight(int row) {
//			if (getColumnCount() < 1)
//				super.getRowHeight(row);
//			
//			final Object val = getValueAt(row, 0);
//			
//			if (val instanceof Item) {
//				final VizMapperProperty<?, ?, ?> prop = (VizMapperProperty<?, ?, ?>) ((Item) val).getProperty();
//				
//				if (prop != null && prop.getCellType() == CellType.CONTINUOUS)
//					return MAPPING_IMG_ROW_HEIGHT;
//			}
//			
//			return PROP_SHEET_ROW_HEIGHT;
//		}
	}
	
	private static class ExpandCollapseButton extends JButton {
		
	    private boolean selected;
	    BufferedImage open, closed;
	    final int PAD = 8;
	    
	    public ExpandCollapseButton(final ActionListener al) {
	        addActionListener(al);
	        setRequestFocusEnabled(true);
	    }
	    
	    public void toggle() {
	        selected = !selected;
	        repaint();
	    }
	    
	    @Override
	    protected void paintComponent(final Graphics g) {
	        super.paintComponent(g);
	        
	        if (open == null)
	        	createImages();
	        
	        final Graphics2D g2 = (Graphics2D)g;
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        
	        if (selected)
	            g2.drawImage(open, 0, 0, this);
	        else
	            g2.drawImage(closed, 0, 0, this);
	    }
	    
	    @Override
	    public void repaint() {
	    	open = closed = null;
	    	super.repaint();
	    }
	    
	    private void createImages() {
	        int w = getSize().width;
	        int h = getSize().height;
	        
	        open = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	        Graphics2D g2 = open.createGraphics();
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g2.setPaint(getBackground());
	        g2.fillRect(0, 0, w, h);
	        int[] x = { PAD, w-PAD, w/2 };
	        int[] y = { PAD, PAD,   h-PAD };
	        Polygon p = new Polygon(x, y, 3);
	        g2.setPaint(getForeground());
	        g2.fill(p);
	        g2.dispose();
	        
	        closed = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	        g2 = closed.createGraphics();
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g2.setPaint(getBackground());
	        g2.fillRect(0, 0, w, h);
	        x = new int[] { PAD, w-PAD, PAD };
	        y = new int[] { PAD, h/2,   h-PAD };
	        p = new Polygon(x, y, 3);
	        g2.setPaint(getForeground());
	        g2.fill(p);
	        g2.dispose();
	    }
	}
}
