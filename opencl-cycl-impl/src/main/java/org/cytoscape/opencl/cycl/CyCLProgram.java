package org.cytoscape.opencl.cycl;

import org.lwjgl.opencl.*;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Scanner;

public class CyCLProgram 
{
	private Boolean finalized = false;
	
	private CLProgram program;
	private Hashtable<String, CyCLKernel> kernels = new Hashtable<String, CyCLKernel>();
	private HashMap<String, String> defines;
	
	public CyCLProgram(CyCLContext context, CyCLDevice device, URL resourcePath, String[] kernelNames, HashMap<String, String> defines) throws IOException
	{
    	InputStream programTextStream = resourcePath.openStream();
    	Scanner programTextScanner = new Scanner(programTextStream, "UTF-8");
    	String programText = programTextScanner.useDelimiter("\\Z").next();
    	programTextScanner.close();
        programTextStream.close();
        
        this.defines = new HashMap<>();
        if (defines != null)
	        for (Entry<String, String> entry : defines.entrySet())
	        {
	        	String defineline = "#define " + entry.getKey() + " " + entry.getValue() + "\n";
	        	programText = defineline + programText;
	        	this.defines.put(entry.getKey(), entry.getValue());
	        }
    
        IntBuffer errorBuffer = BufferUtils.createIntBuffer(1);
        program = CL10.clCreateProgramWithSource(context.getContext(), programText, errorBuffer);
        Util.checkCLError(errorBuffer.get(0));
		
        Util.checkCLError(CL10.clBuildProgram(program, device.getDevice(), "", null));
		
		for(String kernelName : kernelNames)
			kernels.put(kernelName, new CyCLKernel(context, this, kernelName));
	}
	
	public CLProgram getProgram()
	{
		return program;
	}
	
	public CyCLKernel getKernel(String name)
	{
		return kernels.get(name);
	}
	
	@Override
	protected void finalize() throws Throwable 
	{
		if(finalized)
			return;
		
		for(Entry<String, CyCLKernel> entry : kernels.entrySet())
			entry.getValue().finalize();
		kernels.clear();
		
		Util.checkCLError(CL10.clReleaseProgram(program));
		
		finalized = true;		
		super.finalize();
	}
}