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

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.model.LockedValueState;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.CyComboBoxPropertyEditor;
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

	private static final int HEIGHT = 32;
	private static final int PROP_SHEET_ROW_HEIGHT = 24;
	private static final int MAPPING_IMG_ROW_HEIGHT = 90;
	
	private static final int VALUE_ICON_WIDTH = 24;
	private static final int VALUE_ICON_HEIGHT = 24;
	private static final int BUTTON_V_PAD = 2;
	private static final int BUTTON_H_PAD = 4;
	
	
	private Color fgColor = new Color(115, 115, 115);
	private Color bgColor = UIManager.getColor("Table.background");
	private Color selectedBgColor = new Color(222, 234, 252);
	private Color borderColor = UIManager.getColor("Separator.foreground");
	private Color disabledColor = UIManager.getColor("Panel.background");
	
	private JPanel topPnl;
	private JPanel mappingPnl;
	private PropertySheetPanel propSheetPnl;
	private ExpandCollapseButton expandCollapseBtn;
	private JButton defaultBtn;
	private JToggleButton mappingBtn;
	private DropDownMenuButton bypassBtn;
	private JCheckBox dependencyCkb;
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
	
	public void expand() {
		getMappingPnl().setVisible(true);
	}
	
	public void collapse() {
		getMappingPnl().setVisible(false);
	}
	
	public void update() {
		updateSelection();
		updateDefaultButton();
		updateBypassButton(getBypassBtn());
		updateMapping();
		
		if (model.getVisualMappingFunction() != null)
			updateMappingIcon();
		
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
			topPnl.setBorder(BorderFactory.createEmptyBorder(1, 2, 0, 2));
			topPnl.setLayout(new BoxLayout(topPnl, BoxLayout.X_AXIS));
			
			if (model.getVisualPropertyDependency() == null)
				topPnl.add(getDefaultBtn());
			else
				topPnl.add(getDependencyCkb());
			
			if (model.isVisualMappingAllowed())
				topPnl.add(getMappingBtn());
			
			if (model.isLockedValueAllowed())
				topPnl.add(getBypassBtn());
			
			topPnl.add(Box.createHorizontalStrut(4));
			topPnl.add(getTitleLbl());
			topPnl.add(Box.createHorizontalGlue());
			topPnl.add(Box.createHorizontalStrut(4));
			
//			if (model.isVisualMappingAllowed())
//				topPnl.add(getShowMappingBtn()); // Network view properties don't have visual mappings
			
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
			expandCollapseBtn.setForeground(fgColor);
			expandCollapseBtn.setBackground(bgColor);
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
			final Icon icon = getIcon(model.getDefaultValue(), VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT);
			defaultBtn = new JButton(icon);
			defaultBtn.setUI(new VPButtonUI());
			final Object value = model.getDefaultValue();
			
			if (value != null)
				defaultBtn.setToolTipText("Default Value: " + value.toString());
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
			final Icon icon = getIcon(null, VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT); // TODO
			mappingBtn = new JToggleButton(icon);
			mappingBtn.setUI(new VPButtonUI());
			mappingBtn.setHorizontalAlignment(JLabel.CENTER);
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
	
	protected DropDownMenuButton getBypassBtn() {
		if (bypassBtn == null) {
			bypassBtn = new DropDownMenuButton(getBypassMenu(), false);
			bypassBtn.setIcon(getIcon(model.getLockedValue(), VALUE_ICON_WIDTH, VALUE_ICON_HEIGHT));
			bypassBtn.setUI(new VPButtonUI());
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
		
//		btn.setVisible(state != LockedValueState.DISABLED);
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
		
		final String elementsStr = model.getTargetDataType() == CyNode.class ? "nodes" : "edges";
		
		String toolTipText = "No bypass";
		
		if (state == LockedValueState.DISABLED)
			toolTipText = "To bypass the visual property, first select one or more " + elementsStr;
		else if (state == LockedValueState.ENABLED_UNIQUE_VALUE)
			toolTipText = "Bypass: " + model.getLockedValue();
		else if (state == LockedValueState.ENABLED_MULTIPLE_VALUES)
			toolTipText = "The selected " + elementsStr + " have different bypass values";
		
		btn.setToolTipText(toolTipText);
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
		
	    BufferedImage openImg, closedImg;
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
	            g2.drawImage(closedImg, 0, 0, this);
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
	    }
	}
	
	static class VPButtonUI extends BasicButtonUI {

		private static final int H_MARGIN = 1;
		private static final int BORDER_WIDTH = 1;
		
		private static final Color BG_OVER_COLOR = new Color(224, 232, 246);
		private static final Color BORDER_OVER_COLOR = new Color(152, 180, 226);
		private static final Color BG_SELECTED_COLOR = new Color(193, 210, 238);
		private static final Color BORDER_SELECTED_COLOR = new Color(125, 125, 125);
		
		public VPButtonUI() {
			super();
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
			btn.setBackground(UIManager.getColor("Table.background"));
			btn.setForeground(Color.DARK_GRAY);
			btn.setFocusable(false);
			btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			// margin
			final Border b1 = BorderFactory.createEmptyBorder(1, H_MARGIN, 0, H_MARGIN);
			// actual border
			final Border b2 = BorderFactory.createMatteBorder(BORDER_WIDTH, BORDER_WIDTH, 0, BORDER_WIDTH,
					UIManager.getColor("Separator.foreground"));
			// padding
			final Border b3 = 
					BorderFactory.createEmptyBorder(BUTTON_V_PAD, BUTTON_H_PAD, BUTTON_V_PAD + 1, BUTTON_H_PAD);
			final CompoundBorder cb1 = 
					BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(b1, b2), b3);
			btn.setBorder(cb1);
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

				if (btnModel.isSelected())
					g.setColor(BORDER_SELECTED_COLOR);
				else
					g.setColor(BORDER_OVER_COLOR);
				
				if (btnModel.isSelected())
					g.drawPolyline(new int[]{ 0,                 0, c.getWidth() - 1, c.getWidth() - 1 },
								   new int[]{ c.getHeight() - 1, 0, 0,                c.getHeight() - 1 },
								   4);
				else
					g.drawRect(0, 0, c.getWidth() - 1 , c.getHeight() - 1);
				
				g.setColor(oldColor);
			}
			
			super.paint(g, c);
		}
		
		static int getPreferredWidth() {
			return VALUE_ICON_WIDTH + 2*H_MARGIN + 2*BORDER_WIDTH + 2*BUTTON_H_PAD;
		}
	}
}
