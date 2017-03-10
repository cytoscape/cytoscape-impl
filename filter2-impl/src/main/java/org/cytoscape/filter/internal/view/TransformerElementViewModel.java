package org.cytoscape.filter.internal.view;

import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

import org.cytoscape.filter.internal.filters.composite.CompositeSeparator;
import org.cytoscape.filter.internal.work.ValidationViewListener;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

public class TransformerElementViewModel<V extends SelectPanelComponent> implements ValidationViewListener {
	
	private static final int WARN_ICON_WIDTH = 18;
	private static final int WARN_ICON_HEIGHT = 15;
	
	public final V parent;
	
	public final JComponent view;
	public final JComponent handle;
	public final JComponent deleteButton;
	public final JComponent separator;
	public final JComponent warnIcon;
	
	public TransformerElementViewModel(final JComponent view, final AbstractPanelController<?, V> controller, final V parent) {
		this.view = view;
		this.parent  = parent;
		
		handle = new Handle<V>(controller.getIconManager(), parent, controller, view);
		separator = new CompositeSeparator();
		deleteButton = createDeleteButton(controller.getIconManager(), controller);
		warnIcon = createWarningIcon(controller.getIconManager());

		new DropTarget(view,      new DragHandler<V>(view,      controller, parent, handle));
		new DropTarget(separator, new DragHandler<V>(separator, controller, parent, null));
		new DropTarget(handle,    new DragHandler<V>(handle,    controller, parent, null));
		new DropTarget(warnIcon,  new DragHandler<V>(warnIcon,  controller, parent, null));
	}

	private JComponent createDeleteButton(IconManager iconManager, final AbstractPanelController<?, V> controller) {
		JButton button = new JButton(IconManager.ICON_REMOVE);
		button.setFont(iconManager.getIconFont(15));
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setOpaque(false);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.addActionListener(e -> controller.handleDelete(parent, view));
		return button;
	}
	
	private JLabel createWarningIcon(IconManager iconManager) {
		JLabel warnIcon = new JLabel();
		warnIcon.setHorizontalTextPosition(SwingConstants.CENTER);
		warnIcon.setPreferredSize(new Dimension(WARN_ICON_WIDTH, WARN_ICON_HEIGHT));
		warnIcon.setFont(iconManager.getIconFont(16.0f));
		warnIcon.setText(IconManager.ICON_WARNING);
		warnIcon.setForeground(LookAndFeelUtil.getWarnColor());
		warnIcon.setVisible(false);
		
		// Hack to prolong a tooltip’s visible delay
		// Thanks to: http://tech.chitgoks.com/2010/05/31/disable-tooltip-delay-in-java-swing/
		// copied from VisualPropertySheetItem.getMsgIconLbl()
		warnIcon.addMouseListener(new MouseAdapter() {
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
		
		return warnIcon;
	}

	@Override
	public void handleValidated(ValidationEvent event) {
		warnIcon.setVisible(!event.isValid());
		warnIcon.setToolTipText(event.getFormattedTooltip());
	}
}