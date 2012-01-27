package org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingEditor;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

public abstract class AbstractContinuousMappingEditor<K extends Number, V> extends AbstractPropertyEditor implements ContinuousMappingEditor<K, V> {
	
	private static final Dimension DEF_SIZE = new Dimension(550, 400);
	private static final Dimension MIN_SIZE = new Dimension(300, 350);
	
	protected ContinuousMapping<K, V> mapping;
	protected ContinuousMappingEditorPanel<K, V> editorPanel;
	
	protected final CyNetworkTableManager manager;
	protected final CyApplicationManager appManager;
	protected final SelectedVisualStyleManager selectedManager;
	protected final EditorManager editorManager;
	
	protected final VisualMappingManager vmm;
	
	private final JLabel iconLabel;
	
	private boolean isEditorDialogActive;
	private JDialog currentDialog;
	
	public AbstractContinuousMappingEditor(final CyNetworkTableManager manager, final CyApplicationManager appManager, 
			final SelectedVisualStyleManager selectedManager, final EditorManager editorManager, final VisualMappingManager vmm) {
	
		this.isEditorDialogActive = false;
		this.iconLabel = new JLabel();
		this.vmm = vmm;
		this.manager = manager;
		this.appManager = appManager;
		this.selectedManager = selectedManager;
		this.editorManager = editorManager;
		
		editor = new JPanel();
		((JPanel)editor).setLayout(new BorderLayout());
		((JPanel)editor).add(iconLabel, BorderLayout.CENTER);
				
		this.editor.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent ev) {
				
				// Open only one editor at a time.
				if(isEditorDialogActive) {
					// Bring it to the front
					if(currentDialog != null)
						currentDialog.toFront();
					return;
				}
				
				final JDialog editorDialog = new JDialog();
				initComponents(editorDialog);
				
				editorDialog.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosed(WindowEvent evt) {
						final Dimension size = editor.getSize();
						drawIcon(size.width, size.height, false);
						isEditorDialogActive = false;
					}
				});
				
				editorDialog.setTitle("Continuous Mapping Editor for " + mapping.getVisualProperty().getDisplayName());
				editorDialog.setLocationRelativeTo(editor);
				editorDialog.setAlwaysOnTop(true);
				editorDialog.setVisible(true);
				isEditorDialogActive = true;
				currentDialog = editorDialog;
			}
			
			private void initComponents(final JDialog dialog) {

				dialog.setLayout(new BorderLayout());
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.getContentPane().add(editorPanel, BorderLayout.CENTER);

				dialog.setPreferredSize(DEF_SIZE);
				dialog.setMinimumSize(MIN_SIZE);

				dialog.pack();
			}
		});
	}
	
	
	/* (non-Javadoc)
	 * @see org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor.ContinuousMappingEditor#drawIcon(int, int, boolean)
	 */
	@Override
	public ImageIcon drawIcon(int width, int height, boolean detail) {
		if(editorPanel == null)
			return null;
		
		final ImageIcon newIcon = this.editorPanel.drawIcon(width, height, detail);
		iconLabel.setIcon(newIcon);
		
		return newIcon;
	}
	
	
	@Override public Object getValue() {
		return mapping;
	}
}
