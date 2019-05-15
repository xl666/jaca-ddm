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


import java.util.Random;

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
import static jcuda.driver.JCudaDriver.cuCtxCreate;
import static jcuda.driver.JCudaDriver.cuCtxSetCurrent;
import static jcuda.driver.JCudaDriver.cuCtxSynchronize;
import static jcuda.driver.JCudaDriver.cuDeviceGet;
import static jcuda.driver.JCudaDriver.cuInit;
import static jcuda.driver.JCudaDriver.cuLaunchKernel;
import static jcuda.driver.JCudaDriver.cuMemAlloc;
import static jcuda.driver.JCudaDriver.cuMemFree;
import static jcuda.driver.JCudaDriver.cuMemcpyDtoH;
import static jcuda.driver.JCudaDriver.cuMemcpyHtoD;
import static jcuda.driver.JCudaDriver.cuModuleGetFunction;
import static jcuda.driver.JCudaDriver.cuModuleLoad;
import static jcuda.driver.JCudaDriver.cuCtxDestroy;
import classifiers.BaseClassifier;
import classifiers.CUDATree;
import classifiers.J48CUDATree;
import utils.InstanceManager;
import utils.Statistic;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import cartago.*;
import weka.classifiers.trees.J48;



import weka.experiment.PairedStats;



@ARTIFACT_INFO(
		  outports = {
		    @OUTPORT(name="port1"), // Port used to link with ClassifierJ48
		    @OUTPORT(name="port2")  // Port used to link with Oraculo
		  }
		)

public class Evaluator extends Artifact {	
	private Instances testData;
	private BaseClassifier model;	
	private Evaluation eval;
	private CUdeviceptr deviceInstances;
	private CUcontext context;
	private CUDATree gpuModel;
	private int nLeaves = 0;
	private int treeSize = 0;
	private int iters = 0;
	private int[][] overallConfusionMatrix = null;
	
	
	//Copy Tree from ClassifierJ48 to Evaluator
	//all the classifier artifacts must implement a getModel operation
	  @OPERATION void getModelFromClassifier() {
	    OpFeedbackParam<BaseClassifier> v = new OpFeedbackParam<BaseClassifier>();
	    try{
	    	
	    	execLinkedOp("port1", "getModel", v);		    
	    	this.model = v.get();   
	    }catch (Exception ex){
	    	ex.printStackTrace();
	    }
	    
	  }
	  
	  
	  @LINK
	  void receiveModel(BaseClassifier model) 
	  {
		  this.model = model;
		  signal("readyToEvaluate");
	  }
	  
	  @LINK
	  void receiveModelGPU(CUDATree gpuModel) 
	  {
		  this.gpuModel = gpuModel;
		  signal("readyToEvaluate");
	  }
	  
	  @OPERATION void transformModel2GPU()
	  {
		 gpuModel = ((J48CUDATree)this.model).convert();  
		  
	  }
	  
	//Copy Test Data from Oracle to Evaluator
	  @OPERATION void getTestDataFromOraculo() {
		    OpFeedbackParam<Instances> v = new OpFeedbackParam<Instances>();
		    try{
		    		execLinkedOp("port2","getTesting", v);
		    		
	    			this.testData = (new Instances(v.get()));
		    } catch (Exception ex){
		      ex.printStackTrace();
		    }
		  }
	 
	  
	//Evaluate the model received from the artifact ClassifierJ48  
	@OPERATION
	void evaluate(	OpFeedbackParam<java.lang.Double> pcCorrect,
					OpFeedbackParam<java.lang.Double> Correct,
					OpFeedbackParam<java.lang.Double> Incorrect,
					OpFeedbackParam<java.lang.Integer> trainedExemaples) 
	{
		try{ 
			double correct = 0;
			double incorrect = 0;
			double porcenCorrect = 0;
			
			for(int i = 0; i < this.testData.numInstances(); i++)
			{
				Instance ins = this.testData.get(i);
				if(this.model.correctlyClassifies(ins))
				{
					correct++;
				}
				else
					incorrect++;				
			}
			porcenCorrect = ((double)correct/testData.numInstances()) * 100;
			pcCorrect.set(porcenCorrect);
			Correct.set(correct);
			Incorrect.set(incorrect);
			trainedExemaples.set(this.model.getExamplesTrained());
			
		} 
		catch (Exception ex) {
		     ex.printStackTrace();
		}
		
		
	}
	
	@OPERATION
	void evaluateWithConfusion(	OpFeedbackParam<java.lang.Double> pcCorrect,
					OpFeedbackParam<java.lang.Double> Correct,
					OpFeedbackParam<java.lang.Double> Incorrect,
					OpFeedbackParam<java.lang.Integer> trainedExemaples,
					OpFeedbackParam<java.lang.String> confusionMatrix) 
	{
		try{ 
			double correct = 0;
			double incorrect = 0;
			double porcenCorrect = 0;
			int[][] confusion = new int[this.testData.numClasses()][this.testData.numClasses()];
			if(this.overallConfusionMatrix == null)
			{
				this.overallConfusionMatrix = new int[this.testData.numClasses()][this.testData.numClasses()];
				for(int i = 0; i < confusion.length; i++)
				{
					for(int j = 0; j < confusion[0].length; j++)
					{
						this.overallConfusionMatrix[i][j] = 0;
					}
				}
			}
			for(int i = 0; i < confusion.length; i++)
			{
				for(int j = 0; j < confusion[0].length; j++)
				{
					confusion[i][j] = 0;
				}
			}
			
			for(int i = 0; i < this.testData.numInstances(); i++)
			{
				Instance ins = this.testData.get(i);
				int cPredicted = (int)this.model.classifyInstance(ins);
				int cActual = (int)ins.classValue();
				confusion[cActual][cPredicted]++;
				this.overallConfusionMatrix[cActual][cPredicted]++;
				if(cPredicted == cActual)
				{
					correct++;
				}
				else
					incorrect++;				
			}
			porcenCorrect = ((double)correct/testData.numInstances()) * 100;
			pcCorrect.set(porcenCorrect);
			Correct.set(correct);
			Incorrect.set(incorrect);
			confusionMatrix.set(this.model.getComplexity() + "\n" + matrix2String(confusion));
			if(this.model instanceof J48)
			{
				this.nLeaves += ((J48)this.model).measureNumLeaves();
				this.treeSize += ((J48)this.model).measureTreeSize();
				this.iters++;
			}
			
			trainedExemaples.set(this.model.getExamplesTrained());
			
		} 
		catch (Exception ex) {
		     ex.printStackTrace();
		}
		
		
	}
		
	
	@OPERATION
	void getOverallConfusionMatrix(OpFeedbackParam<java.lang.String> confusionMatrix)
	{
		String res = matrix2String(this.overallConfusionMatrix);
		if(this.model instanceof J48)
		{
			String leaves = "Overall number of leaves: " + ((float)this.nLeaves/this.iters);
			String ts = "\nOverall tree size: " + ((float)this.treeSize/this.iters) + "\n";
			res = leaves + ts + res;
		}
		confusionMatrix.set(res);
	}
	
	private String matrix2String(int[][] matrix)
	{
		String header = "Confusion Matrix:\n\tPredicted\nActual";
		for(int i = 0; i < this.testData.numClasses(); i++)
		{
			header += "\t" + this.testData.classAttribute().value(i);
		}
		header += "\n";
		String res = "";
		for(int i = 0; i < matrix.length; i++)
		{
			res += this.testData.classAttribute().value(i) + "\t";
			for(int j = 0; j < matrix[0].length-1; j++)
			{
				res += matrix[i][j] + "\t";
			}
			res += matrix[i][matrix.length-1];
			res += "\n";
		}
				
		
		
		return header+res;
	}
	
	
	//Evaluate the model received from the artifact ClassifierJ48  
		@OPERATION
		void evaluateGPU(OpFeedbackParam<java.lang.Double> pcCorrect,
						OpFeedbackParam<java.lang.Double> Correct,
						OpFeedbackParam<java.lang.Double> Incorrect,
						OpFeedbackParam<java.lang.Integer> trainedExemaples) 
		{
			try{ 
				double correct = 0;
				double incorrect = 0;
				double porcenCorrect = 0;
				
				/*for(int i = 0; i < this.testData.numInstances(); i++)
				{
					Instance ins = this.testData.get(i);
					if(this.model.correctlyClassifies(ins))
					{
						correct++;
					}
					else
						incorrect++;				
				}*/
				
				cuCtxSetCurrent(this.context);
		        

		        // Load the ptx file.
		        CUmodule module = new CUmodule();        
		        cuModuleLoad(module, "ptx/gpu.ptx");
		        
		        // Obtain a function pointer to the "add" function.
		        CUfunction function = new CUfunction();
		        cuModuleGetFunction(function, module, "calcAcc");	        	        
		        
		        CUfunction function2 = new CUfunction();
		        cuModuleGetFunction(function2, module, "totalAcc");
		        
		        cudaDeviceProp prop = new cudaDeviceProp();
		        JCuda.cudaGetDeviceProperties(prop, 0);
		        
		        int multiProc = prop.multiProcessorCount;
		        int maxThreads = prop.maxThreadsPerBlock;
		        
		        CUDATree ar = this.gpuModel;
		        
		        ar.loadToDevice(); //load model to device
				int chunk = (int) Math.ceil((double)testData.numInstances()/(multiProc * maxThreads));
		       			
				// Allocate device output memory
		        CUdeviceptr deviceResAcc = new CUdeviceptr();	        
		        cuMemAlloc(deviceResAcc, multiProc * maxThreads * Sizeof.INT);
		        
		        Pointer kernelParameters = Pointer.to(
		                Pointer.to(this.deviceInstances),
		                Pointer.to(new int[]{testData.numAttributes()}),
		                Pointer.to(new int[]{chunk}),
		                Pointer.to(ar.deviceAttributes),
		                Pointer.to(ar.deviceIsLeaf),
		                Pointer.to(ar.deviceNumberOfArcs),
		                Pointer.to(ar.deviceEvalTypes),
		                Pointer.to(ar.deviceVals),
		                Pointer.to(ar.deviceNodeIndices),
		                Pointer.to(new int[]{ar.MAX_NUM_ARCS}),
		                Pointer.to(new int[]{testData.numInstances()}),		               
		                Pointer.to(deviceResAcc)
		            );
		        
		        cuLaunchKernel(function,
		                multiProc,  1, 1,      // Grid dimension
		                maxThreads, 1, 1,      // Block dimension
		                0, null,               // Shared memory size and stream
		                kernelParameters, null // Kernel- and extra parameters
		            );
		            cuCtxSynchronize();
		            
		        CUdeviceptr deviceTotal = new CUdeviceptr();	        
			    cuMemAlloc(deviceTotal, 1 * Sizeof.INT);
			    
			    Pointer kernelParameters2 = Pointer.to(		                              
		                Pointer.to(deviceResAcc),
		                Pointer.to(new int[]{multiProc * maxThreads}),
		                Pointer.to(deviceTotal)		                		                
		            );
			    
			    cuLaunchKernel(function2,
		                1,  1, 1,      // Grid dimension
		                1, 1, 1,      // Block dimension
		                0, null,               // Shared memory size and stream
		                kernelParameters2, null // Kernel- and extra parameters
		            );
		        cuCtxSynchronize();
		        
		        int total[] = new int[1];
		        cuMemcpyDtoH(Pointer.to(total), deviceTotal,
		        	1 * Sizeof.INT);
		        
		        correct = total[0];
		        incorrect = testData.numInstances() - correct;
				
				porcenCorrect = ((double)correct/testData.numInstances()) * 100;
				pcCorrect.set(porcenCorrect);
				Correct.set(correct);
				Incorrect.set(incorrect);
				trainedExemaples.set(this.gpuModel.examplesTrained);
				
			} 
			catch (Exception ex) {
			     ex.printStackTrace();
			}
			
			
		}
			
	
	@OPERATION 
	void feedTestDataFromFile(String path)
	{
		try
		{
			this.testData = new InstanceManager().readArff(path);
			this.testData.setClassIndex(this.testData.numAttributes() - 1);
			//this.testData.randomize(new Random());

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	 /*
     * Performs a paired T Test over the results of classification accuracy of 2 classifiers 
     * Returns 1 if res1 is significantly better than res2
     * Returns 0 if there aren't significant differences 
     * Returns -1 if res1 is significantly worser than res2
     */
	@OPERATION
	void pairedTTest(Object[] res1, Object[] res2, OpFeedbackParam<java.lang.Integer> Significance )
    {	
		int size=res1.length;
		double[] res1Double = new double[size];
		double[] res2Double = new double[size];

		for(int i=0; i < size; i++)
			res1Double[i]=Double.parseDouble(res1[i].toString());
			
		for(int i=0; i < size; i++)
			res2Double[i]=Double.parseDouble(res2[i].toString());
		
        PairedStats st = new PairedStats(0.05);        
        st.add(res1Double, res2Double);
        st.calculateDerived();
        Significance.set(st.differencesSignificance);
    }
    
    
    /*
     * Operation to obtain the mean from an array of numeric objects
     */
	 @OPERATION
	 void mean(Object[] list, OpFeedbackParam<java.lang.Double> Feedback) 
	 {
	 	double mean = Statistic.Mean(list);		 			 	
        Feedback.set(mean);	        
	 }
	 
	 
   	/*
     * Operation to obtain the standard deviation from an array of numeric objects
     */
	 @OPERATION
	 void stdDev(Object[] list, OpFeedbackParam<java.lang.Double> Feedback) 
	 {
		 double stdDev = Statistic.StdDev(list);
		 Feedback.set(stdDev);
	 }	
	 
	 @LINK
	void initExamples(Instances header)
	{
		 this.testData = header;
	}
	 
	 @LINK		 
	  void addInstances(Instances ins) 
	  {
		  for(Instance in : ins)
			this.testData.add(in);
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
				loadInstancesDevice(this.testData);	
				cuCtxSynchronize();
				
			}
			
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
	            reservar.length * Sizeof.INT);
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
	 
}


