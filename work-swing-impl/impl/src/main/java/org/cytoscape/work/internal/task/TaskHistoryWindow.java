package org.cytoscape.work.internal.task;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;

@SuppressWarnings("serial")
public class TaskHistoryWindow {

	final TaskHistory taskHistory;
	final JDialog dialog;
	final JEditorPane pane;

	public TaskHistoryWindow(final TaskHistory taskHistory) {
		this.taskHistory = taskHistory;

		dialog = new JDialog(null, "Cytoscape Task History", JDialog.ModalityType.MODELESS);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		pane = new JEditorPane();
		pane.setEditable(false);
		pane.setContentType("text/html");
		final HTMLEditorKit htmlEditorKit = (HTMLEditorKit) pane.getEditorKit();
		final StyleSheet styleSheet = htmlEditorKit.getStyleSheet();
		styleSheet.addRule("ul {list-style-type: none;}");

		final JButton clearButton = new JButton("Clear Display");
		clearButton.addActionListener(e -> {
            taskHistory.clear();
            update();
        });
		
		final JButton closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});

		final JPanel buttonsPanel = LookAndFeelUtil.createOkCancelPanel(null, closeButton, clearButton);
		buttonsPanel.add(clearButton);

		final JScrollPane scrollPane = new JScrollPane(pane);

		final GroupLayout layout = new GroupLayout(dialog.getContentPane());
		dialog.getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(scrollPane, DEFAULT_SIZE, 500, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(buttonsPanel)
						.addContainerGap()
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(scrollPane, DEFAULT_SIZE, 400, Short.MAX_VALUE)
				.addComponent(buttonsPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addContainerGap()
		);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), null, closeButton.getAction());
		dialog.getRootPane().setDefaultButton(closeButton);

		taskHistory.setFinishListener(history -> update());

		dialog.pack();
		open();
	}

	public void open() {
		update();
		dialog.setVisible(true);
	}

	private static String getIconURL(final FinishStatus.Type finishType) {
		if (finishType == null)
			return null;
		
		String name = null;
		
		switch (finishType) {
			case SUCCEEDED:
				name = "finished";
				break;
			case FAILED:
				name = "error";
				break;
			case CANCELLED:
				name = "cancelled";
				break;
		}
		
		if (name == null)
			return null;
		
		return GUIDefaults.ICON_URLS.get(name).toString();
	}

	private static String getIconURL(final TaskMonitor.Level level) {
		if (level == null)
			return null;
		
		String name = null;
		
		switch (level) {
			case INFO:
				name = "info";
				break;
			case WARN:
				name = "warn";
				break;
			case ERROR:
				name = "error";
				break;
		}
		
		if (name == null)
			return null;
		
		return GUIDefaults.ICON_URLS.get(name).toString();
	}

	private void generateMessage(final TaskHistory.Message message, final StringBuffer buffer) {
		final TaskMonitor.Level level = message.level();
		final String levelIconURL = getIconURL(level);
		
		if (levelIconURL != null) {
			buffer.append("<li style=\"margin-top: 5px;\">");
			buffer.append("<img src=\"");
			buffer.append(levelIconURL);
			buffer.append("\">&nbsp;");
		} else {
			buffer.append("<li style=\"margin-top: 10px;\">");
			buffer.append("<b>");
		}
		
		buffer.append(message.message());
		
		if (level == null)
			buffer.append("</b>");
		
		buffer.append("</li>");
	}

	private void generateHistory(final TaskHistory.History history, final StringBuffer buffer) {
		if (history.getFirstTaskClass() == null) {
			// skip task iterators that never called history.setFirstTaskClass()
			// -- these
			// iterators were never started because they were cancelled by its
			// first tunable dialog
			return;
		}

		buffer.append("<p>");
		buffer.append("<h1 style=\"margin-top: 0px; margin-bottom: 0px;\">&nbsp;");

		final FinishStatus.Type finishType = history.getFinishType();
		final String finishIconURL = getIconURL(finishType);
		
		if (finishIconURL != null) {
			buffer.append("<img src=\"");
			buffer.append(finishIconURL);
			buffer.append("\">&nbsp;");
		}

		final String title = history.getTitle();
		
		if (title == null || title.length() == 0) {
			buffer.append("<i>Untitled</i>");
			final Class<?> klass = history.getFirstTaskClass();
			
			if (klass != null) {
				buffer.append(" <font size=\"-1\">(");
				buffer.append(klass.getName());
				buffer.append(")</font>");
			}
		} else {
			buffer.append(title);
		}
		
		buffer.append("</h1>");
		buffer.append("<ul style=\"margin-top: 0px; margin-bottom: 0px;\">");
		
		for (final TaskHistory.Message message : history) {
			generateMessage(message, buffer);
		}
		
		buffer.append("</ul>");
		buffer.append("</p>");
	}

	private String generateHistoryHTML() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("<html>");

		for (final Object element : taskHistory) {
			if (element instanceof TaskHistory.History) {
				generateHistory((TaskHistory.History) element, buffer);
			} else if (element instanceof TaskHistory.Message) {
				buffer.append(
						"<ul style=\"margin-top: 0px; margin-bottom: 0px; margin-left: 0px; padding-left: 0px;\">");
				generateMessage((TaskHistory.Message) element, buffer);
				buffer.append("</ul>");
			}
		}
		
		buffer.append("</html>");
		
		return buffer.toString();
	}

	public void update() {
		final String content = generateHistoryHTML();

		if (SwingUtilities.isEventDispatchThread()) {
			pane.setText(content);
		} else {
			SwingUtilities.invokeLater(() -> pane.setText(content));
		}
	}
}
