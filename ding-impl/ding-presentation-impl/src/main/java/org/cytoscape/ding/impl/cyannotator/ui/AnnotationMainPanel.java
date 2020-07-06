package org.cytoscape.ding.impl.cyannotator.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.util.swing.LookAndFeelUtil.equalizeSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.AnnotationNode;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree.Shift;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.create.AbstractDingAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.dialogs.AbstractAnnotationEditor;
import org.cytoscape.ding.internal.util.IconUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;


/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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
public class AnnotationMainPanel extends JPanel implements CytoPanelComponent2 {

	public static final CytoPanelName CYTOPANEL_NAME = CytoPanelName.WEST;
	
	private static final String TITLE = "Annotation";
	private static final String ID = "org.cytoscape.Annotation";
	
	private static final Color TREE_BG_COLOR = UIManager.getColor("Table.background");
	private static final Color TREE_FG_COLOR = UIManager.getColor("Table.foreground");
	private static final Color TREE_SEL_BG_COLOR = UIManager.getColor("Table.selectionBackground");
	private static final Color TREE_SEL_FG_COLOR = UIManager.getColor("Table.selectionForeground");
	
	private JPanel toolBarPanel;
	private JTabbedPane contentTabbedPane;
	private JPanel layersPanel;
	private JPanel editPanel;
	private JLabel infoLabel;
	private JLabel selectionLabel;
	private JButton groupAnnotationsButton;
	private JButton ungroupAnnotationsButton;
	private JButton removeAnnotationsButton;
	private JButton pullToForegroundButton;
	private JButton pushToBackgroundButton;
	private LayerPanel foregroundLayerPanel;
	private LayerPanel backgroundLayerPanel;
	private JButton selectAllButton;
	private JButton selectNoneButton;
	private final Map<Class<? extends Annotation>, AnnotationToggleButton> buttonMap = new LinkedHashMap<>();
	private final Map<Class<? extends Annotation>, Icon> iconMap = new LinkedHashMap<>();
	private final ButtonGroup buttonGroup;
	
	private SequentialGroup btnHGroup;
	private ParallelGroup btnVGroup;
	
	/** Default icon for Annotations that provide no icon */
	private Icon defIcon;
	/** GroupAnnotation icon when collapsed */
	private Icon closedAnnotationIcon;
	/** GroupAnnotation icon when expanded */
	private Icon openAnnotationIcon;
	
	/** Tab icon */
	private TextIcon icon;
	
	private DRenderingEngine re;
	
	private boolean createMode;
	private Annotation editingAnnotation;
	
	private final CyServiceRegistrar serviceRegistrar;

	public AnnotationMainPanel(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		// When a selected button is clicked again, we want it to be be unselected
		buttonGroup = new ButtonGroup() {
			private boolean isAdjusting;
			private ButtonModel prevModel;
			
			@Override
			public void setSelected(ButtonModel m, boolean b) {
				if (isAdjusting)
					return;
				
				if (m != null && m.equals(prevModel)) {
					isAdjusting = true;
					clearSelection();
					isAdjusting = false;
				} else {
					super.setSelected(m, b);
				}
				
				prevModel = getSelection();
				updateInfoLabel();
			}
		};
		
		init();
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CYTOPANEL_NAME;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}
	
	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public Icon getIcon() {
		if (icon == null) {
			var font = serviceRegistrar.getService(IconManager.class).getIconFont(IconUtil.CY_FONT_NAME, 16f);
			icon = new TextIcon(IconUtil.ICON_ANNOTATION_BOUNDED_TEXT_2, font, 16, 16);
		}
		
		return icon;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		if (!enabled)
			clearAnnotationButtonSelection();
		
		buttonMap.values().forEach(btn -> btn.setEnabled(enabled));
		updateGroupUngroupButton();
		updateRemoveAnnotationsButton();
		updateSelectionButtons();
		updateMoveToCanvasButtons();
		
		getBackgroundLayerPanel().setEnabled(enabled);
		getForegroundLayerPanel().setEnabled(enabled);
	}
	
	boolean isCreateMode() {
		return createMode;
	}
	
	void setCreateMode(boolean createMode) {
		if (this.createMode != createMode) {
			this.createMode = createMode;
			
			if (createMode)
				editingAnnotation = null;
			
			updateEditPanel();
		}
	}
	
	boolean isEditMode() {
		return !createMode && editingAnnotation != null;
	}
	
	Annotation getEditingAnnotation() {
		return editingAnnotation;
	}
	
	void setEditingAnnotation(Annotation a) {
		if (a != null)
			setCreateMode(false);
		
		if (!Objects.equals(a, editingAnnotation)) {
			editingAnnotation = a;
			updateEditPanel();
		}
	}
	
	/**
	 * Calls {@link #setEditingAnnotation(Annotation)} and shows the edit panel.
	 */
	void editAnnotation(Annotation a) {
		if (a != null) {
			setEditingAnnotation(a);
			getContentTabbedPane().setSelectedComponent(getEditPanel());
		}
	}
	
	/**
	 * Adds a buttons that creates annotations through the passed factory and also
	 * adds an corresponding editor panel if one is returned by {@link AbstractDingAnnotationFactory#createEditor()}.
	 */
	JToggleButton addAnnotationButton(AnnotationFactory<? extends Annotation> f) {
		var btn = new AnnotationToggleButton(f);
		btn.setFocusable(false);
		btn.setFocusPainted(false);
		
		buttonGroup.add(btn);
		buttonMap.put(f.getType(), btn);
		iconMap.put(f.getType(), f.getIcon());
		
		btnHGroup.addComponent(btn, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		btnVGroup.addComponent(btn, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		
		if (isAquaLAF())
			btn.putClientProperty("JButton.buttonType", "gradient");
		
		return btn;
	}
	
	void removeAnnotationButton(AnnotationFactory<? extends Annotation> f) {
		var btn = buttonMap.remove(f.getType());
		iconMap.remove(f.getType());
		
		if (btn != null)
			getToolBarPanel().remove(btn);
		
		if (f instanceof AbstractDingAnnotationFactory) {
			var comp = ((AbstractDingAnnotationFactory<?>) f).getEditor();
			
			if (comp != null) {
				getEditPanel().remove(comp);
				updateEditPanel();
			}
		}
	}
	
	Set<Annotation> getAllAnnotations() {
		var set = new HashSet<Annotation>();
		set.addAll(((AnnotationTreeModel) getBackgroundTree().getModel()).getData());
		set.addAll(((AnnotationTreeModel) getForegroundTree().getModel()).getData());
		
		return set;
	}
	
	Collection<Annotation> getSelectedAnnotations() {
		var set = new HashSet<Annotation>();
		set.addAll(getSelectedAnnotations(getBackgroundTree()));
		set.addAll(getSelectedAnnotations(getForegroundTree()));
		
		return set;
	}
	
	List<Annotation> getSelectedAnnotations(JTree tree) {
		return getSelectedAnnotations(tree, Annotation.class);
	}
	
	<T extends Annotation> Collection<T> getSelectedAnnotations(Class<T> type) {
		var set = new LinkedHashSet<T>();
		set.addAll(getSelectedAnnotations(getBackgroundTree(), type));
		set.addAll(getSelectedAnnotations(getForegroundTree(), type));
		
		return set;
	}
	
	@SuppressWarnings("unchecked")
	<T extends Annotation> List<T> getSelectedAnnotations(JTree tree, Class<T> type) {
		var set = new ArrayList<T>();
		var treePaths = tree.getSelectionModel().getSelectionPaths();
		
		for (var path : treePaths) {
			var node = (AnnotationNode) path.getLastPathComponent();
			var obj = node.getAnnotation();
			
			if (type.isAssignableFrom(obj.getClass()))
				set.add((T) obj);
		}
		
		return set;
	}
	
	DRenderingEngine getRenderingEngine() {
		return re;
	}
	
	int getAnnotationCount() {
		return getBackgroundTree().getRowCount() + getForegroundTree().getRowCount();
	}
	
	int getSelectedAnnotationCount() {
		return getBackgroundTree().getSelectionCount() + getForegroundTree().getSelectionCount();
	}
	
	int getSelectedAnnotationCount(Class<? extends Annotation> type) {
		return getSelectedAnnotationCount(getBackgroundTree(), type)
				+ getSelectedAnnotationCount(getForegroundTree(), type);
	}
	
	int getSelectedAnnotationCount(JTree tree, Class<? extends Annotation> type) {
		int count = 0;
		var treePaths = tree.getSelectionModel().getSelectionPaths();
		
		for (var path : treePaths) {
			var node = (AnnotationNode) path.getLastPathComponent();
			var obj = node.getAnnotation();
			
			if (type.isAssignableFrom(obj.getClass()))
				count++;
		}
		
		return count;
	}
	
	void clearAnnotationButtonSelection() {
		buttonGroup.clearSelection();
	}
	
	void setSelected(Annotation a, boolean selected) {
System.out.println("AnnotationMainPanel.setSelected(): " + a + " -- " + selected);
		if (re != null && getAnnotationCount() > 0) {
			// group annotations can be on both canvases at the same time
			setSelected(a, selected, getForegroundTree());
			setSelected(a, selected, getBackgroundTree());
		}
		
		maybeUpdateEditingAnnotation(a);
	}
	
	private void setSelected(Annotation a, boolean selected, JTree tree) {
		var model = (AnnotationTreeModel) tree.getModel();
		var path = model.pathTo(a);
		
		if (path == null)
			return;

		if (selected) {
			tree.addSelectionPath(path);
			tree.scrollPathToVisible(path);
		} else {
			tree.removeSelectionPath(path);
		}
		
		a.setSelected(selected);
	}
	
	void update(DRenderingEngine re) {
		this.re = re;
		
		List<DingAnnotation> annotations = re != null ? re.getCyAnnotator().getAnnotations() : Collections.emptyList();
		
		// Always clear the toggle button selection when annotations are added or removed
		clearAnnotationButtonSelection();
		// Enable/disable before next steps
		setEnabled(re != null);
		
		// Update annotation trees
		var annotationTree = AnnotationTree.buildTree(annotations, re == null ? null : re.getCyAnnotator());
		getBackgroundLayerPanel().update(annotationTree, Annotation.BACKGROUND);
		getForegroundLayerPanel().update(annotationTree, Annotation.FOREGROUND);
		
		// Enable/disable annotation add buttons
		if (isEnabled()) {
			// The ArrowAnnotation requires at least one other annotation before it can be added
			var btn = buttonMap.get(ArrowAnnotation.class);

			if (btn != null)
				btn.setEnabled(getAnnotationCount() > 0);
		}
		
		// Editor panel
		updateEditPanel();
		
		// Labels and other components
		updateInfoLabel();
		updateSelectionLabel();
		updateSelectionButtons();
		updateMoveToCanvasButtons();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void updateEditPanel() {
		var editComp = ((BorderLayout) getEditPanel().getLayout()).getLayoutComponent(BorderLayout.CENTER);
		
		// Remove annotation from current editor
		if (editComp instanceof AbstractAnnotationEditor)
			((AbstractAnnotationEditor<?>) editComp).setAnnotation(null);
		
		// Remove current editor
		if (editComp != null)
			getEditPanel().remove(editComp);
		
		if (isCreateMode()) {
			for (var entry : buttonMap.entrySet()) {
				var btn = entry.getValue();
				
				if (btn.isSelected()) {
					var f = btn.getFactory();
					
					if (f instanceof AbstractDingAnnotationFactory) {
						var comp = ((AbstractDingAnnotationFactory<?>) f).getEditor();
						
						if (comp != null) {
							getEditPanel().add(comp, BorderLayout.CENTER);
							getContentTabbedPane().setSelectedComponent(getEditPanel());
						}
					}
					
					break;
				}
			}
		} else {
			var selectedList = getSelectedAnnotations();
			var annotation = selectedList.size() == 1 ? selectedList.iterator().next() : null;
			
			if (annotation instanceof DingAnnotation) {
				var btn = buttonMap.get(((DingAnnotation) annotation).getType());
				
				if (btn != null) {
					var f = btn.getFactory();
					
					if (f instanceof AbstractDingAnnotationFactory) {
						var comp = ((AbstractDingAnnotationFactory) f).getEditor();
					
						if (comp != null) {
							comp.setAnnotation(annotation);
							getEditPanel().add(comp);
						}
					}
				}
			}
		}
		
		getContentTabbedPane().revalidate();
		getContentTabbedPane().repaint();
	}
	
	private void updateInfoLabel() {
		if (buttonGroup.getSelection() == null) {
			getInfoLabel().setText(isEnabled() ? "Select the Annotation you want to add..." : " ");
		} else {
			for (var btn : buttonMap.values()) {
				if (btn.isSelected()) {
					if (ArrowAnnotation.class.equals(btn.getFactory().getType()))
						getInfoLabel().setText("Click another Annotation in the view...");
					else
						getInfoLabel().setText("Click anywhere on the view...");
					
					break;
				}
			}
		}
	}
	
	void updateSelectionLabel() {
		int total = getAnnotationCount();
		
		if (total == 0) {
			getSelectionLabel().setText(null);
		} else {
			int selected = getSelectedAnnotationCount();
			getSelectionLabel().setText(selected + " of " + total + " Annotation" + (total == 1 ? "" : "s") + " selected");
		}
	}
	
	private void updateRemoveAnnotationsButton() {
		getRemoveAnnotationsButton().setEnabled(isEnabled() && getSelectedAnnotationCount() > 0);
	}
	
	private void updateGroupUngroupButton() {
		var annotations = getSelectedAnnotations();
		getGroupAnnotationsButton().setEnabled(isEnabled() && annotations.size() > 1 && AnnotationTree.hasSameParent(annotations));
		getUngroupAnnotationsButton().setEnabled(isEnabled() && getSelectedAnnotationCount(GroupAnnotation.class) > 0);
	}
	
	void updateSelectionButtons() {
		int total = getAnnotationCount();
		int selected = getSelectedAnnotationCount();
		getSelectAllButton().setEnabled(isEnabled() && selected < total);
		getSelectNoneButton().setEnabled(isEnabled() && selected > 0);
	}
	
	void updateMoveToCanvasButtons() {
		getPushToBackgroundButton().setEnabled(
				getSelectedAnnotationCount(getForegroundTree(), DingAnnotation.class) > 0);
		getPullToForegroundButton().setEnabled(
				getSelectedAnnotationCount(getBackgroundTree(), DingAnnotation.class) > 0);
	}
	
	void updateAnnotationsOrder() {
		var selectedAnnotations = getSelectedAnnotations();
		
		// Update all annotation trees, because an annotation may have been moved to another layer
		List<DingAnnotation> annotations = re != null ? re.getCyAnnotator().getAnnotations() : Collections.emptyList();
		{
			var annotationTree = AnnotationTree.buildTree(annotations, re.getCyAnnotator());
			getBackgroundLayerPanel().update(annotationTree, Annotation.BACKGROUND);
			getForegroundLayerPanel().update(annotationTree, Annotation.FOREGROUND);
		}
		// Restore the row selection (the annotations that were selected before must be still selected)
		getBackgroundTree().clearSelection();
		getForegroundTree().clearSelection();
		
		for (var a : selectedAnnotations)
			setSelected(a, true);
		
		getBackgroundLayerPanel().updateButtons();
		getForegroundLayerPanel().updateButtons();
	}
	
	void maybeUpdateEditingAnnotation(Annotation candidate) {
		// Do not edit if in creation mode; and ignore if already editing this annotation
		if (isCreateMode() || (candidate != null && candidate.isSelected() && candidate.equals(editingAnnotation)))
			return;
		
		Annotation selected = null;
		
		if (candidate != null && candidate.isSelected() && candidate instanceof GroupAnnotation == false) {
			// The passed one can be edited...
			selected = candidate;
		} else {
			// There may be another selected annotation...
			var stack = new Stack<Annotation>();
			
			if (candidate != null && candidate.isSelected())
				stack.add(candidate);
			
			stack.addAll(getSelectedAnnotations(getForegroundTree()));
			stack.addAll(getSelectedAnnotations(getBackgroundTree()));
			
			// Traverse the selection tree to find the fist non-GroupAnnotation that is selected
System.out.println("===============");
			while (!stack.isEmpty()) {
				var a = stack.pop();
				
				System.out.println(". " + a.getName());
//				if (a instanceof GroupAnnotation) {
//					for (var aa : ((GroupAnnotation) a).getMembers())
//						stack.push(aa);
//				}
				
				if (a.isSelected()) {
					selected = a;
					break;
				}
			}
		}
		
System.out.println("\t>>> " + selected);
		setEditingAnnotation(selected);
	}
	
	void stopTreeCellEditing() {
		stopCellEditing(getBackgroundTree());
		stopCellEditing(getForegroundTree());
	}
	
	void stopCellEditing(JTree tree) {
		if (tree.isEditing())
			tree.stopEditing();
	}
	
	private void init() {
		setOpaque(!isAquaLAF());
		
		equalizeSize(getGroupAnnotationsButton(), getUngroupAnnotationsButton(), getRemoveAnnotationsButton());
		equalizeSize(getSelectAllButton(), getSelectNoneButton());
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(!isAquaLAF());
		layout.setAutoCreateGaps(!isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getToolBarPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getContentTabbedPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getToolBarPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getContentTabbedPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		setEnabled(false);
	}
	
	JPanel getToolBarPanel() {
		if (toolBarPanel == null) {
			toolBarPanel = new JPanel();
			toolBarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
			
			if (isAquaLAF())
				toolBarPanel.setOpaque(false);
			
			var layout = new GroupLayout(toolBarPanel);
			toolBarPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(!isAquaLAF());
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addGroup(layout.createSequentialGroup()
							.addGap(0, 0, Short.MAX_VALUE)
							.addGroup(btnHGroup = layout.createSequentialGroup())
							.addGap(0, 0, Short.MAX_VALUE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(btnVGroup = layout.createParallelGroup(CENTER, true))
			);
		}
		
		return toolBarPanel;
	}
	
	JTabbedPane getContentTabbedPane() {
		if (contentTabbedPane == null) {
			contentTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
			contentTabbedPane.addTab("Annotation", getEditPanel());
			contentTabbedPane.addTab("Layers", getLayersPanel());
			makeSmall(contentTabbedPane);
		}
		
		return contentTabbedPane;
	}
	
	JPanel getEditPanel() {
		if (editPanel == null) {
			editPanel = new JPanel();
			editPanel.setLayout(new BorderLayout());
			editPanel.setOpaque(!isAquaLAF());
			editPanel.add(getInfoLabel(), BorderLayout.NORTH);
		}
		
		return editPanel;
	}
	
	JPanel getLayersPanel() {
		if (layersPanel == null) {
			layersPanel = new JPanel();
			layersPanel.setOpaque(!isAquaLAF());
			
			var layout = new GroupLayout(layersPanel);
			layersPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			// I don't know of a better way to center the selection label perfectly (with this layout manager)
			// other than by using this "filler" panel hack...
			var rightFiller = new JPanel();
			rightFiller.setPreferredSize(getRemoveAnnotationsButton().getPreferredSize());
			
			if (isAquaLAF())
				rightFiller.setOpaque(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addGroup(layout.createSequentialGroup()
							.addContainerGap()
							.addComponent(getGroupAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getUngroupAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(getSelectionLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(rightFiller, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getRemoveAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addContainerGap()
					)
					.addComponent(getForegroundLayerPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
							.addGap(0, 0, Short.MAX_VALUE)
							.addComponent(getPushToBackgroundButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(getPullToForegroundButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addGap(0, 0, Short.MAX_VALUE)
					)
					.addComponent(getBackgroundLayerPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
							.addContainerGap()
							.addComponent(getSelectAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getSelectNoneButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addContainerGap()
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER, true)
							.addComponent(getGroupAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getUngroupAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(rightFiller, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getRemoveAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getForegroundLayerPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(getPushToBackgroundButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getPullToForegroundButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getBackgroundLayerPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(getSelectAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getSelectNoneButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
		}
		return layersPanel;
	}
	
	private JLabel getInfoLabel() {
		if (infoLabel == null) {
			infoLabel = new JLabel(" ");
			infoLabel.setHorizontalAlignment(JLabel.CENTER);
			infoLabel.setEnabled(false);
			makeSmall(infoLabel);
		}
		
		return infoLabel;
	}
	
	private JLabel getSelectionLabel() {
		if (selectionLabel == null) {
			selectionLabel = new JLabel();
			selectionLabel.setHorizontalAlignment(JLabel.CENTER);
			makeSmall(selectionLabel);
		}
		
		return selectionLabel;
	}
	
	JButton getGroupAnnotationsButton() {
		if (groupAnnotationsButton == null) {
			groupAnnotationsButton = new JButton(IconManager.ICON_OBJECT_GROUP);
			groupAnnotationsButton.setToolTipText("Group Selected Annotations");
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(groupAnnotationsButton, iconManager.getIconFont(16f));
		}
		
		return groupAnnotationsButton;
	}
	
	JButton getUngroupAnnotationsButton() {
		if (ungroupAnnotationsButton == null) {
			ungroupAnnotationsButton = new JButton(IconManager.ICON_OBJECT_UNGROUP);
			ungroupAnnotationsButton.setToolTipText("Ungroup Selected Annotations");
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(ungroupAnnotationsButton, iconManager.getIconFont(16f));
		}
		
		return ungroupAnnotationsButton;
	}
	
	JButton getRemoveAnnotationsButton() {
		if (removeAnnotationsButton == null) {
			removeAnnotationsButton = new JButton(IconManager.ICON_TRASH_O);
			removeAnnotationsButton.setToolTipText("Remove Selected Annotations");
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(removeAnnotationsButton, iconManager.getIconFont(18f));
		}
		
		return removeAnnotationsButton;
	}
	
	JButton getPushToBackgroundButton() {
		if (pushToBackgroundButton == null) {
			pushToBackgroundButton = new JButton(IconManager.ICON_ARROW_DOWN);
			pushToBackgroundButton.setToolTipText("Push Annotations to Background Layer");
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(pushToBackgroundButton, iconManager.getIconFont(12f));
		}
		
		return pushToBackgroundButton;
	}
	
	JButton getPullToForegroundButton() {
		if (pullToForegroundButton == null) {
			pullToForegroundButton = new JButton(IconManager.ICON_ARROW_UP);
			pullToForegroundButton.setToolTipText("Pull Annotations to Foreground Layer");
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(pullToForegroundButton, iconManager.getIconFont(12f));
		}
		
		return pullToForegroundButton;
	}
	
	LayerPanel getForegroundLayerPanel() {
		if (foregroundLayerPanel == null) {
			foregroundLayerPanel = new LayerPanel(Annotation.FOREGROUND);
		}
		
		return foregroundLayerPanel;
	}
	
	LayerPanel getBackgroundLayerPanel() {
		if (backgroundLayerPanel == null) {
			backgroundLayerPanel = new LayerPanel(Annotation.BACKGROUND);
		}
		
		return backgroundLayerPanel;
	}
	
	JTree getForegroundTree() {
		return getForegroundLayerPanel().getTree();
	}
	
	JTree getBackgroundTree() {
		return getBackgroundLayerPanel().getTree();
	}
	
	JTree getLayerTree(String canvasName) {
		return Annotation.BACKGROUND.equals(canvasName) ? getBackgroundTree() : getForegroundTree();
	}

	JButton getSelectAllButton() {
		if (selectAllButton == null) {
			selectAllButton = new JButton("Select All");
			selectAllButton.addActionListener(evt -> {
				if (getBackgroundTree().getRowCount() > 0)
					getBackgroundTree().setSelectionInterval(0, getBackgroundTree().getRowCount() - 1);
				if (getForegroundTree().getRowCount() > 0)
					getForegroundTree().setSelectionInterval(0, getForegroundTree().getRowCount() - 1);
				
				maybeUpdateEditingAnnotation(editingAnnotation);
			});
			
			makeSmall(selectAllButton);
			
			if (isAquaLAF()) {
				selectAllButton.putClientProperty("JButton.buttonType", "gradient");
				selectAllButton.putClientProperty("JComponent.sizeVariant", "small");
			}
		}
		
		return selectAllButton;
	}
	
	JButton getSelectNoneButton() {
		if (selectNoneButton == null) {
			selectNoneButton = new JButton("Select None");
			selectNoneButton.addActionListener(evt -> {
				getBackgroundTree().clearSelection();
				getForegroundTree().clearSelection();
				maybeUpdateEditingAnnotation(null);
			});
			
			makeSmall(selectNoneButton);
			
			if (isAquaLAF()) {
				selectNoneButton.putClientProperty("JButton.buttonType", "gradient");
				selectNoneButton.putClientProperty("JComponent.sizeVariant", "small");
			}
		}
		
		return selectNoneButton;
	}
	
	private void styleToolBarButton(AbstractButton btn, Font font) {
		if (font != null)
			btn.setFont(font);
		
		btn.setFocusPainted(false);
		btn.setFocusable(false);
		btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		btn.setContentAreaFilled(false);
		btn.setOpaque(false);
		btn.setHorizontalTextPosition(SwingConstants.CENTER);
		btn.setVerticalTextPosition(SwingConstants.TOP);
	}
	
	private Icon getDefIcon() {
		if (defIcon == null) {
			// Lazily initialize the icon here, because the LAF might not have been set yet,
			// and we need to get the correct colors
			Font font = serviceRegistrar.getService(IconManager.class).getIconFont(IconUtil.CY_FONT_NAME, 16f);
			defIcon = new TextIcon(
					new String[] { IconUtil.ICON_ANNOTATION_1, IconUtil.ICON_ANNOTATION_2 },
					font,
					new Color[] { UIManager.getColor("Label.foreground"), Color.WHITE },
					AbstractDingAnnotationFactory.ICON_SIZE,
					AbstractDingAnnotationFactory.ICON_SIZE,
					1
			);
		}
		
		return defIcon;
	}
	
	private Icon getClosedAnnotationIcon() {
		if (closedAnnotationIcon == null) {
			// Lazily initialize the icon here, because the LAF might not have been set yet,
			// and we need to get the correct colors
			Font font = serviceRegistrar.getService(IconManager.class).getIconFont(16f);
			closedAnnotationIcon = new TextIcon(
					IconManager.ICON_FOLDER,
					font,
					UIManager.getColor("Label.foreground"),
					AbstractDingAnnotationFactory.ICON_SIZE,
					AbstractDingAnnotationFactory.ICON_SIZE
			);
		}
		
		return closedAnnotationIcon;
	}
	
	private Icon getOpenAnnotationIcon() {
		if (openAnnotationIcon == null) {
			// Lazily initialize the icon here, because the LAF might not have been set yet,
			// and we need to get the correct colors
			Font font = serviceRegistrar.getService(IconManager.class).getIconFont(16f);
			openAnnotationIcon = new TextIcon(
					IconManager.ICON_FOLDER_OPEN,
					font,
					UIManager.getColor("Label.foreground"),
					AbstractDingAnnotationFactory.ICON_SIZE,
					AbstractDingAnnotationFactory.ICON_SIZE
			);
		}
		
		return openAnnotationIcon;
	}
	
	private Icon getAnnotationIcon(Annotation a) {
		Icon icon = null;
		
		if (a instanceof DingAnnotation)
			icon = iconMap.get(((DingAnnotation) a).getType());

		return icon != null ? icon : getDefIcon();
	}
	
	//------[ CLASSES ]--------------------------------------------------------------------------------------
	
	class LayerPanel extends JPanel {
		
		private JLabel titleLabel;
		private JButton forwardButton;
		private JButton backwardButton;
		private JScrollPane scrollPane;
		private JTree tree;
		
		private final String canvasName;
		
		public LayerPanel(String canvasName) {
			this.canvasName = canvasName;
			init();
			setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, UIManager.getColor("Separator.foreground")));
			
			getTree().getSelectionModel().addTreeSelectionListener(e -> {
				stopTreeCellEditing();
				updateSelectionLabel();
				updateGroupUngroupButton();
				updateRemoveAnnotationsButton();
				updateMoveToCanvasButtons();
				updateButtons();
			});
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			getTree().setEnabled(enabled);
			getTitleLabel().setEnabled(enabled);
			updateButtons();
		}

		JButton getForwardButton() {
			if (forwardButton == null) {
				forwardButton = new JButton(IconManager.ICON_CARET_UP);
				forwardButton.setToolTipText("Bring Annotations Forward");
				
				var iconManager = serviceRegistrar.getService(IconManager.class);
				styleToolBarButton(forwardButton, iconManager.getIconFont(17f));
			}
			
			return forwardButton;
		}
		
		JButton getBackwardButton() {
			if (backwardButton == null) {
				backwardButton = new JButton(IconManager.ICON_CARET_DOWN);
				backwardButton.setToolTipText("Send Annotations Backward");
				
				var iconManager = serviceRegistrar.getService(IconManager.class);
				styleToolBarButton(backwardButton, iconManager.getIconFont(17f));
			}
			
			return backwardButton;
		}
		
		JScrollPane getScrollPane() {
			if (scrollPane == null) {
				scrollPane = new JScrollPane(getTree());
				scrollPane.setViewportView(getTree());
				scrollPane.getViewport().setOpaque(false);
				scrollPane.getViewport().addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent evt) {
						stopCellEditing(getTree());
						getTree().clearSelection();
						scrollPane.requestFocusInWindow();
					}
				});
				scrollPane.setBackground(TREE_BG_COLOR);
				scrollPane.setBorder(
						BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
			}
			
			return scrollPane;
		}
		
		JTree getTree() {
			if (tree == null) {
				var annotationCellRenderer = new AnnotationTreeCellRenderer();
				
				tree = new JTree(new AnnotationTreeModel(null, null)) {
					@Override
					public TreeCellRenderer getCellRenderer() {
						return annotationCellRenderer;
					}
					@Override
					public Color getBackground() {
						// This guarantees the color will come from the correct Look-And-Feel,
						// even if this component is initialized before swing-application-impl
						return TREE_BG_COLOR;
					}
					@Override
					public void paintComponent(Graphics g) {
						// Highlight entire JTree row on selection...
						g.setColor(getBackground());
						g.fillRect(0, 0, getWidth(), getHeight());
						int[] rows = getSelectionRows();
						
						if (rows != null) {
							for (int i : rows) {
								Rectangle r = getRowBounds(i);
								g.setColor(TREE_SEL_BG_COLOR);
								g.fillRect(0, r.y, getWidth(), r.height);
							}
						}
						
						super.paintComponent(g);
					}
					@Override
					public void setModel(TreeModel newModel) {
						super.setModel(newModel);
						// Or expand icons of first annotation groups won't be displayed
						setRootVisible(true);
						
						setRootVisible(false);
						expandRow(0);
					}
				};
				// Highlight entire JTree row on selection...
				tree.setUI(new BasicTreeUI() {
					@Override
					public Rectangle getPathBounds(JTree tree, TreePath path) {
						if (tree != null && treeState != null)
							return getPathBounds(path, tree.getInsets(), new Rectangle());
						
						return null;
					}
					
					private Rectangle getPathBounds(TreePath path, Insets insets, Rectangle bounds) {
						bounds = treeState.getBounds(path, bounds);
						
						if (bounds != null) {
							bounds.width = tree.getWidth();
							bounds.y += insets.top;
						}
						
						return bounds;
					}
				});
				tree.setName(canvasName);
				tree.setOpaque(false);
				tree.setRowHeight(24);
				makeSmall(tree);
				
				tree.setEditable(true);
				tree.setToggleClickCount(0);
				// Start editing with space key
				tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "startEditing");
				tree.setInvokesStopCellEditing(true); // this helps stop editing within focus of tree
				
				final JTextField textField = new JTextField();
				textField.setEditable(true);
				makeSmall(textField);
				
				TreeCellEditor txtEditor = new DefaultCellEditor(textField);
				TreeCellEditor editor = new AnnotationTreeCellEditor(tree, annotationCellRenderer, txtEditor);
				editor.addCellEditorListener(new CellEditorListener() {
					@Override
					public void editingStopped(ChangeEvent evt) {
						// Repaint both trees when a node's name has changed,
						// because a GroupAnnotation can have a corresponding node in each one
						getBackgroundTree().repaint();
						getForegroundTree().repaint();
					}
					@Override
					public void editingCanceled(ChangeEvent evt) {
						// Ignore...
					}
				});
				tree.setCellEditor(editor);
				
				tree.setShowsRootHandles(true);
				tree.expandRow(0);
				tree.setRootVisible(false);
				
				tree.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent evt) {
						int row = tree.getRowForLocation(evt.getPoint().x, evt.getPoint().y);
						
						if (row < 0) {
							stopCellEditing(tree);
							tree.clearSelection();
						}
					}
					@Override
					public void mouseClicked(MouseEvent evt) {
						if (evt.getClickCount() == 2) {
							TreePath selectionPath = tree.getSelectionPath();

							if (selectionPath != null)
								tree.startEditingAtPath(selectionPath);
						}
					}
				});
			}
			
			return tree;
		}
		
		private void init() {
			if (isAquaLAF())
				setOpaque(false);
			
			var layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(getTitleLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addGap(10, 10, Short.MAX_VALUE)
							.addComponent(getForwardButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getBackwardButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(LEADING, false)
							.addComponent(getTitleLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getForwardButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getBackwardButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		
		private JLabel getTitleLabel() {
			if (titleLabel == null) {
				var text = canvasName.toLowerCase() + " Layer";
				text = text.substring(0, 1).toUpperCase() + text.substring(1); // capitalize the first letter
				
				titleLabel = new JLabel(text);
				titleLabel.setVerticalAlignment(SwingConstants.BOTTOM);
				titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 2, 12));
				
				var tb = LookAndFeelUtil.createTitledBorder(text);
				
				if (tb instanceof TitledBorder)
					titleLabel.setFont(((TitledBorder) tb).getTitleFont());
				else
					makeSmall(titleLabel);
			}
			
			return titleLabel;
		}
		
		private void update(AnnotationTree annotationTree, String canvas) {
			// Save collapsed groups
			var tree = getTree();
			var collapsedGroups = new HashSet<GroupAnnotation>();
			
			for (int i = 0; i < tree.getRowCount(); i++) {
				var path = tree.getPathForRow(i);
				var node = (AnnotationNode) path.getLastPathComponent();
				var a = node.getAnnotation();
				
				if (a instanceof GroupAnnotation && tree.isCollapsed(path))
					collapsedGroups.add((GroupAnnotation) a);
			}
			
			// Update Tree Model
			tree.setModel(new AnnotationTreeModel(annotationTree, canvas));
			
			// Collapse groups that were collapsed before the update and expand all other groups by default.
			// IMPORTANT: getRowCount() increases after each expansion, so don't store it in a variable!
			for (int i = 0; i < tree.getRowCount(); i++) {
				var path = tree.getPathForRow(i);
				var node = (AnnotationNode) path.getLastPathComponent();
				var a = node.getAnnotation();
				
				if (a instanceof GroupAnnotation) {
					if (collapsedGroups.contains(a))
						tree.collapsePath(path);
					else
						tree.expandPath(path);
				}
			}
			
			// Update everything else
			updateButtons();
		}
		
		private void updateButtons() {
			getForwardButton().setEnabled(false);
			getBackwardButton().setEnabled(false);
			
			var tree = getTree();
			var annotationTree = ((AnnotationTreeModel)tree.getModel()).tree;
			
			if (annotationTree != null) {
				var selectedAnnotations = getSelectedAnnotations(tree);
				
				boolean forward  = annotationTree.shiftAllowed(Shift.UP_ONE, canvasName, selectedAnnotations);
				boolean backward = annotationTree.shiftAllowed(Shift.DOWN_ONE, canvasName, selectedAnnotations);
				
				getForwardButton().setEnabled(forward);
				getBackwardButton().setEnabled(backward);
			}
		}
	}
	
	class AnnotationToggleButton extends JToggleButton {
		
		private final AnnotationFactory<? extends Annotation> factory;

		public AnnotationToggleButton(AnnotationFactory<? extends Annotation> f) {
			this.factory = f;
			
			var icon = f.getIcon();
			setIcon(icon != null ? icon : getDefIcon());
			setToolTipText("<html>Add <b>" + f.getName() + " Annotation</b>...</html>");
			setHorizontalTextPosition(SwingConstants.CENTER);
		}
		
		public AnnotationFactory<? extends Annotation> getFactory() {
			return factory;
		}
	}
	
	class AnnotationTreeModel extends DefaultTreeModel {
		
		private AnnotationTree tree;
		private String canvas;
		
		public AnnotationTreeModel(AnnotationTree tree, String canvas) {
			super(null);
			
			if (tree != null && canvas != null) {
				var root = tree.getRoot(canvas);
				setRoot(root);
				this.tree = tree;
				this.canvas = canvas;
			}
		}
		
		@Override
		public AnnotationNode getRoot() {
			return (AnnotationNode) super.getRoot();
		}
		
		public List<Annotation> getData() {
			return getRoot().depthFirstOrder();
		}
		
		public TreePath pathTo(Annotation a) {
			var node = tree.get(canvas, a);
			return node == null ? null : new TreePath(node.getPath());
		}
	}
	
	private final class AnnotationTreeCellRenderer extends DefaultTreeCellRenderer {
        
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder()); // Do not highlight the focused cell
			setHorizontalAlignment(LEFT);
			
			if (value instanceof AnnotationNode) {
				var annotation = ((AnnotationNode) value).getAnnotation();
				
				if (annotation != null) {
					setText(annotation.getName());
					setToolTipText(annotation.getName());
					setIconTextGap(8);
				}
				
				if (annotation instanceof GroupAnnotation) {
					setOpenIcon(getOpenAnnotationIcon());
					setClosedIcon(getClosedAnnotationIcon());
				} else {
					var icon = getAnnotationIcon(annotation);
					setLeafIcon(icon);
					setIcon(icon);
				}
			}
			
			if (selected) {
				setForeground(TREE_SEL_FG_COLOR);
				setBackground(TREE_SEL_BG_COLOR);
			} else {
				setBackground(TREE_BG_COLOR);
				setForeground(TREE_FG_COLOR);
			}
			
			return this;
		}
	}
	
	class AnnotationTreeCellEditor extends DefaultTreeCellEditor {

		public AnnotationTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer, TreeCellEditor editor) {
			super(tree, renderer, editor);
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
				boolean leaf, int row) {
			if (value instanceof AnnotationNode)
				value = ((AnnotationNode) value).getAnnotation().getName();
			
			return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		}

		@Override
		public boolean isCellEditable(EventObject e) {
			return super.isCellEditable(e) && lastPath != null
					&& lastPath.getLastPathComponent() instanceof AnnotationNode;
		}

		@Override
		protected Container createContainer() {
	        return new EditorContainer() {
	        		@Override
	        		public void paint(Graphics g) {
	        			// There's a weird race condition that prevents the cell editor from rendering the
	        			// actual leaf icon for the editing row when double-clicking to edit the annotation name
	        			// (no problem when pressing SPACE), so let's force it to get the icon again
	        			// right before painting it.
						if (lastPath != null) {
							Object obj = lastPath.getLastPathComponent();

							if (obj instanceof AnnotationNode) {
								if (((AnnotationNode) obj).getAnnotation() instanceof GroupAnnotation == false)
									editingIcon = getAnnotationIcon(((AnnotationNode) obj).getAnnotation());
							}
						}

						super.paint(g);
					}
	        };
	    }
	}
}
