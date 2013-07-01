package org.cytoscape.work.internal.task;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Font;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Dialog.ModalityType;
import javax.swing.BoxLayout;
import javax.swing.Box;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;

/**
 * Handles the task window's user interface and
 * the user interface for individual tasks.
 */
class TaskWindow {

	protected final JDialog dialog;
	protected final JScrollPane scrollPane;
	protected final JPanel tasksPanel;

	protected final List<TaskUIImpl> tasks = new ArrayList<TaskUIImpl>();

	public TaskWindow() {
		dialog = new JDialog(null, "Cytoscape Tasks", ModalityType.MODELESS);
		dialog.setPreferredSize(new Dimension(500, 400));

		dialog.setLayout(new GridBagLayout());
		final EasyGBC c = new EasyGBC();

		final JButton cleanButton = new JButton("Clean");
		cleanButton.setToolTipText("Clean list of tasks by removing all finished, cancelled, and failed tasks.");
		cleanButton.addActionListener(new CleanAction());

		tasksPanel = new JPanel();
		tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
		scrollPane = new JScrollPane(tasksPanel);
		dialog.add(scrollPane, c.expandBoth());

		final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonsPanel.add(cleanButton);
		dialog.add(buttonsPanel, c.down().expandHoriz());

		dialog.pack();
	}	

	public void show() {
		dialog.setVisible(true);
	}

	public void hide() {
		dialog.setVisible(false);
	}
	
	public TaskUI createTaskUI() {
		final TaskUIImpl taskUI = new TaskUIImpl();
		tasks.add(taskUI);
		tasksPanel.add(taskUI.getPanel());

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
				scrollBar.setUnitIncrement((scrollBar.getMaximum() - scrollBar.getMinimum()) / 20);
			}
		});

		return taskUI;
	}

	class CleanAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			final Iterator<TaskUIImpl> i = tasks.iterator();
			while (i.hasNext()) {
				final TaskUIImpl ui = i.next();
				if (ui.isCompleted()) {
					ui.remove();
					i.remove();
				}
			}
			tasksPanel.revalidate();
			tasksPanel.repaint();
		}
	}
}	

class TaskUIImpl implements TaskUI {
	private static JLabel labelWithFont(String style) {
		final JLabel label = new JLabel();
		if (style == null)
			style = "plain 12";
		style = "Helvetica " + style;
		label.setFont(Font.decode(style));
		return label;
	}

	boolean completed = false;
	int numStatusMsgs = 0;
	final JLabel titleLabel;
	final RoundedProgressBar progressBar;
	final JLabel cancelLabel;
	final JPanel msgsPanel;
	final DiscloseTriangle discloseTriangle;
	final JLabel statusLabel;
	final JLabel cancelStatusLabel;
	final JPanel taskPanel;

	public void setTaskAsCompleted() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setTaskAsCompleted();
				}
			});
			return;
		}

		progressBar.setVisible(false);
		cancelLabel.setVisible(false);
		cancelStatusLabel.setVisible(false);
		completed = true;
	}

	public void setTitle(final String title) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setTitle(title);
				}
			});
			return;
		}

		titleLabel.setText(title);
	}

	public void setProgress(final float progress) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setProgress(progress);
				}
			});
			return;
		}

		if (0.0f <= progress && progress <= 1.0f) {
			progressBar.setProgress(progress);
		} else {
			progressBar.setIndeterminate();
		}
	}

	public void addMessage(final Icon icon, final String msg) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					addMessage(icon, msg);
				}
			});
			return;
		}

		if (numStatusMsgs == 0) {
			statusLabel.setVisible(true);
		} else {
			final JLabel label = labelWithFont("plain 12");
			label.setText(statusLabel.getText());
			label.setIcon(statusLabel.getIcon());
			label.setHorizontalAlignment(JLabel.LEFT);
			msgsPanel.add(label);
			msgsPanel.add(Box.createRigidArea(new Dimension(0, 10)), -1);
		}
		if (numStatusMsgs == 1) {
			discloseTriangle.setVisible(true);
		}
		statusLabel.setIcon(icon);
		statusLabel.setText(msg);
		numStatusMsgs++;
	}

	public void addCancelListener(final ActionListener listener) {
		cancelLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				ActionEvent event = new ActionEvent(TaskUIImpl.this, 0, "cancel");
				listener.actionPerformed(event);
			}
		});
	}

	public void setCancelStatus(final String status) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setCancelStatus(status);
				}
			});
			return;
		}

		cancelStatusLabel.setText(status);
	}

	public void disableCancelButton() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					disableCancelButton();
				}
			});
			return;
		}

		cancelLabel.setVisible(false);
	}

	public void remove() {
		taskPanel.getParent().remove(taskPanel);
	}

	public boolean isCompleted() {
		return completed;
	}

	public TaskUIImpl() {
		titleLabel = labelWithFont("bold 16");
		progressBar = new RoundedProgressBar();
		cancelLabel = new CancelButton();

		msgsPanel = new JPanel();
		msgsPanel.setLayout(new BoxLayout(msgsPanel, BoxLayout.Y_AXIS));
		msgsPanel.setVisible(false);
		discloseTriangle = new DiscloseTriangle();
		statusLabel = labelWithFont("plain 12");
		cancelStatusLabel = labelWithFont("italic 12");

		discloseTriangle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				msgsPanel.setVisible(discloseTriangle.isOpen());
			}
		});

		discloseTriangle.setVisible(false);

		final EasyGBC c = new EasyGBC();
		final JPanel statusPanel = new JPanel(new GridBagLayout());
		statusPanel.add(discloseTriangle, c.insets(0, 0, 0, 5));
		statusPanel.add(statusLabel, c.right().expandHoriz().insets(0, 0, 0, 0));
		statusPanel.add(cancelStatusLabel, c.right().noExpand().anchor("east"));
		statusPanel.add(msgsPanel, c.down().spanHoriz(2).expandHoriz().anchor("northwest").insets(8, 17, 0, 0));

		taskPanel = new JPanel(new GridBagLayout());
		taskPanel.add(titleLabel, c.reset().spanHoriz(2).expandHoriz().insets(20, 20, 0, 20));
		taskPanel.add(progressBar, c.down().noSpan().expandHoriz().insets(15, 20, 0, 10));
		taskPanel.add(cancelLabel, c.right().noExpand().anchor("east").insets(7, 0, 0, 20));
		taskPanel.add(statusPanel, c.down().spanHoriz(2).expandHoriz().insets(15, 20, 0, 20));

		final JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		separator.setForeground(new Color(0xdddddd));
		separator.setOpaque(false);
		taskPanel.add(separator, c.down().spanHoriz(2).expandHoriz().insets(20, 20, 0, 20));
	}

	public JPanel getPanel() {
		return taskPanel;
	}
}

class CancelButton extends JLabel {
	protected static final Icon CANCEL_ICON  		= new ImageIcon(CancelButton.class.getResource("/images/cancel-icon.png"));
	protected static final Icon CANCEL_HOVER_ICON	= new ImageIcon(CancelButton.class.getResource("/images/cancel-hover-icon.png"));
	protected static final Icon CANCEL_PRESSED_ICON = new ImageIcon(CancelButton.class.getResource("/images/cancel-pressed-icon.png"));

	public CancelButton() {
		super(CANCEL_ICON);
		super.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				final boolean isPressed = (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0;
				setIcon(isPressed ? CANCEL_PRESSED_ICON : CANCEL_HOVER_ICON);
			}

			public void mousePressed(MouseEvent e) {
				setIcon(CANCEL_PRESSED_ICON);
			}

			public void mouseReleased(MouseEvent e) {
				final int x = e.getX();
				final int y = e.getY();
				if (0 <= x && x <= getWidth() &&
					0 <= y && y <= getHeight()) {
					setIcon(CANCEL_HOVER_ICON);
				} else {
					setIcon(CANCEL_ICON);
				}
			}

			public void mouseExited(MouseEvent e) {
				setIcon(CANCEL_ICON);
			}
		});
	}
}
