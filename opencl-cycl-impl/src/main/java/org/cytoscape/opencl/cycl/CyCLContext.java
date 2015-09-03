package org.cytoscape.opencl.cycl;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opencl.*;
import org.lwjgl.BufferUtils;

public class CyCLContext 
{
	private Boolean finalized = false;
	
	private CLContext context;
	private CLCommandQueue queue;
	
	public CyCLContext(CyCLDevice device)
	{
		List<CLDevice> devices = new ArrayList<>();
		devices.add(device.getDevice());
		IntBuffer errorBuffer = BufferUtils.createIntBuffer(1);
		
		try
		{
	        context = CLContext.create(device.getPlatform(), devices, errorBuffer);
	        Util.checkCLError(errorBuffer.get(0));
	        
	        queue = CL10.clCreateCommandQueue(context, device.getDevice(), CL10.CL_QUEUE_PROFILING_ENABLE, errorBuffer);
	        Util.checkCLError(errorBuffer.get(0));
		}
		catch (Exception e) { throw new RuntimeException("Could not create device context."); }
	}
	
	public CLContext getContext()
	{
		return context;
	}
	
	public CLCommandQueue getQueue()
	{
		return queue;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(finalized)
			return;
		
		CL10.clReleaseCommandQueue(queue);
		CL10.clReleaseContext(context);
		
		finalized = true;		
		super.finalize();
	}
}
