package org.cytoscape.view.table.internal.cg;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.table.CellCustomGraphics;
import org.cytoscape.view.presentation.property.table.CellCustomGraphicsFactory;
import org.cytoscape.view.table.internal.cg.sparkline.CellSparkline;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyValueEditor;

public class CellCGValueEditor implements VisualPropertyValueEditor<CellCustomGraphics> {

	private boolean editCancelled;
	
	private JTabbedPane groupTpn;
	private JPanel bottomPnl;
//	private ImageCustomGraphicsSelector imageSelector;
	private Map<String/*group*/, CellCGPanel> cgPnlMap;
	private JButton removeBtn;
	private JButton cancelBtn;
	private JButton applyBtn;
	
	private CellCustomGraphics oldCustomGraphics;
	private CellCustomGraphics newCustomGraphics;
	
	private boolean initialized;

//	private final CustomGraphicsBrowser browser;
	private final CellCGManager cgMgr;
	private final CyServiceRegistrar serviceRegistrar;
	
	private JDialog dialog;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public CellCGValueEditor(/*CustomGraphicsBrowser browser, */CellCGManager cgMgr, CyServiceRegistrar serviceRegistrar) {
//		this.browser = browser;
		this.cgMgr = cgMgr;
		this.serviceRegistrar = serviceRegistrar;
		cgPnlMap = new HashMap<>();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public <S extends CellCustomGraphics> CellCustomGraphics showEditor(Component parent, S initialValue,
			VisualProperty<S> vp) {
		oldCustomGraphics = initialValue;
		
		// Make sure it initializes only after the Cytoscape UI (specially DefaultViewPanel) is ready
		if (!initialized) {
			init(parent);
			initialized = true;
		}
		
		update();
		
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		
		return editCancelled ? oldCustomGraphics : newCustomGraphics;
	}

	@Override
	public Class<CellCustomGraphics> getValueType() {
		return CellCustomGraphics.class;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void init(Component parent) {
		var owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
		dialog = new JDialog(owner, ModalityType.APPLICATION_MODAL);
		dialog.setMinimumSize(new Dimension(400, 600));
		dialog.setTitle("Graphics");
		dialog.setResizable(false);
		
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});
		
		var layout = new GroupLayout(dialog.getContentPane());
		dialog.getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getGroupTpn(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getBottomPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getGroupTpn(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getBottomPnl())
		);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), getApplyBtn().getAction(),
				getCancelBtn().getAction());
		dialog.getRootPane().setDefaultButton(getApplyBtn());
	}
	
	private void update() {
//		getImageSelector().update(oldCustomGraphics);
//		Component newSelectedComp = getImageSelector();
		Component newSelectedComp = null;
		var iter = cgPnlMap.entrySet().iterator();
		
		while (iter.hasNext()) {
			var cgPnl = iter.next().getValue();
			cgPnl.update(oldCustomGraphics instanceof CellSparkline ? (CellSparkline) oldCustomGraphics : null);
			
			if (cgPnl.getEditorCount() == 0) {
				iter.remove();
				
				if (getGroupTpn().indexOfComponent(cgPnl) >= 0)
					getGroupTpn().remove(cgPnl);
				
				continue;
			}
			
			if (cgPnl.canEdit(oldCustomGraphics))
				newSelectedComp = cgPnl;
		}
		
		if (newSelectedComp != null)
			getGroupTpn().setSelectedComponent(newSelectedComp);
	}
	
	private void remove() {
		editCancelled = false;
		newCustomGraphics = NullCellCustomGraphics.getNullObject();
		dialog.dispose();
	}
	
	private void cancel() {
		editCancelled = true;
		dialog.dispose();
	}
	
	private void apply() {
		editCancelled = false;
		var c = getGroupTpn().getSelectedComponent();
		
//		if (c instanceof ImageCustomGraphicsSelector)
//			newCustomGraphics = ((ImageCustomGraphicsSelector) c).getSelectedValue();
//		else 
		if (c instanceof CellCGPanel)
			newCustomGraphics = ((CellCGPanel) c).getCustomGraphics();

		dialog.dispose();
	}
	
	private JTabbedPane getGroupTpn() {
		if (groupTpn == null) {
			groupTpn = new JTabbedPane();
//			groupTpn.addTab("Images", getImageSelector());
			
			for (var group : cgMgr.getGroups()) {
				var cgPnl = getCGPnl(group);
				
				if (cgPnl != null)
					groupTpn.addTab(group, cgPnl);
			}
		}
		
		return groupTpn;
	}
	
	private JPanel getBottomPnl() {
		if (bottomPnl == null) {
			bottomPnl = LookAndFeelUtil.createOkCancelPanel(getApplyBtn(), getCancelBtn(), getRemoveBtn());
		}
		
		return bottomPnl;
	}
	
//	private ImageCustomGraphicsSelector getImageSelector() {
//		if (imageSelector == null) {
//			imageSelector = new ImageCustomGraphicsSelector(browser, serviceRegistrar);
//			imageSelector.addActionListener(evt -> apply());
//		}
//		
//		return imageSelector;
//	}
	
	private CellCGPanel getCGPnl(String group) {
		var cgPnl = cgPnlMap.get(group);
		
		if (cgPnl == null) {
			cgPnl = new CellCGPanel(group);
			cgPnl.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
			cgPnlMap.put(group, cgPnl);
		}
		
		return cgPnl;
	}
	
	public JButton getRemoveBtn() {
		if (removeBtn == null) {
			removeBtn = new JButton("Remove Graphics");
			removeBtn.addActionListener(evt -> remove());
		}
		
		return removeBtn;
	}
	
	@SuppressWarnings("serial")
	private JButton getCancelBtn() {
		if (cancelBtn == null) {
			cancelBtn = new JButton(new AbstractAction("Cancel") {
				@Override
				public void actionPerformed(ActionEvent e) {
					cancel();
				}
			});
		}
		
		return cancelBtn;
	}
	
	@SuppressWarnings("serial")
	private JButton getApplyBtn() {
		if (applyBtn == null) {
			applyBtn = new JButton(new AbstractAction("Apply") {
				@Override
				public void actionPerformed(ActionEvent e) {
					apply();
				}
			});
		}
		
		return applyBtn;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	/**
	 * Panel that contains all CellCustomGraphics editors that belong to the same group
	 */
	@SuppressWarnings("serial")
	private class CellCGPanel extends JPanel {
		
		static final int ICON_SIZE = 18;
		
		private final String group;
		private Collection<CellCustomGraphicsFactory> supportedFactories = Collections.emptyList();
		private CellCustomGraphics cg;
		private boolean updatingTypes;
		
		private JTabbedPane typeTpn;

		public CellCGPanel(String group) {
			this.group = group;
			this.setLayout(new BorderLayout());
			this.add(getTypeTpn(), BorderLayout.CENTER);
		}
		
		String getGroup() {
			return group;
		}
		
		boolean canEdit(CellCustomGraphics cg) {
			for (var cf : supportedFactories) {
				if (cf.getSupportedClass().isAssignableFrom(cg.getClass()))
					return true;
			}
			
			return false;
		}
		
		CellCustomGraphics getCustomGraphics() {
			return cg;
		}
		
		int getEditorCount() {
			return getTypeTpn().getTabCount();
		}
		
		private void update(CellCustomGraphics cg) {
			updatingTypes = true;
			supportedFactories = cgMgr.getFactories(group);
			
			CellCGEditorPane selectedEditorPn = null;
			int maxWidth = 100;
			
			try {
				getTypeTpn().removeAll();
				CellCustomGraphics initialCg = null;
				
				for (var cf : supportedFactories) {
					var cgEditorPn = new CellCGEditorPane(cf);
					var icon = cf.getIcon(ICON_SIZE, ICON_SIZE);
					
					if (cg != null) {
						initialCg = cf.getInstance(cg.getProperties());
						
						if (cf.getSupportedClass().isAssignableFrom(cg.getClass())) {
							selectedEditorPn = cgEditorPn;
							this.cg = initialCg;
						}
					} else {
						initialCg = cf.getInstance(new HashMap<String, Object>());
						this.cg = null;
					}
					
					cgEditorPn.update(initialCg);
					
					if (cgEditorPn.getEditor() != null) {
						getTypeTpn().addTab(
								icon == null ? cf.getDisplayName() : "", 
								icon, 
								cgEditorPn,
								cf.getDisplayName());
						
						maxWidth = Math.max(maxWidth, cgEditorPn.getPreferredSize().width);
					}
				}
				
				if (selectedEditorPn != null)
					getTypeTpn().setSelectedComponent(selectedEditorPn);
				else
					getTypeTpn().setSelectedIndex(0);
			} finally {
				updatingTypes = false;
			}
			
			if (this.cg == null) {
				selectedEditorPn = (CellCGEditorPane) getTypeTpn().getSelectedComponent();
				
				if (selectedEditorPn != null)
					this.cg = selectedEditorPn.getCg();
			}
			
			getTypeTpn().setPreferredSize(new Dimension(maxWidth + 40, 520));
		}
		
		private JTabbedPane getTypeTpn() {
			if (typeTpn == null) {
				typeTpn = new JTabbedPane();
				typeTpn.addChangeListener(evt -> {
					if (updatingTypes) return;
					
					var c = typeTpn.getSelectedComponent();
					
					if (c instanceof CellCGEditorPane) {
						var cf = ((CellCGEditorPane) c).getFactory();
						
						if (cg == null || !cf.getSupportedClass().isAssignableFrom(cg.getClass()))
							cg = cf.getInstance(cg != null ? cg.getProperties() : new HashMap<String, Object>());
							
						((CellCGEditorPane)c).update(cg);
					}
				});
			}
			
			return typeTpn;
		}
		
		@Override
		public String toString() {
			return "CellCG Panel for " + group;
		}
		
		private class CellCGEditorPane extends JScrollPane {
			
			private final CellCustomGraphicsFactory factory;
			private JComponent editor;
			private CellCustomGraphics cg;

			CellCGEditorPane(CellCustomGraphicsFactory factory) {
				this.factory = factory;
				this.setBorder(BorderFactory.createEmptyBorder());
				this.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
				this.getViewport().setOpaque(!LookAndFeelUtil.isAquaLAF());
			}
			
			void update(CellCustomGraphics initialCg) {
				this.cg = initialCg;
				editor = factory.createEditor(initialCg);
				this.setViewportView(editor);
				this.updateUI();
			}
			
			CellCustomGraphics getCg() {
				return cg;
			}
			
			CellCustomGraphicsFactory getFactory() {
				return factory;
			}
			
			JComponent getEditor() {
				return editor;
			}
			
			@Override
			public String toString() {
				return "CellCGEditorPane for " + factory.getDisplayName();
			}
		}
	}
}
