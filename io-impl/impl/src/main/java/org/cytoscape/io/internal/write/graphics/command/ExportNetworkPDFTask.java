package org.cytoscape.io.internal.write.graphics.command;

import static org.cytoscape.io.internal.write.graphics.PDFWriter.PreDefinedPageSize.*;

import java.io.OutputStream;

import org.cytoscape.io.internal.write.graphics.PDFWriter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class ExportNetworkPDFTask extends AbstractExportNetworkImageTask {
	
	
	@Tunable(
		longDescription = "If true (the default value), texts will be exported as fonts.",
		exampleStringValue = "true"
	)
	public boolean exportTextAsFont = true;
	
	
	@Tunable(
		longDescription = "If true then node and edge labels will not be visible in the image.",
		exampleStringValue = "false"
	)
	public boolean hideLabels;
		
		
	public ListSingleSelection<String> pageSize;
	@Tunable(
		description = "Page Size:",
		longDescription = "Predefined standard page size, or choose custom. Default is 'Letter'.",
		exampleStringValue = "Letter"
	)
	public ListSingleSelection<String> getPageSize() {
		return pageSize;
	}
	public void setPageSize(ListSingleSelection<String> pageSize) {
		this.pageSize = pageSize;
	} 
		

	public ListSingleSelection<String> orientation = new ListSingleSelection<>(PDFWriter.PORTRAIT, PDFWriter.LANDSCAPE);
	@Tunable(
		longDescription = "Page orientation, portrait or landscape.",
		exampleStringValue = PDFWriter.PORTRAIT
	)
	public ListSingleSelection<String> getOrientation() {
		return orientation;
	}
	public void setOrientation(ListSingleSelection<String> orientation) {
		this.orientation = orientation;
	} 
		
	
	public ExportNetworkPDFTask(CyServiceRegistrar registrar) {
		super(registrar);
		// Do not include "Custom" as an option
		pageSize = new ListSingleSelection<>(
				AUTO.label, LETTER.label, LEGAL.label, TABLOID.label, 
				A0.label, A1.label, A2.label, A3.label, A4.label, A5.label
		);
		pageSize.setSelectedValue(LETTER.label);
	}

	
	@Override
	CyWriter createWriter(RenderingEngine<?> re, OutputStream outStream) {
		var writer = new PDFWriter(re, outStream);
		writer.exportTextAsFont = exportTextAsFont;
		writer.hideLabels = hideLabels;
		writer.pageSize.setSelectedValue(pageSize.getSelectedValue());
		writer.orientation.setSelectedValue(orientation.getSelectedValue());
		return writer;
	}
	
}
