package org.cytoscape.work.internal.task;


import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableInterceptor;


/**
 * Executes <code>Task</code>s and displays interfaces by
 * writing to a <code>PrintStream</code>.
 *
 * This is better suited for applications running in headless mode.
 *
 * This will only periodically display information about
 * <code>Task</code>s it is executing to prevent the screen from being flooded
 * with messages.
 *
 * This cannot cancel <code>Task</code>s because it has no means for receiving
 * input from the user.
 */
public class HeadlessTaskManager extends AbstractTaskManager {
	private final PrintStream output;
	private final ExecutorService taskExecutorService;

	public HeadlessTaskManager(final PrintStream output, final TunableInterceptor tunableInterceptor) {
		super(tunableInterceptor);
		this.output = output;
		this.taskExecutorService = Executors.newCachedThreadPool();
	}

	/**
	 * Use <code>System.out</code> as the output stream.
	 */
	public HeadlessTaskManager(final TunableInterceptor tunableInterceptor) {
		this(System.out, tunableInterceptor);
	}

	@Override
	public void execute(final TaskFactory factory) {
		final TaskIterator taskIterator = factory.createTaskIterator();
		final Timer timer = new Timer();
		final ConsoleTaskMonitor taskMonitor = new ConsoleTaskMonitor(timer);

		Runnable runnable = new Runnable() {
			public void run() {
				try {
					while (taskIterator.hasNext()) {
						final Task task = taskIterator.next();

						if (tunableInterceptor != null) {
							// load the tunables from the object
							tunableInterceptor.loadTunables(task);

							// create the UI based on the object
							if (!tunableInterceptor.execUI(task))
								return;
						}

						task.run(taskMonitor);
					}
				} catch (Exception exception) {
					taskMonitor.showException(exception);
				}
				timer.cancel();
			}
		};

		Future<?> future = taskExecutorService.submit(runnable);	
	}

	class ConsoleTaskMonitor implements TaskMonitor {
		static final int UPDATE_DELAY_IN_MILLISECONDS = 2000;

		final Timer timer;
		String title = "Task";
		String statusMessage = "";
		int progress = 0;
		boolean hasChanged = false;

		public ConsoleTaskMonitor(Timer timer)
		{
			this.timer = timer;
			timer.scheduleAtFixedRate(new UpdateTask(), UPDATE_DELAY_IN_MILLISECONDS, UPDATE_DELAY_IN_MILLISECONDS);
		}

		public void setTitle(String title)
		{
			if (title == null || title.length() == 0)
				this.title = "Task";
			else
				this.title = title;
		}

		public void setProgress(double progress)
		{
			this.progress = (int) (progress * 100);
			hasChanged = true;
		}

		public void setStatusMessage(String statusMessage)
		{
			if (statusMessage == null)
				this.statusMessage = "";
			else
				this.statusMessage = statusMessage;
			hasChanged = true;
		}

		public void showException(Exception exception)
		{
			timer.cancel();
			if (exception.getMessage() == null || exception.getMessage().length() == 0)
				output.println(String.format("%s has encountered an error.", title));
			else
				output.println(String.format("%s has encountered an error: %s", title, exception.getMessage()));
			exception.printStackTrace(output);
		}

		class UpdateTask extends TimerTask
		{
			public void run()
			{
				if (!hasChanged) return;

				if (statusMessage.length() == 0)
					output.println(String.format("%s is at %d%%.", title, progress));
				else
					output.println(String.format("%s is at %d%%: %s", title, progress, statusMessage));
				
				hasChanged = false;
			}
		}
	}
}
