package org.cytoscape.work.internal.task;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

class TaskDialog2 extends JDialog {
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
  
  private static final int PADDING = 10;
  private static final int DEFAULT_WIDTH = 600;

  public static final Map<String,Icon> ICONS = new HashMap<String,Icon>();
  static {
    for (final Map.Entry<String,URL> icon : ICON_URLS.entrySet()) {
      ICONS.put(icon.getKey(), new ImageIcon(icon.getValue()));
    }
  }

  static JLabel newLabelWithFont(final int style, final int size) {
    final Font defaultFont = UIManager.getFont("Label.font");
    final Font font = new Font(defaultFont == null ? null : defaultFont.getName(), style, size);
    final JLabel label = new JLabel();
    label.setFont(font);
    label.setPreferredSize(new Dimension(DEFAULT_WIDTH, size + PADDING));
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

  final SwingTaskMonitor parentTaskMonitor;
  boolean haltRequested = false;
  boolean errorOccurred = false;

  final JLabel titleLabel;
  final JLabel subtitleLabel;
  final RoundedProgressBar progressBar;
  final JEditorPane msgLabel;
  final JButton cancelButton;
  final JButton closeButton;

  public TaskDialog2(final Window parent, final SwingTaskMonitor parentTaskMonitor) {
    super(parent, "Cytoscape Task", DEFAULT_MODALITY_TYPE);

    this.parentTaskMonitor = parentTaskMonitor;

    titleLabel    = newLabelWithFont(Font.PLAIN, 20);
    subtitleLabel = newLabelWithFont(Font.PLAIN, 14);

    progressBar   = new RoundedProgressBar();
    progressBar.setIndeterminate();

    msgLabel      = new JEditorPane();
    msgLabel.setEditable(false);
    msgLabel.setContentType("text/html");
    msgLabel.setOpaque(false);

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

    final JPanel msgPanel = new JPanel(new GridBagLayout());
    msgPanel.add(msgLabel, c.right().expandHoriz().insets(0, 10, 0, 0));
    msgPanel.add(closeButton, c.right().noExpand().insets(0, 10, 0, 0));

    super.setLayout(new GridBagLayout());
    super.add(titleLabel, c.reset().expandBoth().spanHoriz(2).insets(10, 10, 0, 10));
    super.add(subtitleLabel, c.down().expandBoth().spanHoriz(2).insets(0, 10, 0, 10));
    super.add(progressBar, c.down().expandHoriz().noSpan().insets(0, 10, 0, 10));
    super.add(cancelButton, c.right().noExpand().insets(0, 0, 0, 10));
    super.add(msgPanel, c.down().expandBoth().spanHoriz(2).insets(10, 10, 10, 10));
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
      super.setTitle("Cytoscape Task: " + taskTitle);
    } else {
      subtitleLabel.setText(taskTitle);
    }
  }

  public void setPercentCompleted(final int percent) {
    if (percent < 0) {
      progressBar.setIndeterminate();
    } else {
      progressBar.setProgress(percent / 100.f);
    }
  }

  public void setException(final Throwable t, final String userErrorMessage) {
    t.printStackTrace();
    this.errorOccurred = true;
    this.subtitleLabel.setIcon(ICONS.get("error"));
    msgLabel.setText("Error: " + t.getMessage());
    progressBar.setVisible(false);
    closeButton.setVisible(true);
    cancelButton.setVisible(false);
  }

  public void setStatus(final String message) {
    msgLabel.setText(message);
    this.pack();
  }


  public boolean errorOccurred() {
    return errorOccurred;
  }

  public boolean haltRequested() {
    return haltRequested;
  }

  synchronized void cancel() {
    if (haltRequested)
      return;

    haltRequested = true;
    msgLabel.setText("Cancelling");
    cancelButton.setVisible(false);
    progressBar.setIndeterminate();
    parentTaskMonitor.cancel();
  }

  synchronized void close() {
    parentTaskMonitor.close();
  }
}