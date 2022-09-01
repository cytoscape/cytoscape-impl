package org.cytoscape.cg.util;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.cg.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.cg.internal.util.ViewUtil.styleToolBarButton;
import static org.cytoscape.util.swing.IconManager.ICON_CHECK_SQUARE_O;
import static org.cytoscape.util.swing.IconManager.ICON_EDIT;
import static org.cytoscape.util.swing.IconManager.ICON_PLUS;
import static org.cytoscape.util.swing.IconManager.ICON_SQUARE_O;
import static org.cytoscape.util.swing.IconManager.ICON_TRASH_O;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.cg.internal.util.ViewUtil;
import org.cytoscape.cg.internal.util.VisualPropertyIconFactory;
import org.cytoscape.cg.model.AbstractURLImageCustomGraphics;
import org.cytoscape.cg.model.BitmapCustomGraphics;
import org.cytoscape.cg.model.CGComparator;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.cg.model.NullCustomGraphics;
import org.cytoscape.cg.model.SVGCustomGraphics;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes", "serial" })
public class ImageCustomGraphicsSelector extends JPanel {
	
	private static final String IMG_FILES_DESCRIPTION = "Image file (PNG, GIF, JPEG or SVG)";
	private static final String[] IMG_EXTENSIONS = { "jpg", "jpeg", "png", "gif", "svg" };
	
	private static final int IMAGE_WIDTH = 120;
	private static final int IMAGE_HEIGHT = 68;
	private static final int ITEM_BORDER_WIDTH = 1;
	private static final int ITEM_MARGIN = 2;
	private static final int ITEM_PAD = 2;
	
	final Color BG_COLOR;
	final Color FG_COLOR;
	final Color SEL_BG_COLOR;
	final Color SEL_FG_COLOR;
	final Color FOCUS_BORDER_COLOR;
	final Color FOCUS_OVERLAY_COLOR;
	final Color BORDER_COLOR;
	
	/** Flag to ensure that infinite loops do not occur with ActionEvents. */
    private boolean firingActionEvent;
    private String actionCommand = "imageCustomGraphicsSelectorChanged";
	
    private JButton addBtn;
    private JToggleButton editBtn;
	private JPanel toolBarPanel;
	private JButton selectAllBtn;
	private JButton selectNoneBtn;
	private JButton removeImagesBtn;
    
	private ImageGrid imageGrid;
	private JScrollPane gridScrollPane;
	
	private final int minColumns = 1;
	private final int maxColumns = 0; // 0 means any number of columns
	
	private int cols;
	private boolean editMode;
	private boolean editingName;
	private boolean selectionIsAdjusting;
	
	private int selectionHead;
	private int selectionTail;
	
	private List<CyCustomGraphics> allImages;
	private CyCustomGraphics selectedImage;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	public ImageCustomGraphicsSelector(CyServiceRegistrar serviceRegistrar) {
		this(false, serviceRegistrar);
	}
	
	public ImageCustomGraphicsSelector(boolean editMode, CyServiceRegistrar serviceRegistrar) {
		super(true);
		
		this.serviceRegistrar = serviceRegistrar;
		
		allImages = new ArrayList<>();
		
		BG_COLOR = UIManager.getColor("Table.background");
		FG_COLOR = UIManager.getColor("Table.foreground");
		SEL_BG_COLOR = UIManager.getColor("Table.focusCellBackground");
		SEL_FG_COLOR = UIManager.getColor("Table.focusCellForeground");
		FOCUS_BORDER_COLOR = UIManager.getColor("Table.selectionBackground");
		FOCUS_OVERLAY_COLOR = new Color(FOCUS_BORDER_COLOR.getRed(), FOCUS_BORDER_COLOR.getGreen(), FOCUS_BORDER_COLOR.getBlue(), 100);
		BORDER_COLOR = UIManager.getColor("Separator.foreground");
		
		init();
		
		setEditMode(editMode);
		
		// If starting on edit mode, then hide the edit button--it's always on edit mode from now on!
		if (editMode)
			getEditBtn().setVisible(false);
		
		update(serviceRegistrar.getService(CustomGraphicsManager.class).getAllCustomGraphics(), null);
	}
	
	/**
	 * Add a listener to be notified when the user finally chooses an image (e.g. double-click an item).
	 */
	public void addActionListener(ActionListener l) {
		listenerList.add(ActionListener.class, l);
	}

	public void removeActionListener(ActionListener l) {
		listenerList.remove(ActionListener.class, l);
	}

	public ActionListener[] getActionListeners() {
		return listenerList.getListeners(ActionListener.class);
	}
	
	/**
     * Sets the action command that should be included in the event
     * sent to action listeners.
     *
     * @param aCommand  a string containing the "command" that is sent
     *                  to action listeners; the same listener can then
     *                  do different things depending on the command it
     *                  receives
     */
    public void setActionCommand(String aCommand) {
        actionCommand = aCommand;
    }

    /**
     * Returns the action command that is included in the event sent to
     * action listeners.
     *
     * @return  the string containing the "command" that is sent
     *          to action listeners.
     */
    public String getActionCommand() {
        return actionCommand;
    }

    public boolean isEditMode() {
		return editMode;
	}
	
	public void setEditMode(boolean editMode) {
		if (editMode != this.editMode) {
			selectionIsAdjusting = true;
			this.editMode = editMode;
			
			try {
				update();
				
				if (!editMode)
					getImageGrid().setSelectedValue(selectedImage, getImageGrid().isShowing());
			} finally {
				selectionIsAdjusting = false;
			}
			
			firePropertyChange("editMode", !editMode, editMode);
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		getEditBtn().setEnabled(enabled);
		getSelectAllBtn().setEnabled(enabled);
		getSelectNoneBtn().setEnabled(enabled);
		getRemoveImagesBtn().setEnabled(enabled);
		getImageGrid().setEnabled(enabled);
	}
	
	public void update(Collection<CyCustomGraphics> images, CyCustomGraphics selectedValue) {
		allImages.clear();
		
		if (images != null) {
			for (var cg : images) {
				if (cg instanceof NullCustomGraphics == false)
					allImages.add(cg);
			}
			
			allImages.sort(new CGComparator());
		}
		
		getImageGrid().update(allImages);
		update(selectedValue);
		update(true);
	}
	
	public void update(CyCustomGraphics selectedValue) {
		getImageGrid().setSelectedValue(selectedValue, getImageGrid().isShowing());
	}
	
	public void setSelectedImage(CyCustomGraphics image) {
		if (!Objects.equals(selectedImage, image)) {
			var oldValue = selectedImage;
			selectedImage = image;
			getImageGrid().setSelectedValue(image, getImageGrid().isShowing());
			
			if (!selectionIsAdjusting)
				firePropertyChange("selectedImage", oldValue, selectedImage);
		}
	}
	
	/**
	 * It this is on edit mode (see {@link #isEditMode()}),
	 * you probably want to use {@link #getSelectedImageList()} instead.
	 */
	public CyCustomGraphics getSelectedImage() {
		return selectedImage;
	}
	
	/**
	 * Returns the selected images. Use this one instead of {@link #getSelectedImage()} when on edit mode.
	 */
	public List<CyCustomGraphics> getSelectedImageList() {
		return getImageGrid().getSelectedValuesList();
	}
	
	public int getSelectionCount() {
		return getImageGrid().getSelectionModel().getSelectedItemsCount();
	}
	
	public boolean isSelected(CyCustomGraphics image) {
		var index = getImageGrid().indexOf(image);
		return getImageGrid().getSelectionModel().isSelectedIndex(index);
	}
	
	public boolean isEmpty() {
		return allImages == null || allImages.isEmpty();
	}
	
	public void dispose() {
		try {
			allImages.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void fireActionEvent() {
		if (!firingActionEvent) {
			// Set flag to ensure that an infinite loop is not created
			firingActionEvent = true;
			ActionEvent e = null;
			
			// Guaranteed to return a non-null array
			Object[] listeners = listenerList.getListenerList();
			long mostRecentEventTime = EventQueue.getMostRecentEventTime();
			int modifiers = 0;
			AWTEvent currentEvent = EventQueue.getCurrentEvent();
			
			if (currentEvent instanceof InputEvent)
				modifiers = ((InputEvent) currentEvent).getModifiersEx();
			else if (currentEvent instanceof ActionEvent)
				modifiers = ((ActionEvent) currentEvent).getModifiers();
			
			try {
				// Process the listeners last to first, notifying those that are interested in this event
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == ActionListener.class) {
						// Lazily create the event:
						if (e == null)
							e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand(),
									mostRecentEventTime, modifiers);
						((ActionListener) listeners[i + 1]).actionPerformed(e);
					}
				}
			} finally {
				firingActionEvent = false;
			}
		}
    }
	
	private void init() {
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(false);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(getEditBtn())
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(getToolBarPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(0, 0, Short.MAX_VALUE)
				)
				.addComponent(getGridScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getAddBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(Alignment.CENTER)
						.addComponent(getEditBtn())
						.addComponent(getToolBarPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(getGridScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(getAddBtn())
				.addContainerGap()
		);
	}
	
	JScrollPane getGridScrollPane() {
		if (gridScrollPane == null) {
			gridScrollPane = new JScrollPane(getImageGrid());
			gridScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			gridScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			gridScrollPane.setBackground(BG_COLOR);
			gridScrollPane.getViewport().setBackground(BG_COLOR);
			
			var debouncer = new DebounceTimer();
			
			gridScrollPane.getViewport().addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent evt) {
					debouncer.debounce(() -> {
						invokeOnEDT(() -> getImageGrid().update());
					});
				}
			});
			gridScrollPane.getViewport().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent evt) {
					if (isEditMode() && !evt.isShiftDown()) // Deselect all items
						getImageGrid().deselectAll();
				}
			});
			
			gridScrollPane.setDropTarget(new URLDropTarget());
		}
		
		return gridScrollPane;
	}
	
	private ImageGrid getImageGrid() {
		if (imageGrid == null) {
			imageGrid = new ImageGrid(allImages);
			imageGrid.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent evt) {
					if (evt.getClickCount() == 2)
						fireActionEvent();
				}
			});
			imageGrid.addListSelectionListener(evt -> {
				update();
				selectedImage = getImageGrid().getSelectedValue();
			});
		}
		
		return imageGrid;
	}
	
	public JButton getAddBtn() {
		if (addBtn == null) {
			var iconFont = serviceRegistrar.getService(IconManager.class).getIconFont(18.0f);
			var icon = new TextIcon(ICON_PLUS, iconFont, 18, 18);
			
			addBtn = new JButton("Add Images", icon);
			addBtn.addActionListener(evt -> addButtonActionPerformed(evt));
		}
		
		return addBtn;
	}
	
	JToggleButton getEditBtn() {
		if (editBtn == null) {
			var iconFont = serviceRegistrar.getService(IconManager.class).getIconFont(18.0f);
			var icon = new TextIcon(ICON_EDIT, iconFont, 18, 18);
			
			editBtn = new JToggleButton("Edit", icon, isEditMode());
			editBtn.addActionListener(evt -> setEditMode(editBtn.isSelected()));
			
			if (isAquaLAF())
				editBtn.putClientProperty("JButton.buttonType", "textured");
		}
		
		return editBtn;
	}
	
	JPanel getToolBarPanel() {
		if (toolBarPanel == null) {
			toolBarPanel = new JPanel();
			
			var layout = new GroupLayout(toolBarPanel);
			toolBarPanel.setLayout(layout);
			layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
			layout.setAutoCreateContainerGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getSelectAllBtn())
					.addComponent(getSelectNoneBtn())
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(getRemoveImagesBtn())
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER)
					.addComponent(getSelectAllBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getSelectNoneBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getRemoveImagesBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return toolBarPanel;
	}
	
	JButton getSelectAllBtn() {
		if (selectAllBtn == null) {
			selectAllBtn = createToolBarButton(ICON_CHECK_SQUARE_O + ICON_CHECK_SQUARE_O, "Select All", 14.0f);
			selectAllBtn.addActionListener(evt -> getImageGrid().selectAll());
		}
		
		return selectAllBtn;
	}
	
	JButton getSelectNoneBtn() {
		if (selectNoneBtn == null) {
			selectNoneBtn = createToolBarButton(ICON_SQUARE_O + ICON_SQUARE_O, "Select None", 14.0f);
			selectNoneBtn.addActionListener(evt -> getImageGrid().deselectAll());
		}
		
		return selectNoneBtn;
	}
	
	JButton getRemoveImagesBtn() {
		if (removeImagesBtn == null) {
			removeImagesBtn = createToolBarButton(ICON_TRASH_O, "Remove Selected Images", 18.0f);
			removeImagesBtn.addActionListener(evt -> deleteButtonActionPerformed(evt));
		}
		
		return removeImagesBtn;
	}
	
	private void addButtonActionPerformed(ActionEvent evt) {
		// Add a directory
		var chooser = new JFileChooser();
		
		var filter = new FileNameExtensionFilter(IMG_FILES_DESCRIPTION, IMG_EXTENSIONS);
		chooser.setDialogTitle("Select Image Files");
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION)
			processFiles(chooser.getSelectedFiles());
	}
	
	private void deleteButtonActionPerformed(ActionEvent evt) {
		var toBeRemoved = imageGrid.getSelectedValuesList();
		
		if (!toBeRemoved.isEmpty()) {
			var manager = serviceRegistrar.getService(CustomGraphicsManager.class);
			
			for (var obj : toBeRemoved) {
				var cg = obj;
				
				if (!manager.isUsedInCurrentSession(cg)) {
					manager.removeCustomGraphics(cg.getIdentifier());
				} else {
					JOptionPane.showMessageDialog(this,
							cg.getDisplayName() + " is used in current session and cannot remove it.",
							"Custom Graphics is in Use.", JOptionPane.ERROR_MESSAGE);
				}
			}
			
			update(manager.getAllCustomGraphics(), null);
		}
	}
	
	private void processFiles(File[] files) {
		var manager = serviceRegistrar.getService(CustomGraphicsManager.class);
		AbstractURLImageCustomGraphics<?> lastCG = null;
		
		for (var file : files) {
			BufferedImage img = null;
			String svg = null;
			
			if (file.isFile()) {
				try {
					if (file.getName().toLowerCase().endsWith(".svg"))
						svg = Files.readString(file.toPath());
					else
						img = ImageIO.read(file);
				} catch (Exception e) {
					logger.error("Could not read file: " + file.toString(), e);
					continue;
				}
			}

			try {
				var url = file.toURI().toURL();
				AbstractURLImageCustomGraphics<?> cg = null;
				
				if (svg != null)
					cg = new SVGCustomGraphics(manager.getNextAvailableID(), file.toString(), url, svg);
				else if (img != null)
					cg = new BitmapCustomGraphics(manager.getNextAvailableID(), file.toString(), url, img);

				if (cg != null) {
					manager.addCustomGraphics(cg, url);
					lastCG = cg;
				}
			} catch (Exception e) {
				logger.error("Could not create custom graphics: " + file, e);
				continue;
			}
		}
		
		if (lastCG != null)
			update(manager.getAllCustomGraphics(), lastCG);
	}
	
	private int calculateColumns(int itemWidth, int gridWidth) {
		var cols = itemWidth > 0 ? Math.floorDiv(gridWidth, itemWidth) : 1;
		cols = Math.max(cols, 1);
		
		return Math.max(cols, maxColumns);
	}
	
	private int calculateRows(int total, int cols) {
		return (int) Math.round(Math.ceil((float)total / (float)cols));
	}
	
	private JButton createToolBarButton(String iconText, String tooltipText, float size) {
		var btn = new JButton(iconText);
		btn.setToolTipText(tooltipText);
		
		styleToolBarButton(btn, serviceRegistrar.getService(IconManager.class).getIconFont(size), 2, 0);
		
		return btn;
	}
	
	private JToggleButton createToolBarToggleButton(String iconText, String tooltipText, float size) {
		var btn = new SimpleToolBarToggleButton(iconText);
		btn.setToolTipText(tooltipText);
		
		styleToolBarButton(btn, serviceRegistrar.getService(IconManager.class).getIconFont(size), 2, 0);
		
		return btn;
	}
	
	public void setDirty(CyCustomGraphics image) {
		getImageGrid().setDirty(image);
	}
	
	void update() {
		update(false);
	}
	
	void update(boolean updateThumbnails) {
		setEnabled(!isEmpty());
		getEditBtn().setSelected(isEditMode());
		getToolBarPanel().setVisible(isEditMode());
		
		if (updateThumbnails)
			getImageGrid().updateImagePanels();
		
		getImageGrid().getSelectionModel().setSelectionMode(
				isEditMode() ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
		
		updateToolBarButtons();
	}
	
	private void updateToolBarButtons() {
		if (isEnabled() && isEditMode()) {
			var selValues = getImageGrid().getSelectedValuesList();
			int selCount = selValues.size();
			int total =  getImageGrid().getModel().getSize();
			
			getSelectAllBtn().setEnabled(selCount < total);
			getSelectNoneBtn().setEnabled(selCount > 0);
			getRemoveImagesBtn().setEnabled(selCount > 1 || selCount == 1);
		}
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	/**
	 * Some methods were copied from the JList implementation.
	 */
	class ImageGrid extends JComponent implements Scrollable {
		
		private final LinkedHashMap<CyCustomGraphics, ImagePanel> vsPanelMap = new LinkedHashMap<>();
		
		private ListModel<CyCustomGraphics> dataModel;
		private ListSelectionModel selectionModel;
		private ListSelectionListener selectionListener;
		
		ImageGrid(Collection<CyCustomGraphics> data) {
			this.dataModel = new ImageGridModel(data);
			this.selectionModel = createSelectionModel();
			this.setOpaque(true);
			this.setBackground(BG_COLOR);
			
			setKeyBindings();
			
			addListSelectionListener(evt -> {
				revalidate();
				repaint();
			});
			
			update();
		}
		
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getModel().getSize() == 0 ? getMinimumSize() : getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 10;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return cols > 0;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return getModel().getSize() == 0;
		}
		
		@Override
		public void repaint() {
			for (var item : vsPanelMap.values())
				item.repaint();
			
			super.repaint();
		}
		
		ImagePanel getItem(CyCustomGraphics image) {
			return vsPanelMap.get(image);
		}
		
		int indexOf(CyCustomGraphics image) {
			int i, c;
			var dm = getModel();
			
			for (i = 0, c = dm.getSize(); i < c; i++) {
				if (image.equals(dm.getElementAt(i)))
					return i;
			}
			
			return -1;
		}
		
		void setFocus(int index) {
			var dm = getModel();
			
			if (index > -1 && index < dm.getSize()) {
				var vs = dm.getElementAt(index);
				var item = getItem(vs);
				
				if (item != null)
					item.requestFocusInWindow();
			}
		}
		
		/**
	     * Returns the value for the smallest selected cell index;
	     * <i>the selected value</i> when only a single item is selected in the
	     * list. When multiple items are selected, it is simply the value for the
	     * smallest selected index. Returns {@code null} if there is no selection.
	     * <p>
	     * This is a convenience method that simply returns the model value for
	     * {@code getMinSelectionIndex}.
	     *
	     * @return the first selected value
	     */
	    CyCustomGraphics getSelectedValue() {
			int i = getMinSelectionIndex();
			
			return (i == -1) || (i >= getModel().getSize()) ? null : getModel().getElementAt(i);
	    }
		
		void setSelectedValue(CyCustomGraphics image, boolean shouldScroll) {
			if (image == null) {
				deselectAll();
			} else if (!image.equals(getSelectedValue())) {
				int i = indexOf(image);
				setSelectedIndex(i);

				if (i >= 0 && shouldScroll)
					ensureIndexIsVisible(i);
			}
		}
		
		List<CyCustomGraphics> getSelectedValuesList() {
			var dm = getModel();
			var sm = getSelectionModel();
			int[] selectedIndices = sm.getSelectedIndices();
			var selectedItems = new ArrayList<CyCustomGraphics>();

			if (selectedIndices.length > 0) {
				int size = dm.getSize();
				
				if (selectedIndices[0] < size) {
					for (int i : selectedIndices) {
						if (i >= size)
							break;
						
						selectedItems.add(dm.getElementAt(i));
					}
				}
			}
			
			return selectedItems;
		}

		void selectAll() {
			if (getModel().getSize() >= 0) {
				getSelectionModel().setSelectionInterval(0, getModel().getSize() - 1);
				selectionHead = -1;
				selectionTail = -1;
			}
		}
		
		void deselectAll() {
			getSelectionModel().clearSelection();
			selectionHead = -1;
			selectionTail = -1;
		}
		
		/**
	     * Selects a single cell. Does nothing if the given index is greater
	     * than or equal to the model size. This is a convenience method that uses
	     * {@code setSelectionInterval} on the selection model. Refer to the
	     * documentation for the selection model class being used for details on
	     * how values less than {@code 0} are handled.
	     *
	     * @param index the index of the cell to select
	     */
		void setSelectedIndex(int index) {
			if (index >= getModel().getSize())
				return;
			
			getSelectionModel().setSelectionInterval(index, index);
		}
		
		int getMinSelectionIndex() {
	        return getSelectionModel().getMinSelectionIndex();
	    }
		
		int getMaxSelectionIndex() {
	        return getSelectionModel().getMaxSelectionIndex();
	    }
		
		boolean isSelectedIndex(int index) {
	        return getSelectionModel().isSelectedIndex(index);
	    }
		
		boolean isSelectionEmpty() {
	        return getSelectionModel().isSelectionEmpty();
	    }
		
		ListSelectionModel getSelectionModel() {
	        return selectionModel;
	    }
		
		void setSelectionInterval(int anchor, int lead) {
			getSelectionModel().setSelectionInterval(anchor, lead);
		}

		void addSelectionInterval(int anchor, int lead) {
			getSelectionModel().addSelectionInterval(anchor, lead);
		}

		void removeSelectionInterval(int index0, int index1) {
			getSelectionModel().removeSelectionInterval(index0, index1);
		}
		
		void setSelectedList(List<CyCustomGraphics> images) {
			var sm = getSelectionModel();
			sm.clearSelection();
			
			for (var vs : images) {
				int idx = indexOf(vs);
				
				if (idx >= 0)
					sm.addSelectionInterval(idx, idx);
			}
			
			selectionHead = -1;
			selectionTail = -1;
		}
		
		void setSelectionModel(ListSelectionModel selectionModel) {
	        if (selectionModel == null)
	            throw new IllegalArgumentException("selectionModel must be non null");

	        // Remove the forwarding ListSelectionListener from the old
	        // selectionModel, and add it to the new one, if necessary.
	        if (selectionListener != null) {
	            this.selectionModel.removeListSelectionListener(selectionListener);
	            selectionModel.addListSelectionListener(selectionListener);
	        }

	        var oldValue = this.selectionModel;
	        this.selectionModel = selectionModel;
	        firePropertyChange("selectionModel", oldValue, selectionModel);
	    }
		
		ListModel<CyCustomGraphics> getModel() {
			return dataModel;
		}

		void setModel(ListModel<CyCustomGraphics> model) {
			if (model == null)
				throw new IllegalArgumentException("model must be non null");

			var oldValue = dataModel;
			dataModel = model;
			firePropertyChange("model", oldValue, dataModel);
			
			selectionIsAdjusting = true;
			
			try {
				deselectAll();
				update();
			} finally {
				selectionIsAdjusting = false;
			}
		}
		
		/**
	     * Scrolls the list within an enclosing viewport to make the specified
	     * cell completely visible. This calls {@code scrollRectToVisible} with
	     * the bounds of the specified cell. For this method to work, the
	     * {@code JList} must be within a <code>JViewport</code>.
	     * <p>
	     * If the given index is outside the list's range of cells, this method
	     * results in nothing.
	     *
	     * @param index  the index of the cell to make visible
	     */
	    void ensureIndexIsVisible(int index) {
	        var cellBounds = getCellBounds(index, index);
	        
	        if (cellBounds != null)
	            scrollRectToVisible(cellBounds);
	    }
	    
	    /**
	     * Returns the bounding rectangle, in the list's coordinate system,
	     * for the range of cells specified by the two indices.
	     * These indices can be supplied in any order.
	     * <p>
	     * If the smaller index is outside the list's range of cells, this method
	     * returns {@code null}. If the smaller index is valid, but the larger
	     * index is outside the list's range, the bounds of just the first index
	     * is returned. Otherwise, the bounds of the valid range is returned.
	     * <p>
	     * This is a cover method that delegates to the method of the same name
	     * in the list's {@code ListUI}. It returns {@code null} if the list has
	     * no {@code ListUI}.
	     *
	     * @param index0 the first index in the range
	     * @param index1 the second index in the range
	     * @return the bounding rectangle for the range of cells, or {@code null}
	     */
	    Rectangle getCellBounds(int index0, int index1) {
	        // TODO
	        return null;
	    }
		
		void addListSelectionListener(ListSelectionListener listener) {
			if (selectionListener == null) {
				selectionListener = new ListSelectionHandler();
				getSelectionModel().addListSelectionListener(selectionListener);
			}

			listenerList.add(ListSelectionListener.class, listener);
		}

		void removeListSelectionListener(ListSelectionListener listener) {
			listenerList.remove(ListSelectionListener.class, listener);
		}

		ListSelectionListener[] getListSelectionListeners() {
			return listenerList.getListeners(ListSelectionListener.class);
		}
		
		private ListSelectionModel createSelectionModel() {
	        return new DefaultListSelectionModel();
	    }
		
		protected void fireSelectionValueChanged(int firstIndex, int lastIndex, boolean isAdjusting) {
			Object[] listeners = listenerList.getListenerList();
			ListSelectionEvent e = null;

			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == ListSelectionListener.class) {
					if (e == null)
						e = new ListSelectionEvent(this, firstIndex, lastIndex, isAdjusting);
					
					((ListSelectionListener) listeners[i + 1]).valueChanged(e);
				}
			}
		}
		
		void setDirty(CyCustomGraphics image) {
			if (image != null) {
				var item = getItem(image);
				
				if (item != null)
					item.setDirty();
			}
		}
		
		void updateImagePanels() {
			for (var item : vsPanelMap.values()) {
				if (item.isDirty())
					item.update();
			}
		}
		
		void update() {
			removeAll();
			
			var dm = getModel();
			
			if (dm.getSize() > 0) {
				var size = this.getParent() != null ? this.getParent().getSize() : null;
				
				if (size != null) {
					var itemWidth = IMAGE_WIDTH + 2 * ITEM_BORDER_WIDTH + 2 * ITEM_MARGIN + 2 * ITEM_PAD;
					var width = size != null ? size.width : itemWidth;
					cols = width <= 0 ? minColumns : calculateColumns(itemWidth, width);
				} else {
					cols = minColumns;
				}
				
				var rows = calculateRows(dm.getSize(), cols);
				setLayout(new GridLayout(rows, cols));
				
				for (int i = 0; i < dm.getSize(); i++) {
					var vs = dm.getElementAt(i);
					var itemPnl = getItem(vs);
					
					if (itemPnl == null)
						itemPnl = createItem(vs);
					
					add(itemPnl);
				}
				
				var diff = (cols * rows) - dm.getSize();
					
				for (int i = 0; i < diff; i++) {
					var fillPnl = new JPanel();
					fillPnl.setBackground(BG_COLOR);
					fillPnl.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent evt) {
							if (isEditMode() && !evt.isShiftDown()) // Deselect all items
								deselectAll();
						}
					});
					add(fillPnl);
				}
			}
			
			revalidate();
			repaint();
		}
		
		void update(Collection<CyCustomGraphics> data) {
			vsPanelMap.clear();
			
			// Save current selection
			var selectedImages = isEditMode() ? getSelectedImageList() : new ArrayList<CyCustomGraphics>();
			
			if (selectedImage != null && !isEditMode())
				selectedImages.add(selectedImage);
			
			selectionIsAdjusting = true;
			
			try {
				setModel(new ImageGridModel(allImages));
				setSelectedList(selectedImages); // Restore previous selection
			} finally {
				selectionIsAdjusting = false;
			}
		}
		
		private ImagePanel createItem(CyCustomGraphics image) {
			var item = new ImagePanel(image);
			vsPanelMap.put(image, item);
			
			// Events
			item.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent evt) {
					if (isEnabled() && !editingName)
						item.requestFocusInWindow();
				}
				@Override
				public void mousePressed(MouseEvent evt) {
					if (isEnabled())
						onMousePressedItem(evt, item);
				}
			});
			item.getNameTextField().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent evt) {
					if (isEnabled())
						onMousePressedItem(evt, item);
				}
				@Override
				public void mouseClicked(MouseEvent evt) {
					if (isEnabled()) {
						if (isEditMode() && evt.getClickCount() == 2)
							editNameStart(item);
					}
				}
			});
			
			return item;
		}
		
		private void onMousePressedItem(MouseEvent evt, ImagePanel item) {
			item.requestFocusInWindow();
			
			if (isEditMode()) {
				int index = indexOf(item.image);
				boolean selected = isSelected(item.image);
				var sm = getSelectionModel();
				
				if (evt.isPopupTrigger()) {
					// RIGHT-CLICK...
					selectionHead = index;
				} else {
					// LEFT-CLICK...
					var isMac = LookAndFeelUtil.isMac();
					
					if ((isMac && evt.isMetaDown()) || (!isMac && evt.isControlDown())) {
						// CMD or CTRL key pressed...
						toggleSelection(item);
						// Find new selection range head
						selectionHead = selected ? index : findNextSelectionHead(selectionHead);
					} else if (evt.isShiftDown()) {
						// SHIFT key pressed...
						if (selectionHead >= 0 && selectionHead != index && sm.isSelectedIndex(selectionHead)) {
							// First deselect previous range, if there is a tail
							if (selectionTail >= 0)
								changeRangeSelection(selectionHead, selectionTail, false);
							// Now select the new range
							changeRangeSelection(selectionHead, (selectionTail = index), true);
						} else if (!selected) {
							addSelectionInterval(index, index);
						}
					} else {
						setSelectedValue(item.getImage(), false);
					}
					
					if (sm.getSelectedItemsCount() == 1)
						selectionHead = index;
				}
			} else {
				setSelectedValue(item.getImage(), false);
			}
			
			item.repaint();
		}
		
		private void toggleSelection(ImagePanel item) {
			var index = indexOf(item.image);
			
			if (isSelectedIndex(index))
				removeSelectionInterval(index, index);
			else
				addSelectionInterval(index, index);
		}
		
		private void changeRangeSelection(int index0, int index1, boolean select) {
			if (select)
				addSelectionInterval(index0, index1);
			else
				removeSelectionInterval(index0, index1);
		}
		
		private int findNextSelectionHead(int fromIndex) {
			int head = -1;
			
			if (fromIndex >= 0) {
				var dm = getModel();
				int total = dm.getSize();
				
				// Try with the tail subset first (go down)...
				for (int i = fromIndex; i < total; i++) {
					var nextItem = dm.getElementAt(i);
					
					if (isSelected(nextItem)) {
						head = i;
						break;
					}
				}
				
				if (head == -1) {
					// Try with the head subset  (go up)...
					for (int i = fromIndex; i <= 0; i--) {
						var nextItem = dm.getElementAt(i);
						
						if (isSelected(nextItem)) {
							head = i;
							break;
						}
					}
				}
			}
			
			return head;
		}

		void editNameStart(ImagePanel item) {
			editingName = true;
			var oldValue = item.getNameTextField().getText().trim();
			
			item.getNameTextField().setFocusable(true);
			item.getNameTextField().setEditable(true);
			item.getNameTextField().requestFocusInWindow();
			item.getNameTextField().selectAll();
			
			item.getNameTextField().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					editNameCommit(item, oldValue);
					item.getNameTextField().removeActionListener(this);
				}
			});
			item.getNameTextField().addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent evt) {
					editNameCommit(item, oldValue);
					item.getNameTextField().removeFocusListener(this);
				}
			});
			item.getNameTextField().addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
						editNameCancel(item, oldValue);
						item.getNameTextField().removeKeyListener(this);
						evt.consume(); // Prevent any dialog that contains this component from closing
					}
				}
			});
		}
		
		void editNameCommit(ImagePanel item, String oldValue) {
			var newValue = item.getNameTextField().getText().trim();
			
			if (newValue.isEmpty() || newValue.equals(oldValue)) {
				editNameCancel(item, oldValue);
				return;
			}
			
			editNameEnd(item);
			item.getImage().setDisplayName(newValue);
		}
		
		void editNameCancel(ImagePanel item, String oldValue) {
			item.getNameTextField().setText(oldValue);
			editNameEnd(item);
		}
		
		void editNameEnd(ImagePanel item) {
			item.getNameTextField().setEditable(false);
			item.requestFocusInWindow();
			item.getNameTextField().setFocusable(false);
			editingName = false;
		}

		private void setKeyBindings() {
			var actionMap = this.getActionMap();
			var inputMap = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			int ctrl = LookAndFeelUtil.isMac() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;

			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), KeyAction.VK_LEFT);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), KeyAction.VK_RIGHT);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KeyAction.VK_UP);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KeyAction.VK_DOWN);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), KeyAction.VK_ENTER);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), KeyAction.VK_SPACE);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ctrl), KeyAction.VK_CTRL_A);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ctrl + InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_CTRL_SHIFT_A);
			
			actionMap.put(KeyAction.VK_LEFT, new KeyAction(KeyAction.VK_LEFT));
			actionMap.put(KeyAction.VK_RIGHT, new KeyAction(KeyAction.VK_RIGHT));
			actionMap.put(KeyAction.VK_UP, new KeyAction(KeyAction.VK_UP));
			actionMap.put(KeyAction.VK_DOWN, new KeyAction(KeyAction.VK_DOWN));
			actionMap.put(KeyAction.VK_ENTER, new KeyAction(KeyAction.VK_ENTER));
			actionMap.put(KeyAction.VK_SPACE, new KeyAction(KeyAction.VK_SPACE));
			actionMap.put(KeyAction.VK_CTRL_A, new KeyAction(KeyAction.VK_CTRL_A));
			actionMap.put(KeyAction.VK_CTRL_SHIFT_A, new KeyAction(KeyAction.VK_CTRL_SHIFT_A));
		}
		
		private class KeyAction extends AbstractAction {

			final static String VK_LEFT = "VK_LEFT";
			final static String VK_RIGHT = "VK_RIGHT";
			final static String VK_UP = "VK_UP";
			final static String VK_DOWN = "VK_DOWN";
			final static String VK_ENTER = "VK_ENTER";
			final static String VK_SPACE = "VK_SPACE";
			final static String VK_CTRL_A = "VK_CTRL_A";
			final static String VK_CTRL_SHIFT_A = "VK_CTRL_SHIFT_A";
			
			KeyAction(final String actionCommand) {
				putValue(ACTION_COMMAND_KEY, actionCommand);
			}

			@Override
			public void actionPerformed(ActionEvent evt) {
				var cmd = evt.getActionCommand();
				var focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
				var focusedItem = focusOwner instanceof ImagePanel ? (ImagePanel) focusOwner : null;
				var dm = ImageGrid.this.getModel();
				
				if (cmd.equals(VK_ENTER) || cmd.equals(VK_SPACE)) {
					if (focusedItem != null)
						setSelectedValue(focusedItem.getImage(), false);
				} else if (dm.getSize() > 0) {
					var vs = focusedItem != null ? focusedItem.getImage() : dm.getElementAt(0);
					int size = dm.getSize();
					int idx = indexOf(vs);
					int newIdx = idx;
					
					if (cmd.equals(VK_RIGHT)) {
						newIdx = idx + 1;
					} else if (cmd.equals(VK_LEFT)) {
						newIdx = idx - 1;
					} else if (cmd.equals(VK_UP)) {
						newIdx = idx - cols < 0 ? idx : idx - cols;
					} else if (cmd.equals(VK_DOWN)) {
						final boolean sameRow = Math.ceil(size / (double) cols) == Math.ceil((idx + 1) / (double) cols);
						newIdx = sameRow ? idx : Math.min(size - 1, idx + cols);
					} else if (cmd.equals(VK_CTRL_A)) {
						if (isEditMode())
							selectAll();
					} else if (cmd.equals(VK_CTRL_SHIFT_A)) {
						if (isEditMode())
							deselectAll();
					}
					
					if (newIdx != idx)
						setFocus(newIdx);
				}
			}
		}
		
		private class ListSelectionHandler implements ListSelectionListener, Serializable {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				fireSelectionValueChanged(e.getFirstIndex(), e.getLastIndex(), e.getValueIsAdjusting());
			}
		}
	}
	
	class ImageGridModel extends AbstractListModel<CyCustomGraphics>  {

		private final List<CyCustomGraphics> data = new ArrayList<>();

		public ImageGridModel(Collection<CyCustomGraphics> data) {
			this.data.addAll(data);
		}
		
		@Override
		public int getSize() {
			return data.size();
		}

		@Override
		public CyCustomGraphics getElementAt(int index) {
			return data.get(index);
		}
	}
	
	class ImagePanel extends JComponent {
		
		private JLabel imageLabel;
		private JTextField nameTextField;
		
		private final CyCustomGraphics image;
		private boolean dirty = true;

		ImagePanel(CyCustomGraphics image) {
			this.image = image;
			init();
			
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent evt) {
					ImagePanel.this.requestFocusInWindow();
				}
			});
			this.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					ImagePanel.this.repaint();
				}
				@Override
				public void focusLost(FocusEvent e) {
					ImagePanel.this.repaint();
				}
			});
		}
		
		CyCustomGraphics getImage() {
			return image;
		}
		
		private void init() {
			setBackground(BG_COLOR);
			setFocusable(true);
			setDoubleBuffered(true);
			
			var gap = ITEM_MARGIN + ITEM_PAD;
			setBorder(BorderFactory.createEmptyBorder(gap,  gap,  gap,  gap));
			
			var layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateGaps(false);
			layout.setAutoCreateContainerGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
					.addComponent(getImageLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getNameTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getImageLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getNameTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		JLabel getImageLabel() {
			if (imageLabel == null) {
				imageLabel = new JLabel();
				imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
				imageLabel.setOpaque(true);
				
				int bw = 1;
				imageLabel.setBorder(BorderFactory.createMatteBorder(0, 0, bw, 0, BORDER_COLOR));
				imageLabel.setMinimumSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT + bw));
			}
			
			return imageLabel;
		}
		
		JTextField getNameTextField() {
			if (nameTextField == null) {
				nameTextField = new JTextField(image.getDisplayName()) {
					@Override
					public String getToolTipText(MouseEvent evt) {
					    return "<html><p image='text-align: center;'>" +
								"<b>" + image.getDisplayName() + "</b>" +
								(isEditMode() ? "<br>(double-click to rename...)" : "") +
								"</p></html>";
					}
				};
				nameTextField.setHorizontalAlignment(SwingConstants.CENTER);
				nameTextField.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
				nameTextField.setBackground(BG_COLOR);
				nameTextField.setEditable(false);
				nameTextField.setFocusable(false);
				makeSmall(nameTextField);
			}
			
			return nameTextField;
		}
		
		boolean isDirty() {
			return dirty;
		}
		
		void setDirty() {
			this.dirty = true;
		}
		
		void update() {
			setToolTipText(image.getDisplayName());
			
			// Image
			var icon = VisualPropertyIconFactory.createIcon(image, IMAGE_WIDTH, IMAGE_HEIGHT);
			getImageLabel().setIcon(icon);
			
			// Name
			getNameTextField().setText(ViewUtil.getShortName(image.getDisplayName()));
			getNameTextField().setToolTipText(image.getDisplayName()); // Otherwise our getToolTipText() won't be called!
			
			dirty = false;
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			
			var g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			int w = this.getWidth();
			int h = this.getHeight();
			int arc = 10;

			boolean selected = isSelected(image);
			boolean focusOwner = this.isFocusOwner();
			
			// Add a colored border if it is the current image
			if (selected) {
				g2d.setColor(SEL_BG_COLOR);
				g2d.setStroke(new BasicStroke(2 * ITEM_BORDER_WIDTH));
				g2d.drawRoundRect(ITEM_MARGIN, ITEM_MARGIN, w - 2 * ITEM_MARGIN, h - 2 * ITEM_MARGIN, arc, arc);
			} else {
				g2d.setColor(BORDER_COLOR);
				g2d.setStroke(new BasicStroke(ITEM_BORDER_WIDTH));
				g2d.drawRoundRect(ITEM_MARGIN, ITEM_MARGIN, w - 2 * ITEM_MARGIN, h - 2 * ITEM_MARGIN, arc, arc);
			}
			
			// Add a colored border and transparent overlay on top if it currently has focus
			if (focusOwner) {
				g2d.setColor(FOCUS_OVERLAY_COLOR);
				g2d.fillRect(ITEM_MARGIN, ITEM_MARGIN, w - 2 * ITEM_MARGIN, h - 2 * ITEM_MARGIN);
				
				g2d.setColor(FOCUS_BORDER_COLOR);
				g2d.setStroke(new BasicStroke(ITEM_BORDER_WIDTH));
				g2d.drawRoundRect(ITEM_MARGIN, ITEM_MARGIN, w - 2 * ITEM_MARGIN, h - 2 * ITEM_MARGIN, arc, arc);
			}

			g2d.dispose();
		}
		
		@Override
		public String toString() {
			return image.getDisplayName();
		}
	}
	
	private class URLDropTarget extends DropTarget {

		private Border originalBorder;
		private final Border dropBorder = BorderFactory.createLineBorder(UIManager.getColor("Focus.color"), 2);
		
		private DataFlavor urlFlavor;
		
		URLDropTarget() {
			try {
				urlFlavor = new DataFlavor("application/x-java-url; class=java.net.URL");
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			}
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void drop(DropTargetDropEvent dtde) {
			try {
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				var trans = dtde.getTransferable();
				boolean gotData = false;
				
				AbstractURLImageCustomGraphics<?> lastCG = null;
				
				try {
					if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						var fileList = (List<File>) trans.getTransferData(DataFlavor.javaFileListFlavor);
	
						for (var file : fileList)
							lastCG = addCustomGraphics(file.toURI().toURL().toString());
						
						gotData = true;
					} else if (trans.isDataFlavorSupported(urlFlavor)) {
						var url = (URL) trans.getTransferData(urlFlavor);
						// Add image
						lastCG = addCustomGraphics(url.toString());
						gotData = true;
					} else if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
						var s = (String) trans.getTransferData(DataFlavor.stringFlavor);
	
						var url = new URL(s);
						lastCG = addCustomGraphics(url.toString());
						gotData = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					dtde.dropComplete(gotData);
				}
				
				// Select and scroll to the last added image
				if (lastCG != null) {
					var manager = serviceRegistrar.getService(CustomGraphicsManager.class);
					update(manager.getAllCustomGraphics(), lastCG);
					getImageGrid().setSelectedValue(lastCG, true);
				}
			} catch (Exception e) {
				logger.error("Error drag-and-dropping image(s): ", e);
			} finally {
				resetBorder();
			}
		}
		
		@Override
		public synchronized void dragEnter(DropTargetDragEvent dtde) {
			super.dragEnter(dtde);

			var c = getComponent();

			if (c instanceof JComponent) {
				try {
					originalBorder = ((JComponent) c).getBorder();
					((JComponent) c).setBorder(dropBorder);
				} catch (Exception e) {
					// Just ignore, some components do not support setBorder()...
				}
			}
		}

		@Override
		public synchronized void dragExit(DropTargetEvent dte) {
			super.dragExit(dte);
			resetBorder();
		}

		private void resetBorder() {
			var c = getComponent();
			
			if (c instanceof JComponent) {
				try {
					((JComponent) c).setBorder(originalBorder);
				} catch (Exception e) {
					// Just ignore...
				}
			}
		}
		
		private AbstractURLImageCustomGraphics<?> addCustomGraphics(String urlStr) {
			var manager = serviceRegistrar.getService(CustomGraphicsManager.class);
			
			try {
				var url = new URL(urlStr);
				var id = manager.getNextAvailableID();
				var cg = urlStr.toLowerCase().endsWith(".svg")
						? new SVGCustomGraphics(id, urlStr, url)
						: new BitmapCustomGraphics(id, urlStr, url);
				
				if (cg != null)
					manager.addCustomGraphics(cg, url);
				
				return cg;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}
	}
}
