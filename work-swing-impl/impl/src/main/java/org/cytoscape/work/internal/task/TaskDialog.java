package org.cytoscape.work.internal.task;

import java.util.Map;
import java.util.HashMap;

import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.GridBagLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class TaskDialog extends JDialog {
  public static final String CANCEL_EVENT = "task-cancel-event";
  public static final String CLOSE_EVENT = "task-close-event";

  public static final Map<String,URL> ICON_URLS = new HashMap<String,URL>();
  static {
    ICON_URLS.put("info",           TaskDialog.class.getResource("/images/info-icon.png"));
    ICON_URLS.put("warn",           TaskDialog.class.getResource("/images/warn-icon.png"));
    ICON_URLS.put("error",          TaskDialog.class.getResource("/images/error-icon.png"));
    ICON_URLS.put("finished",       TaskDialog.class.getResource("/images/finished-icon.png"));
    ICON_URLS.put("cancel",         TaskDialog.class.getResource("/images/cancel-icon.png"));
    ICON_URLS.put("cancel-hover",   TaskDialog.class.getResource("/images/cancel-hover-icon.png"));
    ICON_URLS.put("cancel-pressed", TaskDialog.class.getResource("/images/cancel-pressed-icon.png"));
    ICON_URLS.put("cancelled",      TaskDialog.class.getResource("/images/cancelled-icon.png"));
  }

  public static final Map<String,Icon> ICONS = new HashMap<String,Icon>();
  static {
    for (final Map.Entry<String,URL> icon : ICON_URLS.entrySet()) {
      ICONS.put(icon.getKey(), new ImageIcon(icon.getValue()));
    }
  }

  static final int DEFAULT_WIDTH = 500;

  static JLabel newLabelWithFont(final int style, final int size) {
    final Font defaultFont = UIManager.getFont("Label.font");
    final Font font = new Font(defaultFont == null ? null : defaultFont.getName(), style, size);
    final JLabel label = new JLabel();
    label.setFont(font);
    label.setPreferredSize(new Dimension(DEFAULT_WIDTH, size));
    return label;
  }

  static JButton newLinkButton(final Icon normal, final Icon hover, final Icon clicked) {
    final JButton button = new JButton(normal);
    final MouseAdapter iconUpdater = new MouseAdapter() {
      boolean pressed = false;
      boolean inside = false;
      public void mousePressed(MouseEvent e) {
        pressed = true;
        button.setIcon(clicked);
      }

      public void mouseReleased(MouseEvent e) {
        pressed = false;
        button.setIcon(inside ? hover : normal);
      }

      public void mouseEntered(MouseEvent e) {
        inside = true;
        button.setIcon(pressed ? clicked : hover);
      }

      public void mouseExited(MouseEvent e) {
        inside = false;
        button.setIcon(pressed ? clicked : normal);
      }
    };
    button.addMouseListener(iconUpdater);
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return button;
  }

  volatile boolean errorOccurred = false;

  final JLabel titleLabel;
  final JLabel subtitleLabel;
  final RoundedProgressBar progressBar;
  final JLabel msgLabel;
  final JButton cancelButton;
  final JLabel cancelLabel;
  final JButton closeButton;

  public TaskDialog(final Window parent) {
    super(parent, "Cytoscape Task", DEFAULT_MODALITY_TYPE);

    titleLabel    = newLabelWithFont(Font.PLAIN, 20);
    subtitleLabel = newLabelWithFont(Font.PLAIN, 14);
    subtitleLabel.setVisible(false);

    progressBar   = new RoundedProgressBar();
    progressBar.setIndeterminate();

    msgLabel      = new JLabel();
    msgLabel.setPreferredSize(new Dimension(DEFAULT_WIDTH, msgLabel.getFont().getSize() * 2 /* show multiline text */));
    msgLabel.setVerticalAlignment(JLabel.TOP);

    cancelLabel   = newLabelWithFont(Font.ITALIC, 12);
    cancelLabel.setText("Cancelling");
    cancelLabel.setVisible(false);

    cancelButton  = newLinkButton(ICONS.get("cancel"), ICONS.get("cancel-hover"), ICONS.get("cancel-pressed"));
    cancelButton.setToolTipText("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancel();
      }
    });

    closeButton   = new JButton("Close");
    closeButton.setVisible(false);
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    final EasyGBC c = new EasyGBC();

    final JPanel progressPanel = new JPanel(new GridBagLayout());
    progressPanel.add(progressBar, c.expandHoriz());
    progressPanel.add(cancelButton, c.right().noExpand().insets(0, 10, 0, 0));

    final JPanel msgPanel = new JPanel(new GridBagLayout());
    msgPanel.add(msgLabel, c.reset().expandBoth().anchor("northwest"));
    msgPanel.add(cancelLabel, c.right().noExpand().insets(0, 10, 0, 10));
    msgPanel.add(closeButton, c.right().noExpand().insets(0, 10, 0, 10));

    super.setLayout(new GridBagLayout());
    super.add(titleLabel, c.reset().expandHoriz().insets(10, 10, 10, 10));
    super.add(subtitleLabel, c.down().insets(0, 10, 10, 10));
    super.add(progressPanel, c.down().insets(0, 10, 0, 10));
    super.add(msgPanel, c.down().expandBoth().anchor("northwest").spanHoriz(2).insets(10, 10, 10, 10));
    super.pack();

    super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    super.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if (errorOccurred)
          close();
        else
          cancel();
      }
    });
    super.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
    super.setModal(true);
    super.setLocationRelativeTo(parent);
  }

  public void setTaskTitle(final String taskTitle) {
    final String currentTitle = titleLabel.getText();
    if (currentTitle == null || currentTitle.length() == 0) {
      titleLabel.setText(taskTitle);
      super.setTitle("Cytoscape: " + taskTitle);
    } else {
      if (!subtitleLabel.isVisible()) {
        subtitleLabel.setVisible(true);
        delayedPack(); // component visibilities change -- update dialog size to reflect this
      }
      subtitleLabel.setText(taskTitle);
    }
  }

  /**
   * Wrap the {@code pack()} invocation in a {@code invokeLater},
   * so that Swing has time to respond to the changing visibility
   * of components. When a component's visibility changes, we need
   * to adjust the dialog size to accomodate the component. But Swing
   * doesn't immediately recognize the changed component's visibility,
   * so we have to update the dialog's size via {@code pack()} by wrapping
   * it in {@code invokeLater}.
   */
  void delayedPack() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        pack();
      }
    });
  }

  public void setPercentCompleted(final float percent) {
    if (percent < 0.0f) {
      progressBar.setIndeterminate();
    } else {
      progressBar.setProgress(percent);
    }
  }

  public void setException(final Throwable t) {
    t.printStackTrace();
    this.errorOccurred = true;
    setStatus("error", t.getMessage());
    progressBar.setVisible(false);
    closeButton.setVisible(true);
    cancelButton.setVisible(false);
    cancelLabel.setVisible(false);
    delayedPack(); // component visibilities change -- update dialog size to reflect this
  }

  public void setStatus(final String icon, final String message) {
    if (icon == null)
      msgLabel.setIcon(null);
    else
      msgLabel.setIcon(ICONS.get(icon));
    msgLabel.setText(message);
  }

  public boolean errorOccurred() {
    return errorOccurred;
  }

  void cancel() {
    cancelLabel.setVisible(true);
    cancelButton.setVisible(false);
    // don't need to call pack() here, because the dialog will have enough space
    progressBar.setIndeterminate();
    firePropertyChange(CANCEL_EVENT, null, null);
  }

  void close() {
    firePropertyChange(CLOSE_EVENT, null, null);
  }
}
