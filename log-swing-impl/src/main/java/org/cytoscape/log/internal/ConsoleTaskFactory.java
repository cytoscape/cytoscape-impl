package org.cytoscape.log.internal;


import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.application.swing.CySwingApplication;

import org.ops4j.pax.logging.spi.PaxLoggingEvent;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.Date;
import java.text.DateFormat;

import javax.swing.ImageIcon;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;



/**
 * @author Pasteur
 */
public class ConsoleTaskFactory implements TaskFactory {
	final BlockingQueue<PaxLoggingEvent> simpleQueue;
	final BlockingQueue<PaxLoggingEvent> advancedQueue;
	final ExecutorService service;
	final CytoStatusBar statusBar;
	final CySwingApplication app;
	final TaskManager manager;
	final Map simpleLogConfig;
	final Map advancedLogConfig;

	ConsoleDialog dialog = null;
	SimpleLogViewer simpleLogViewer = null;
	AdvancedLogViewer advancedLogViewer = null;

	public ConsoleTaskFactory(BlockingQueue<PaxLoggingEvent> simpleQueue,
				  BlockingQueue<PaxLoggingEvent> advancedQueue,
				  ExecutorService service,
				  CytoStatusBar statusBar,
				  CySwingApplication app,
				  TaskManager manager,
				  Map simpleLogConfig,
				  Map advancedLogConfig)
	{
		this.simpleQueue = simpleQueue;
		this.advancedQueue = advancedQueue;
		this.service = service;
		this.statusBar = statusBar;
		this.app = app;
		this.manager = manager;
		this.simpleLogConfig = simpleLogConfig;
		this.advancedLogConfig = advancedLogConfig;

		statusBar.addActionListener(new ConsoleAction());
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new ConsoleTask());
	}

	synchronized ConsoleDialog getDialog() {
		if (dialog == null) {
			simpleLogViewer = new SimpleLogViewer(statusBar, new LogViewer(simpleLogConfig));
			advancedLogViewer = new AdvancedLogViewer(manager, new LogViewer(advancedLogConfig));
			dialog = new ConsoleDialog(app, simpleLogViewer, advancedLogViewer);
			SimpleUpdater simpleUpdater = new SimpleUpdater(simpleLogViewer, simpleQueue);
			service.submit(simpleUpdater);
			AdvancedUpdater advancedUpdater = new AdvancedUpdater(advancedLogViewer, advancedQueue);
			service.submit(advancedUpdater);
		}
		return dialog;
	}

	class ConsoleTask extends AbstractTask {
		@Override
		public void run(TaskMonitor taskMonitor) {
			getDialog().setVisible(true);
		}

		@Override
		public void cancel() {
		}
	}

	class ConsoleAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			getDialog().setVisible(true);
		}
	}
}


class SimpleUpdater extends QueueProcesser {
	static DateFormat DATE_FORMATTER = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG);

	final SimpleLogViewer simpleLogViewer;

	public SimpleUpdater(SimpleLogViewer simpleLogViewer, BlockingQueue<PaxLoggingEvent> internalQueue)
	{
		super(internalQueue);
		this.simpleLogViewer = simpleLogViewer;
	}

	public void processEvent(PaxLoggingEvent event)
	{
		String message = event.getMessage().toString();
		String timeStamp = DATE_FORMATTER.format(new Date(event.getTimeStamp()));
		simpleLogViewer.append(event.getLevel().toString(), message, timeStamp);
	}
}

class AdvancedUpdater extends QueueProcesser
{
	static final DateFormat DATE_FORMATTER = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);

	final AdvancedLogViewer advancedLogViewer;

	public AdvancedUpdater(AdvancedLogViewer advancedLogViewer, BlockingQueue<PaxLoggingEvent> queue)
	{
		super(queue);
		this.advancedLogViewer = advancedLogViewer;
	}

	public void processEvent(PaxLoggingEvent event)
	{
		String[] formattedEvent = new String[5];
		formattedEvent[0] = DATE_FORMATTER.format(new Date(event.getTimeStamp()));
		formattedEvent[1] = event.getLoggerName();
		formattedEvent[2] = event.getLevel().toString().toLowerCase();
		formattedEvent[3] = event.getThreadName();
		formattedEvent[4] = event.getMessage().toString();

		advancedLogViewer.addLogEvent(formattedEvent);
	}
}
