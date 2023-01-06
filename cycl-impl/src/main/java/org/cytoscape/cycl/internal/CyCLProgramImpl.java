package org.cytoscape.cycl.internal;

import org.cytoscape.cycl.CyCLContext;
import org.cytoscape.cycl.CyCLDevice;
import org.cytoscape.cycl.CyCLException;
import org.cytoscape.cycl.CyCLKernel;
import org.cytoscape.cycl.CyCLProgram;

import org.lwjgl.*;
import org.lwjgl.opencl.*;
import org.lwjgl.system.*;

import static org.lwjgl.opencl.CL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.*;


public class CyCLProgramImpl implements CyCLProgram
{
  private Boolean finalized = false;

  private long program;
  private Hashtable<String, CyCLKernelImpl> kernels = new Hashtable<String, CyCLKernelImpl>();
  private HashMap<String, String> defines;

  public CyCLProgramImpl(CyCLContext context, CyCLDevice device, URL resourcePath, String[] kernelNames, HashMap<String, String> defines, boolean silentCompilation) throws IOException
  {
      InputStream programTextStream = resourcePath.openStream();
      Scanner programTextScanner = new Scanner(programTextStream, "UTF-8");
      String programText = programTextScanner.useDelimiter("\\Z").next();
      programTextScanner.close();
      programTextStream.close();

      prepareAndBuildProgram(context, device, new String[] {programText}, kernelNames, defines, silentCompilation);
  }

  public CyCLProgramImpl(CyCLContext context, CyCLDevice device, String source, String[] kernelNames, HashMap<String, String> defines, boolean silentCompilation)
  {
        prepareAndBuildProgram(context, device, new String[] {source}, kernelNames, defines, silentCompilation);
  }

  public CyCLProgramImpl(CyCLContext context, CyCLDevice device, String sources[], String[] kernelNames, HashMap<String, String> defines, boolean silentCompilation)
  {
        prepareAndBuildProgram(context, device, sources, kernelNames, defines, silentCompilation);
  }

  private void prepareAndBuildProgram(CyCLContext context, CyCLDevice device, String[] sources, String[] kernelNames, HashMap<String, String> defines, boolean silentCompilation)
  {
    try {
      this.defines = defines;
      StringBuilder buildOptions = new StringBuilder();
      if (defines != null)
      {
        for (Entry<String, String> entry : defines.entrySet()) {
          if(entry.getValue() == null)
          {
            buildOptions.append(" -D").append(entry.getKey());
          }
          else
          {
            buildOptions.append(" -D").append(entry.getKey()).append("=").append(entry.getValue());
          }
        }
      }

      IntBuffer errorBuffer = BufferUtils.createIntBuffer(1);
      //System.out.println("Sources = ["+String.join(",", sources)+"]");
      //System.out.println("Context = "+context.getContext());
      program = CL10.clCreateProgramWithSource(context.getContext(), sources, errorBuffer);
      CyCLUtils.checkCLError(errorBuffer.get(0));

      CountDownLatch latch = new CountDownLatch(1);
      CLProgramCallback buildCallback;
      int errcode = clBuildProgram(program, device.getDevice(), buildOptions, buildCallback = CLProgramCallback.create((program, user_data) -> {
          System.out.println(String.format(
              "The cl_program [0x%X] was built %s",
              program,
              getProgramBuildInfoInt(device.getDevice(), CL_PROGRAM_BUILD_STATUS) == CL_SUCCESS ? "successfully" : "unsuccessfully"
          ));
          String log = getProgramBuildInfoStringASCII(device.getDevice(), CL_PROGRAM_BUILD_LOG);
          if (!log.isEmpty()) {
              System.out.println(String.format("BUILD LOG:\n----\n%s\n-----", log));
          }

          latch.countDown();
      }), NULL);
      CyCLUtils.checkCLError(errcode);

//      CyCLUtils.checkCLError(CL10.clBuildProgram(program, device.getDevice(), buildOptions.toString(), null, NULL));

      for (String kernelName : kernelNames)
      {
        kernels.put(kernelName, new CyCLKernelImpl((CyCLContextImpl)context, this, kernelName));
      }

    }
    catch (Exception exc)
    {
      if (!silentCompilation && program != NULL) //TODO change to Cytoscape logging mechanism
        System.out.println(getBuildInfoString(device, CL10.CL_PROGRAM_BUILD_LOG));

      System.out.println("Could not create CL program");
      throw new CyCLException("Could not create CL program", exc);
    }

    System.out.println("Program built");
  }

  public long getProgram()
  {
    return program;
  }

  public CyCLKernel getKernel(String name)
  {
    return kernels.get(name);
  }

  public String getBuildInfoString(CyCLDevice device, int param_name) {
    try (MemoryStack stack = stackPush()) {
      PointerBuffer pp = stack.mallocPointer(1);
      CyCLUtils.checkCLError(clGetProgramBuildInfo(program, device.getDevice(), param_name, (IntBuffer)null, pp));
      int bytes = (int)pp.get(0);

      ByteBuffer buffer = stack.malloc(bytes);
      CyCLUtils.checkCLError(clGetProgramBuildInfo(program, device.getDevice(), param_name, buffer, null));
      return buffer.toString();
    }
  }

  public String getProgramBuildInfoStringASCII(long cl_device_id, int param_name) {
    try (MemoryStack stack = stackPush()) {
      PointerBuffer pp = stack.mallocPointer(1);
      CyCLUtils.checkCLError(clGetProgramBuildInfo(program, cl_device_id, param_name, (ByteBuffer)null, pp));
      int bytes = (int)pp.get(0);

      ByteBuffer buffer = stack.malloc(bytes);
      CyCLUtils.checkCLError(clGetProgramBuildInfo(program, cl_device_id, param_name, buffer, null));

      return memASCII(buffer, bytes - 1);
    }
  }


  public int getProgramBuildInfoInt(long cl_device_id, int param_name) {
    try (MemoryStack stack = stackPush()) {
      IntBuffer pl = stack.mallocInt(1);
      CyCLUtils.checkCLError(clGetProgramBuildInfo(program, cl_device_id, param_name, pl, null));
      return pl.get(0);
    }
  }


  protected void finalize()
  {
    try
    {
      if(finalized)
        return;

      for(Entry<String, CyCLKernelImpl> entry : kernels.entrySet())
        entry.getValue().finalize();
      kernels.clear();

      CyCLUtils.checkCLError(CL10.clReleaseProgram(program));

      finalized = true;
      super.finalize();
    }
    catch (Throwable exc)
    {
      System.out.println("Could not finalize CyCLProgram " + program + ": " + exc.getMessage());
      throw new CyCLException("Could not finalize CyCLProgram object.");
    }
  }
}
