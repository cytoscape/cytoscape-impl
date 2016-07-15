package org.cytoscape.internal.dialogs;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.PanelTaskManager;

public class ExportImageDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private FileUtil fileUtil;
	private PanelTaskManager taskManager;
	private CyApplicationManager applicationManager;
	private CySessionManager sessionManager;
	private PresentationWriterManager writerManager;
	private CyFileFilter fileFilter;
	private Map<String, CyFileFilter> filterMap;
	
	private JComboBox<String> filterComboBox;
	private JTextField fileTextField;
	private JPanel controlPanel;

	private File file;
	private ByteArrayOutputStream byteArrayOutputStream;
	private CyWriter cyWriter;
	
	public ExportImageDialog(Window owner, CyServiceRegistrar registrar) {
		super(owner, "Export as Image", ModalityType.APPLICATION_MODAL);

		fileUtil = registrar.getService(FileUtil.class);
		taskManager = registrar.getService(PanelTaskManager.class);
		applicationManager = registrar.getService(CyApplicationManager.class);
		sessionManager = registrar.getService(CySessionManager.class);
		writerManager = registrar.getService(PresentationWriterManager.class);
		
		List<CyFileFilter> filterList = writerManager.getAvailableWriterFilters();
		fileFilter = filterList.get(0);
		filterMap = new HashMap<String, CyFileFilter>();
		for (CyFileFilter filter: filterList) {
	    	filterMap.put(filter.getDescription(), filter);
	    	if(filter.getDescription().contains("PNG")) {
	    		fileFilter = filter;
	    	}
		}
		
		initComponents();
		pack();
		setLocationRelativeTo(owner);
		setMinimumSize(new Dimension(600, 0));
	}
	
	private void initComponents() {
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		setResizable(false);
		
		JPanel formatPanel = new JPanel();
		formatPanel.setLayout(new BoxLayout(formatPanel, BoxLayout.LINE_AXIS));
		controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));

	    fileTextField = new JTextField(getSuggestedFileName());
	    fileTextField.setPreferredSize(new Dimension(fileTextField.getMinimumSize().width,
	    		fileTextField.getMinimumSize().height));
		
	    formatPanel.add(new JLabel("Format:"));
	    byteArrayOutputStream = new ByteArrayOutputStream();

	    filterComboBox = new JComboBox<String>(filterMap.keySet().toArray(new String[filterMap.keySet().size()]));
	    filterComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSelectedFilter();
			}});
	    
	    filterComboBox.setSelectedItem(fileFilter.getDescription());
	    formatPanel.add(filterComboBox);
	    add(formatPanel);
	    
	    JPanel filePanel = new JPanel();
		filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.LINE_AXIS));
	    filePanel.add(new JLabel("Save as:"));
	    filePanel.add(fileTextField);
	    
	    JButton browse = new JButton("Browse...");
	    browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browseForFile();
			}
	    });
	    filePanel.add(browse);
	    add(filePanel);
	    add(controlPanel);

	    JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
	    buttonPanel.add(Box.createHorizontalGlue());
	    JButton cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				setVisible(false);
			}
	    });
	    buttonPanel.add(cancelButton);
	    JButton okButton = new JButton("OK");
	    okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExport();
			}
	    });
	    buttonPanel.add(okButton);
	    add(buttonPanel);
	    LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), okButton.getAction(), cancelButton.getAction());
		getRootPane().setDefaultButton(okButton);
	}

	private String getSuggestedFileName() {
		String title = ViewUtil.getTitle(applicationManager.getCurrentNetworkView());
	    
		if (title == null || title.trim().isEmpty()) {
			title = sessionManager.getCurrentSessionFileName();
			title = title.substring(0, title.lastIndexOf("."));
		}
		
		String dir = fileUtil.getCurrentDirectory();
		if(!dir.endsWith(File.separator))
			dir = dir + File.separator;
		
		return dir + title + "." + fileFilter.getExtensions().iterator().next();
	}

	private void updateSelectedFilter() {
		String title = fileTextField.getText();
		// strip existing extension
		for(String ext : fileFilter.getExtensions()) {
			if(title.endsWith("." + ext)) {
				title = title.substring(0, title.lastIndexOf(ext) - 1);
				break;
			}
		}
		controlPanel.removeAll();
		fileFilter = filterMap.get(filterComboBox.getSelectedItem());
		if(fileFilter != null) {		    
			try {
				cyWriter = writerManager.getWriter(applicationManager.getCurrentNetworkView(), applicationManager.getCurrentRenderingEngine(), 
						fileFilter, byteArrayOutputStream);
				JPanel tunablePanel = taskManager.getConfiguration(null, cyWriter);
				controlPanel.add(tunablePanel);
			    fileTextField.setText(title + "." + fileFilter.getExtensions().iterator().next());
				pack();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	private void browseForFile() {
		File file = fileUtil.getFile(getOwner(), "Export Image", FileUtil.SAVE, getFileChooserFilters());
		if (file != null)
			fileTextField.setText(file.getAbsolutePath());
	}
	
	private void doExport() {
		if(fileTextField.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter a file name.",
					"Filename Required", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String fileName = fileUtil.addFileExt(getFileChooserFilters(), fileTextField.getText());
		file = new File(fileName);
		if(file.getParent() == null)
			file = new File(fileUtil.getCurrentDirectory(), fileName);
		//prompt if exists
		if(file.exists()) {
			int answer =
					JOptionPane.showConfirmDialog(
						this,
						"The file '" + file.getName()
						+ "' already exists. Are you sure you want to overwrite it?",
						"File exists",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
			if(answer == JOptionPane.NO_OPTION)
				return;
		}
		else if(!file.getParentFile().exists()) {
			JOptionPane.showMessageDialog(this, "The specified directory " + file.getParentFile().getAbsolutePath() + " does not exist.",
					"Invalid Path", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (taskManager.validateAndApplyTunables(cyWriter)) {
			setVisible(false);
			taskManager.execute(new TaskIterator(new WriterTask()));
		}
	}
	
	private Collection<FileChooserFilter> getFileChooserFilters() {
		CyFileFilter filter = filterMap.get(filterComboBox.getSelectedItem());
		Collection<FileChooserFilter> filters = null;
		if(filter != null) {
			filters = new ArrayList<FileChooserFilter>();
			FileChooserFilter chooserFilter = new FileChooserFilter
					(filter.getDescription(), filter.getExtensions().toArray(new String[filter.getExtensions().size()]));
			filters.add(chooserFilter);
		}
		return filters;
	}

	private class WriterTask extends AbstractTask {
		public void run(TaskMonitor taskMonitor) throws Exception {
			// this is a kludge to prevent the CyWriter from displaying the
			// already-set Tunables, while allowing it to use insertTasksAfterCurrentTask
			// and write the ByteArrayOutputStream to a File afterwards
			TaskIterator ti = new TaskIterator(cyWriter, new AbstractTask() {
				@Override
				public void run(TaskMonitor taskMonitor) throws Exception {
					FileOutputStream fos = new FileOutputStream(file);
					byteArrayOutputStream.writeTo(fos);
					fos.close();
				}});
			
			Task task = ti.next();
			task.run(taskMonitor);
			insertTasksAfterCurrentTask(ti);
		}
	}

}
