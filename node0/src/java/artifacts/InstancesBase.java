/*******************************************************************************
 *  ----------------------------------------------------------------------------------------------------------
 *  Copyright (C) 2015 Xavier Limón, Alejandro Guerra-Hernández, Nicandro Cruz-Ramírez, Francisco Grimaldo-Moreno
 * Departmento de Inteligencia Artificial. Universidad Veracruzana. 
 * Departament d’Informàtica. Universitat de València
 *
 *  This file is part of JaCa-DDM.
 *
 *     JaCa-DDM is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     JaCa-DDM is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with JaCa-DDM.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  To contact the authors:
 *    xavier120@hotmail.com, aguerra@uv.mx, francisco.grimaldo@uv.es
 *    http://www.uv.mx/aguerra/
 *   http://www.uv.es/grimo/
 * -----------------------------------------------------------------------------------------------------------
 *******************************************************************************/
package artifacts;


import static jcuda.driver.JCudaDriver.cuCtxCreate;
import static jcuda.driver.JCudaDriver.cuCtxSynchronize;
import static jcuda.driver.JCudaDriver.cuDeviceGet;
import static jcuda.driver.JCudaDriver.cuInit;
import static jcuda.driver.JCudaDriver.cuLaunchKernel;
import static jcuda.driver.JCudaDriver.cuMemAlloc;
import static jcuda.driver.JCudaDriver.cuCtxDestroy;
import static jcuda.driver.JCudaDriver.cuMemFree;
import static jcuda.driver.JCudaDriver.cuMemcpyDtoH;
import static jcuda.driver.JCudaDriver.cuCtxSetCurrent;
import static jcuda.driver.JCudaDriver.cuMemcpyHtoD;
import static jcuda.driver.JCudaDriver.cuModuleGetFunction;
import static jcuda.driver.JCudaDriver.cuModuleLoad;

import java.io.File;
import java.util.Random;
import java.util.LinkedList;
import java.util.List;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUcontext;
import jcuda.driver.CUdevice;
import jcuda.driver.CUdeviceptr;
import jcuda.driver.CUfunction;
import jcuda.driver.CUmodule;
import jcuda.driver.JCudaDriver;
import jcuda.runtime.JCuda;
import jcuda.runtime.cudaDeviceProp;
import classifiers.BaseClassifier;
import classifiers.CUDATree;
import classifiers.J48CUDATree;
import moa.classifiers.Classifier;
import utils.InstanceManager;
import utils.ParallelSearch;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import cartago.ARTIFACT_INFO;
import cartago.Artifact;
import cartago.ArtifactId;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OUTPORT;
import cartago.OpFeedbackParam;
import cartago.OperationException;


@ARTIFACT_INFO(
		  outports = {
		    @OUTPORT(name="portExa1"),
		    @OUTPORT(name="portExa2") 
		  }
		)
public class InstancesBase extends Artifact 
{	
	private Instances examples;
	private BaseClassifier model;	
	private CUDATree gpuModel;
	private CUdeviceptr deviceInstances;
	private int multiProc;
	private int maxThreads;
	private CUcontext context;
	private CUfunction function;
	private CUfunction function2;
	private CUfunction function3;
	private CUfunction function4;
	private FastVector attInfo;
	
	void init()
	{
		
	}
	
	void init(String params) throws Exception 
	{			    
		params = params.substring(2, params.length()-1); //get ride of b( and the last )

		String atributos = params.substring(params.indexOf(",")+1);
		
		//Gets the information of the attributes 
		this.attInfo = new InstanceManager().attsInfo(atributos);
		
	    //Creates a new Instances object based on the information of the attributes to Train Data 
	    this.examples = new Instances("DB", attInfo,0);
		
	    //Setting class attribute
		this.examples.setClassIndex(this.examples.numAttributes() - 1);
	}
	
	
	@LINK
	void initExamples(Instances header)
	{
		this.examples = header;
	}
	
	/*
	 * if an instancesbase artifact does not feed examples from a file 
	 */
	/*@OPERATION
	void sendHeader(String port)
	{
		Instances header = new Instances(this.examples, 0);
		
		try 
		{				
			execLinkedOp(port,"initExamples", header);	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	*/
	//remote version
	@OPERATION
	void sendHeader(ArtifactId aid)
	{
		Instances header = new Instances(this.examples, 0);
		
		try
		{			
			execLinkedOp(aid,"initExamples", header);	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	
	@OPERATION 
	void feedExamplesFromFile(String path, OpFeedbackParam<java.lang.String> attsInfo)
	{
		try
		{
			this.examples = new InstanceManager().readArff(path);
			this.examples.setClassIndex(this.examples.numAttributes() - 1);	
			this.examples.randomize(new Random());
			attsInfo.set(attsInfo());			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	@OPERATION
	void sendExample()
	{
		try 
		{
			
			OpFeedbackParam<Instance> ii = new OpFeedbackParam<Instance>();
			ii.set(this.examples.firstInstance());
			this.examples.delete(0);
			execLinkedOp("portExa1","addInstance", ii.get());
			
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	/*
	 * this is the local version
	 */
	@OPERATION
	void searchSendContradictions(OpFeedbackParam<Boolean> res)
	{	
		res.set(false);
		//OpFeedbackParam<J48> mRes = new OpFeedbackParam<J48>();	
																
			Instances newExamples = new Instances(this.examples, 0);			
			if(this.examples!= null)
			{				
			for(Instance ej : this.examples) //check class
			{
				OpFeedbackParam<Boolean> corCla = new OpFeedbackParam<Boolean>();				
				try 
				{														
					execLinkedOp("portExa1","correctlyClassifies", ej, corCla);										
				} 
				
				catch (Exception ex)
				{
					ex.printStackTrace(); //ignore because only one of the two link operations is executed in two artifacts at the same time
				}
				if(corCla.get() != null && !corCla.get()) //contradiction found
				{		
					try
					{
						execLinkedOp("portExa1","addInstance", ej);
						res.set(true);
					}
					catch(Exception e){ e.printStackTrace();}
				}
				else
				{						
					newExamples.add(ej)	;											
				}
			}
		}			
			this.examples = newExamples;						
		
					
	}
	
	/*
	 * To send counter examples to the list of ports, local version
	 */
	@OPERATION
	void searchSendContradictionsMultiple(Object[] ports, OpFeedbackParam<Boolean> res)
	{	
		res.set(false);
		//OpFeedbackParam<J48> mRes = new OpFeedbackParam<J48>();	
																
			Instances newExamples = new Instances(this.examples, 0);			
			if(this.examples!= null)
			{				
			for(Instance ej : this.examples) //check class
			{
				OpFeedbackParam<Boolean> corCla = new OpFeedbackParam<Boolean>();				
				try 
				{														
					execLinkedOp("portExa1","correctlyClassifies", ej, corCla);										
				} 
				
				catch (Exception ex)
				{
					ex.printStackTrace(); //ignore because only one of the two link operations is executed in two artifacts at the same time
				}
				if(corCla.get() != null && !corCla.get()) //contradiction found
				{		
					res.set(true);
					try
					{
						for(Object p : ports)
						{
							String port = (String) p;
							execLinkedOp(port,"addInstance", ej);
						}
						
						
					}
					catch(Exception e){ e.printStackTrace();}
				}
				else
				{						
					newExamples.add(ej)	;											
				}
			}
		}			
			this.examples = newExamples;						
		
					
	}
	
	/*
	 * this is the remote version
	 */
	@OPERATION
	void searchSendContradictions(ArtifactId aid, OpFeedbackParam<Boolean> res)
	{	
		
		res.set(false);
		//OpFeedbackParam<J48> mRes = new OpFeedbackParam<J48>();	
		try
		{														
			Instances newExamples = new Instances(this.examples, 0);	
			Instances counter = new Instances(this.examples, 0);
			if(this.examples!= null)
			{				
			for(Instance ej : this.examples) //check class
			{

				try 
				{				
					//the model is loaded into the artifact
					if(!this.model.correctlyClassifies(ej))
					{
						counter.add(ej);
						res.set(true);	
					}					
					else
					{						
						newExamples.add(ej)	;											
					}
				} 
				
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
			System.out.println("COUNTER Encontrados!!! " + counter.numInstances());
			execLinkedOp(aid,"addInstances", counter);
			this.examples = newExamples;						
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}				
	}
	
	@OPERATION
	void parallelSearchSendContradictions(ArtifactId aid, int threads, OpFeedbackParam<Boolean> res)
	{	
		
		res.set(false);
		
		ParallelSearch ps = new ParallelSearch(this.examples, this.model, threads);
		Instances[] todo = ps.searchCounter();
		Instances counter = todo[0];
		Instances newExamples = todo[1];
		System.out.println("COUNTER Encontrados!!! " + counter.numInstances());
		try {
			execLinkedOp(aid,"addInstances", counter);
		} catch (OperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.examples = newExamples;
		if(counter.numInstances() > 0)
			res.set(true);
	}
	
	
	//Prepare the GPU and load the data into the GPU
	@OPERATION
	void initializeGPU()
	{
		synchronized (this)
		{
			CUcontext ctx = getContext();
	        //cuCtxSynchronize(); //for safety
	        //load all the data to the device, just once!!
			loadInstancesDevice(this.examples);
			cuCtxSynchronize();
			
		}
			
	}
	
	//percent if < 1 filter counter examples
	//min: min number of examples to consider per leaf
	@OPERATION
	void searchSendContradictionsGPU(ArtifactId aid, double percent, int min, OpFeedbackParam<Boolean> counterFound)
	{
		synchronized (this) 
		{
			
				
		cuCtxSetCurrent(this.context);
        

        // Load the ptx file.
        CUmodule module = new CUmodule();        
        cuModuleLoad(module, "ptx/gpu.ptx");
        
        // Obtain a function pointer to the "add" function.
        CUfunction function = new CUfunction();
        cuModuleGetFunction(function, module, "hilo");	        	        
        
        CUfunction function2 = new CUfunction();
        cuModuleGetFunction(function2, module, "countSize");
        
        CUfunction function3 = new CUfunction();
        cuModuleGetFunction(function3, module, "genResult");
        
        CUfunction function4 = new CUfunction();
        cuModuleGetFunction(function4, module, "filterParallel");
        
        CUfunction function5 = new CUfunction();
        cuModuleGetFunction(function5, module, "countersForLeaf");
        
        CUfunction function6 = new CUfunction();
        cuModuleGetFunction(function6, module, "countersPerLeaf");
        
        CUfunction function7 = new CUfunction();
        cuModuleGetFunction(function7, module, "filterCounters");
        	        	        
        
        cudaDeviceProp prop = new cudaDeviceProp();
        JCuda.cudaGetDeviceProperties(prop, 0);
        
        int multiProc = prop.multiProcessorCount;
        int maxThreads = prop.maxThreadsPerBlock;
		
		
		
		counterFound.set(false);
		CUDATree ar;
		if(this.gpuModel == null) //no gpu model received
			ar = ((J48CUDATree)this.model).convert();
		else
			ar = this.gpuModel;
		//CUDATree ar = this.gpuModel;
		Instances res =  new Instances(this.examples, 0);		
		CUdeviceptr deviceAttributes = new CUdeviceptr();				
		ar.loadToDevice(); //load model to device
		int chunk = (int) Math.ceil((double)examples.numInstances()/(multiProc * maxThreads));
		//System.out.println("CHUNKKKKK!!! " + chunk);
		
		 // Allocate device output memory
        CUdeviceptr deviceResHuecos = new CUdeviceptr();	        
        cuMemAlloc(deviceResHuecos, examples.numInstances() * examples.numAttributes() * Sizeof.FLOAT);
       
        CUdeviceptr deviceResHojas = new CUdeviceptr();	        
        cuMemAlloc(deviceResHojas, examples.numInstances() * examples.numAttributes() * Sizeof.FLOAT);
        
        CUdeviceptr deviceOutputIndi = new CUdeviceptr();	        
        cuMemAlloc(deviceOutputIndi, maxThreads * multiProc * Sizeof.INT);
        
        
        // Set up the kernel parameters: A pointer to an array
        // of pointers which point to the actual values.
        //float* ins, int insSize, int chunk, int* attributes, int* isLeaf,
        //int* numbersOfArcs, int* evalTypes, float* vals, int* nodeIndices, int MAX_NUM_ARCS, 
        //int NIntances, int* res, int* used
        Pointer kernelParameters = Pointer.to(
            Pointer.to(this.deviceInstances),
            Pointer.to(new int[]{examples.numAttributes()}),
            Pointer.to(new int[]{chunk}),
            Pointer.to(ar.deviceAttributes),
            Pointer.to(ar.deviceIsLeaf),
            Pointer.to(ar.deviceNumberOfArcs),
            Pointer.to(ar.deviceEvalTypes),
            Pointer.to(ar.deviceVals),
            Pointer.to(ar.deviceNodeIndices),
            Pointer.to(new int[]{ar.MAX_NUM_ARCS}),
            Pointer.to(new int[]{examples.numInstances()}),
            Pointer.to(deviceResHuecos),
            Pointer.to(deviceOutputIndi),
            Pointer.to(deviceResHojas)
        );
        	       
        
        System.out.println("Inicio proceso GPU");
		long startTime= System.nanoTime();	
        //int gridSizeX = (int)Math.ceil((double)numElements / blockSizeX);
        cuLaunchKernel(function,
            multiProc,  1, 1,      // Grid dimension
            maxThreads, 1, 1,      // Block dimension
            0, null,               // Shared memory size and stream
            kernelParameters, null // Kernel- and extra parameters
        );
        cuCtxSynchronize();
        
      long endTime= System.nanoTime();
	  double aux = ((double)(endTime - startTime)/1000000000); //time in seconds
	  System.out.println("Tiempo total gpu:" + (Math.floor(aux * 10000) / 10000));
        
	  		  

         // Allocate device output memory
        CUdeviceptr deviceLenCounter = new CUdeviceptr();	        
        cuMemAlloc(deviceLenCounter, 1 * Sizeof.INT);	        
        
        // Set up the kernel parameters: A pointer to an array
        // of pointers which point to the actual values.
        //float* ins, int insSize, int chunk, int* attributes, int* isLeaf,
        //int* numbersOfArcs, int* evalTypes, float* vals, int* nodeIndices, int MAX_NUM_ARCS, 
        //int NIntances, int* res, int* used
        Pointer kernelParameters2 = Pointer.to(
        	Pointer.to(deviceOutputIndi),
        	Pointer.to(new int[]{multiProc * maxThreads}),
        	Pointer.to(deviceLenCounter)	            
        );
        	       
        	        
        //int gridSizeX = (int)Math.ceil((double)numElements / blockSizeX);
        cuLaunchKernel(function2,
            1,  1, 1,      // Grid dimension
            1, 1, 1,      // Block dimension
            0, null,               // Shared memory size and stream
            kernelParameters2, null // Kernel- and extra parameters
        );
        cuCtxSynchronize();
        		
        
        int lenCounter[] = new int[1];
        cuMemcpyDtoH(Pointer.to(lenCounter), deviceLenCounter,
        	1 * Sizeof.INT);
        if(lenCounter[0] > 0) //the other steps only make sense if there are counter examples
        {
        // Allocate device output memory
        CUdeviceptr deviceCounterIndi = new CUdeviceptr();	        
        cuMemAlloc(deviceCounterIndi,lenCounter[0]  * Sizeof.INT);	 
        
        CUdeviceptr deviceCounterHojas = new CUdeviceptr();	        
        cuMemAlloc(deviceCounterHojas,lenCounter[0]  * Sizeof.INT);
 
        
        // Set up the kernel parameters: A pointer to an array
        // of pointers which point to the actual values.
        //float* ins, int insSize, int chunk, int* attributes, int* isLeaf,
        //int* numbersOfArcs, int* evalTypes, float* vals, int* nodeIndices, int MAX_NUM_ARCS, 
        //int NIntances, int* res, int* used
        Pointer kernelParameters3 = Pointer.to(
        	Pointer.to(deviceResHuecos),
        	Pointer.to(deviceResHojas),
        	Pointer.to(deviceOutputIndi),
        	Pointer.to(new int[]{multiProc * maxThreads}),
        	Pointer.to(new int[]{chunk}),
        	Pointer.to(deviceCounterIndi),
        	Pointer.to(deviceCounterHojas)
        );
        //int gridSizeX = (int)Math.ceil((double)numElements / blockSizeX);
        cuLaunchKernel(function3,
            1,  1, 1,      // Grid dimension
            1, 1, 1,      // Block dimension
            0, null,               // Shared memory size and stream
            kernelParameters3, null // Kernel- and extra parameters
        );
        cuCtxSynchronize();
        
        if(percent < 1) //the counter filtering only makes sense if percent < 1
        {
        int hojas = ar.nLeaf;
        // Allocate device output memory
        CUdeviceptr deviceCountersInHojas = new CUdeviceptr();	        
        cuMemAlloc(deviceCountersInHojas,hojas  * Sizeof.INT);
        Pointer kernelParameters5 = Pointer.to(
        		Pointer.to(new int[]{lenCounter[0]}),
        		Pointer.to(deviceCounterHojas),
        		Pointer.to(new int[]{hojas}),
        		Pointer.to(deviceCountersInHojas)
            );
        cuLaunchKernel(function5,
                1,  1, 1,      // Grid dimension
                1, 1, 1,      // Block dimension
                0, null,               // Shared memory size and stream
                kernelParameters5, null // Kernel- and extra parameters
            );
            cuCtxSynchronize();
        
        
        CUdeviceptr deviceCountersPerHojas = new CUdeviceptr();		        
        cuMemAlloc(deviceCountersPerHojas,hojas  * Sizeof.INT);
        CUdeviceptr deviceLenNewCounters = new CUdeviceptr();	        
        cuMemAlloc(deviceLenNewCounters, 1 * Sizeof.INT);
        Pointer kernelParameters6 = Pointer.to(
        		Pointer.to(new int[]{hojas}),
        		Pointer.to(deviceCountersInHojas),
        		Pointer.to(new float[]{(float) percent}),
        		Pointer.to(new int[]{min}),        		
        		Pointer.to(deviceCountersPerHojas),
        		Pointer.to(deviceLenNewCounters)
            );
        cuLaunchKernel(function6,
                1,  1, 1,      // Grid dimension
                1, 1, 1,      // Block dimension
                0, null,               // Shared memory size and stream
                kernelParameters6, null // Kernel- and extra parameters
            );
            cuCtxSynchronize();
            
            int lenNewCounter[] = new int[1];
            cuMemcpyDtoH(Pointer.to(lenNewCounter), deviceLenNewCounters,
            	1 * Sizeof.INT); 
            CUdeviceptr deviceContador = new CUdeviceptr();		        
            cuMemAlloc(deviceContador,hojas  * Sizeof.INT);
            CUdeviceptr deviceNewCounters = new CUdeviceptr();		        
            cuMemAlloc(deviceNewCounters,lenNewCounter[0]  * Sizeof.INT);
            Pointer kernelParameters7 = Pointer.to(
            		Pointer.to(new int[]{hojas}),
            		Pointer.to(new int[]{lenCounter[0]}),
            		Pointer.to(deviceCounterIndi),
            		Pointer.to(deviceCounterHojas),
            		Pointer.to(deviceCountersPerHojas),
            		Pointer.to(deviceContador),
            		Pointer.to(deviceNewCounters)            	
                );
            cuLaunchKernel(function7,
                    1,  1, 1,      // Grid dimension
                    1, 1, 1,      // Block dimension
                    0, null,               // Shared memory size and stream
                    kernelParameters7, null // Kernel- and extra parameters
                );
                cuCtxSynchronize();
            
            //change deviceCounterIndi to new value
            CUdeviceptr auxIndiCounters = deviceCounterIndi;
            lenCounter[0] = lenNewCounter[0];
            deviceCounterIndi = deviceNewCounters;
            
            //clen
            cuMemFree(auxIndiCounters);
            cuMemFree(deviceCountersInHojas);
            cuMemFree(deviceCountersPerHojas);
            cuMemFree(deviceLenNewCounters);
            cuMemFree(deviceContador);
            
        }
        
        int hostCounterIndi[] = new int[lenCounter[0]];
        cuMemcpyDtoH(Pointer.to(hostCounterIndi), deviceCounterIndi,
        	lenCounter[0] * Sizeof.INT);
        
        
     // Allocate device output memory
        CUdeviceptr deviceNewData = new CUdeviceptr();	        
        cuMemAlloc(deviceNewData, ((examples.numInstances() - lenCounter[0]) * examples.numAttributes()) * Sizeof.FLOAT);	        
        
        // Set up the kernel parameters: A pointer to an array
        // of pointers which point to the actual values.
        //float* ins, int insSize, int chunk, int* attributes, int* isLeaf,
        //int* numbersOfArcs, int* evalTypes, float* vals, int* nodeIndices, int MAX_NUM_ARCS, 
        //int NIntances, int* res, int* used
        Pointer kernelParameters4 = Pointer.to(
        	Pointer.to(deviceCounterIndi),		
        	Pointer.to(deviceOutputIndi),
        	Pointer.to(this.deviceInstances),
        	Pointer.to(new int[]{examples.numInstances()}),
        	Pointer.to(new int[]{examples.numAttributes()}),
        	Pointer.to(new int[]{chunk}),
        	Pointer.to(new int[]{multiProc * maxThreads}),
        	Pointer.to(deviceNewData)	            
        );
        	       	        	        
        //int gridSizeX = (int)Math.ceil((double)numElements / blockSizeX);
           cuLaunchKernel(function4,
            multiProc,  1, 1,      // Grid dimension
            maxThreads, 1, 1,      // Block dimension
            0, null,               // Shared memory size and stream
            kernelParameters4, null // Kernel- and extra parameters
        );
        
        /*Pointer kernelParameters4 = Pointer.to(
            	Pointer.to(deviceCounterIndi),		
            	Pointer.to(this.deviceInstances),
            	Pointer.to(new int[]{datos.numInstances()}),
            	Pointer.to(new int[]{datos.numAttributes()}),            	
            	Pointer.to(deviceNewData)	            
            );
            //int gridSizeX = (int)Math.ceil((double)numElements / blockSizeX);
               cuLaunchKernel(function4,
                1,  1, 1,      // Grid dimension
                1, 1, 1,      // Block dimension
                0, null,               // Shared memory size and stream
                kernelParameters4, null // Kernel- and extra parameters
            );*/
        
         
         //do this part parallel to the device
         
        res = returnCounter(hostCounterIndi);
        System.out.println("Contras Encontrados!!!: " + res.size());
         
        cuCtxSynchronize(); //see if the device finished  
        cuMemFree(this.deviceInstances); 
        this.deviceInstances = deviceNewData;
        
        
     // Clean up.        
        cuMemFree(deviceCounterIndi);
        cuMemFree(deviceCounterHojas);               
        //note that the data isn't cleaned here
        }
        
        ar.freeDevice();        
        cuMemFree(deviceResHuecos);
        cuMemFree(deviceOutputIndi);
        cuMemFree(deviceLenCounter);         
        
        //return res;     
        try 
        {
			execLinkedOp(aid,"addInstances", res);
		} 
        catch (OperationException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(res.numInstances() > 0)
        	counterFound.set(true);
       
		}
		
		
		
	}
	
	public Instances returnCounter(int[] indexes)
	{		
						
		Instances res = new Instances(this.examples, 0);
		Instances newData = new Instances(this.examples, 0);
		int i = 0;
		int resi = 0;		
		for(Instance ins : this.examples)
		{			
			if(resi < indexes.length &&  i == indexes[resi])
			{
				res.add(ins);
				resi++;
			}
			else
				newData.add(ins);
			i++;
		}		
		this.examples = newData;
		return res;
	}
	
	@OPERATION 
	void freeGPU()
	{
		synchronized(this)
		{
		
	    cuCtxSetCurrent(this.context);		
		cuMemFree(this.deviceInstances);
		cuCtxSynchronize();
		cuCtxDestroy(this.context);
		}
	}
	
	
	@OPERATION 
	void clearExamples()
	{
		Instances header = new Instances(this.examples, 0);
		this.examples = header; //all the examples are erased
	}
	
	
	/*
	 * Use this one when artifacts can be linked (they are in the same node)
	 */
	@OPERATION
	void sendAllExamples()
	{
		try 
		{		
				
			execLinkedOp("portExa1","addInstances", this.examples);
			
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		//Instances header = new Instances(this.examples, 0);
		//this.examples = header; //all the examples are erased
	}
	
	/*
	 * This version is for remote use
	 */
	@OPERATION
	void sendAllExamples(ArtifactId aid)
	{
		try
		  {
			
			execLinkedOp(aid,"addInstances", this.examples);
			
			 
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
		  }
		//Instances header = new Instances(this.examples, 0);
		//this.examples = header; //all the examples are erased
	}
	
	
	/*
	 * This version is for local use
	 * the percentage is a stratified one (used for initial model)
	 */
	@OPERATION
	void sendPercentageOfExamples(double per)
	{
		
		Instances newExamples = new Instances(this.examples, 0);
		Instances r = new Instances(this.examples, 0);
		//try to create a balanced sample
		int tam = (int) (this.examples.numInstances() * per);
		int perClass = tam/this.examples.numClasses(); //how many instances per class
		int[] sums = new int[this.examples.numClasses()];
		for(int i = 0; i < sums.length; i++)
			sums[i] = 0;
		for(Instance ins : this.examples)
		{
			int cval = (int)ins.classValue();
			if(sums[cval] < perClass)
			{
				sums[cval]++;
				newExamples.add(ins);
			}
			else
				r.add(ins);
		}
		
		this.examples = r;
		
		/*int folds = nFoldsPer(per);
		this.examples.stratify(folds);
		newExamples = this.examples.testCV(folds, 0);
		this.examples = this.examples.trainCV(folds, 0);
		this.examples.randomize(new Random());*/
		
		/*int count = (int) (this.examples.numInstances() * per);
		Instances newExamples = new Instances(this.examples, 0, count);
		this.examples = new Instances(this.examples, count, this.examples.numInstances()-count); //delete the fragment*/
		
		try
		  {			
			execLinkedOp("portExa1","addInstances", newExamples);						 
		  }
		  catch(Exception e)
		  {
			  //e.printStackTrace();
		  }
	}
	
	/*
	 * This version is for remote use
	 */
	@OPERATION
	void sendPercentageOfExamples(ArtifactId aid, double per)
	{
		int count = (int) (this.examples.numInstances() * per);
		Instances newExamples = new Instances(this.examples, 0, count);
		this.examples = new Instances(this.examples, count, this.examples.numInstances()-count); //delete the fragment
		
		try
		  {
			
			execLinkedOp(aid,"addInstances", newExamples);
			
			 
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
		  }
	}
	
	private String attsInfo()
	{
	    
	    //extract attributes info and add believes
        String atts = "atts(" + examples.numAttributes() + ",["; //how many atts
        for(int i = 0; i < examples.numAttributes(); i++)
        {
            Attribute attr = examples.attribute(i);            
            atts += "[" + normalice(attr.name()) + ", [";
            String vaux = "";
            for(int j = 0; j < attr.numValues(); j++)
            {
                String val = normalice(attr.value(j));
                vaux += val + ",";
            }
            if(!vaux.equals(""))
                vaux = vaux.substring(0, vaux.length()-1); //trim extra ,
            atts += vaux + "]],";
        }
        atts = atts.substring(0, atts.length()-1) + "])";       
        return atts;
	}
	
	  @LINK
	  void receiveModel(BaseClassifier model) 
	  {
		  this.model = model;
		  signal("modelReceived");
	  }
	  
	  @LINK
	  void receiveModelGPU(CUDATree gpuModel) 
	  {
		  this.gpuModel = gpuModel;
		  signal("modelReceived");
	  }
	  
		  
	  @LINK		 
	  void addInstance(Instance ins) 
	  {
			this.examples.add(ins);
	  }
	  
	  @LINK		 
	  void addInstances(Instances ins) 
	  {
		  for(Instance in : ins)
			this.examples.add(in);
	  }
	 
	  
	  @OPERATION
	  void prueba()
	  {
		  OpFeedbackParam<String> res = new OpFeedbackParam<String>();
		  for(int i = 0; i < 2; i++)
		  {
		  try
		  {			
			execLinkedOp("portExa1","pp", res);						 
		  }
		  catch(Exception e)
		  {
			 // e.printStackTrace();
		  }
		  if(res.get() != null)
			  System.out.println("todo ok");
		  else
			  System.out.println("todo MALLL");
		  }
	  }
	
	 private  String normalice(String name)
	 {
		 return "\"" + name + "\"";
	 }
	
	 //Returns the number of folds required for this percent (approximation)
	 private int nFoldsPer(double per)
	 {
		 return (int) Math.round(1/per);
	 }
	 
	 private  void loadInstancesDevice(Instances inss)
		{
			float[] reservar = new float[inss.numInstances()*inss.numAttributes()];
			int i = 0;
			for(Instance ins : inss)
			{			
				float[] iii = CUDATree.shrink(ins.toDoubleArray());
				for(int j = 0; j < iii.length; j++)
				{
					reservar[i] = iii[j];
					i++;
				}
			}
			deviceInstances = new CUdeviceptr();
			cuMemAlloc(deviceInstances, reservar.length * Sizeof.FLOAT);
	        cuMemcpyHtoD(deviceInstances, Pointer.to(reservar),
	            reservar.length * Sizeof.FLOAT);
		}
	 
	 private  CUcontext getContext()
	 {
		 JCudaDriver.setExceptionsEnabled(true);

	        // Create the PTX file by calling the NVCC
	        //String ptxFileName = preparePtxFile("JCudaVectorAddKernel.cu");

	        // Initialize the driver and create a context for the first device.
	        cuInit(0);
	        CUdevice device = new CUdevice();
	        cuDeviceGet(device, 0);
	       // try //maybe you can't do this
	       // {
	        	context = new CUcontext();
	        	cuCtxCreate(context, 0, device);
	       // }
	      /*  catch(Exception e)
	        {
	        	getContext(); //call itsel until the context is correct
	        }	     */  
	        return context;
	 }
	 
	 @OPERATION 
	 public void dumpInstances2File(String path)
	 {
		 ArffSaver saver = new ArffSaver();
			saver.setInstances(this.examples);
			try
			 {
				 saver.setFile(new File(path));
				 saver.setDestination(new File(path));  
				 saver.writeBatch();
			 }
			 catch(Exception e)
			 {
				 e.printStackTrace();
			 }
	 }
	 
	 
}
